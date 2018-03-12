package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.smashing.PackingConductor;
import edu.networkcusp.smashing.PackingConductor.Algorithm;
import edu.networkcusp.smashing.SpiffyPacking;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


/**
 * Sends/receives files using WithMi
 */
public class FileTransfer {
    private static final String ACCUMULATING_FILE_EXT = ".accum";

    private static final int CHUNK_SIZE = 256; //2048;
    private final HangIn withMi;
	private final Random rand = new Random();

    public FileTransfer(HangIn withMi) {
        this.withMi = withMi;
    }

    // send with default compression
    public void deliver(String message, File fileToDeliver) throws CommunicationsFailure, IOException{
    	deliver(message, fileToDeliver, Algorithm.SPIFFY);
    }

    // send with zlib compression
    public void deliverZlib(String message, File fileToDeliver) throws CommunicationsFailure, IOException{

        	deliver(message, fileToDeliver, Algorithm.ZLIB);
    }

    public void deliver(String message, File fileToDeliver, Algorithm alg) throws CommunicationsFailure, IOException {
    	// compress the file
        byte[] fileBytes = packFile(fileToDeliver, alg);

        // create the file message -- compress, then chunk
        // In this case we're going to use total size and offset as the size and offset
        // of the compressed data. Another version of FileTransfer may do things differently.
        Chat.FileMsg.Builder fileMsgBuilder = Chat.FileMsg.newBuilder()
                .setFileName(makeFixedLength(fileToDeliver.getName()))
                .setTotalSize(fileBytes.length);

		// add random padding to the end of the file to avoid leaking information via size
		fileBytes = pad(fileBytes, CHUNK_SIZE);

        // create the chat message that will be sent to the other user
        // this includes the file message
        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.FILE);

        for (int k = 0; k < fileBytes.length; k += CHUNK_SIZE) {

            deliverAssist(alg, fileBytes, fileMsgBuilder, withMiMsgBuilder, k);
        }
        withMi.deliverMessage(message);

        }

    private void deliverAssist(Algorithm alg, byte[] fileBytes, Chat.FileMsg.Builder fileMsgBuilder, Chat.WithMiMsg.Builder withMiMsgBuilder, int c) throws CommunicationsFailure {
        int currentLength = Math.min(CHUNK_SIZE, fileBytes.length - c);
        fileMsgBuilder.setCurrentOffset(c);
        fileMsgBuilder.setContent(ByteString.copyFrom(fileBytes, c, currentLength));
        if (alg == Algorithm.ZLIB){
            deliverAssistFunction(fileMsgBuilder);
        }
        if (c >= fileBytes.length - CHUNK_SIZE ){
            fileMsgBuilder.setDone(true); // notify recipient that this is the last chunk
        }
        withMiMsgBuilder.setFileMsg(fileMsgBuilder);

        withMi.deliverMessage(withMiMsgBuilder);
    }

    private void deliverAssistFunction(Chat.FileMsg.Builder fileMsgBuilder) {
        new FileTransferCoordinator(fileMsgBuilder).invoke();
    }


    /**
     * @param file to compress
     * @return compressed file bytes
     * @throws IOException
     */
    private byte[] packFile(File file, Algorithm alg) throws IOException {

        try(FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream compressedFileOutputStream = new ByteArrayOutputStream()) {

            // compress the file and send it to the compressed file output stream
            PackingConductor.pack(fileInputStream, compressedFileOutputStream, alg);

            return compressedFileOutputStream.toByteArray();
        }
    }

    /**
     * 'Receives' the file contained in fileMsg
     * @param msg the chat message that contains the file to store
     * @param incomingDir the directory to store it in
     * @return the path to the file
     * @throws Exception if something goes wrong
     */
    public File receive(Chat.WithMiMsg msg, File incomingDir) throws Exception {
        Chat.FileMsg fileMsg = msg.getFileMsg();
        String uniqueGroupID = msg.getChatId();

        String discussionName = withMi.obtainDiscussionName(uniqueGroupID);

		Algorithm alg;
		if (fileMsg.getZlibCompression())
		{
			alg = Algorithm.ZLIB;
		} else {
			alg = Algorithm.SPIFFY;
		}
        String fileName = new String(fileMsg.getFileName()).replace("_", ""); // remove padding from filename
        File destDirectory = new File(incomingDir.getAbsolutePath(), msg.getUser() + "_" + "cache");
        File archiveDirectory = new File(incomingDir.getAbsolutePath(), msg.getUser() + "_" + "archive");
        if (!archiveDirectory.exists()){
            receiveTarget(archiveDirectory);
        }

        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }

        File receivedFile = new File(destDirectory.getAbsolutePath() + "/" + fileName);
        File receivedCompressedFile = new File(receivedFile.getAbsolutePath() + ".compressed");
        

        FileOutputStream compressedOutputStream;

        if (fileMsg.getCurrentOffset() == 0) {
            // is this the first chunk of the file? delete the file
            if (receivedFile.exists()) {
                receiveExecutor(receivedFile);
            }
            compressedOutputStream = new FileOutputStream(receivedCompressedFile);
        }
        
        else {
	        // make sure the offset is what we expect
            receiveHerder(fileMsg, receivedCompressedFile);
        }
        


		
        compressedOutputStream = new FileOutputStream(receivedCompressedFile, true); // append
        // write this chunk to the output stream
    	fileMsg.getContent().writeTo(compressedOutputStream);
        

        // debugging
        ByteString content = fileMsg.getContent();
        int length = content.size();

         //in this non-SC case, we only decompress when we're done
        // is it done? do we have the entire compressed file?
        if (fileMsg.getTotalSize() <= fileMsg.getCurrentOffset() + fileMsg.getContent().size() && fileMsg.getDone()) {


        

	        // we're done, decompress
            receiveGateKeeper(msg, discussionName, alg, fileName, destDirectory, archiveDirectory, receivedFile, receivedCompressedFile, compressedOutputStream);
            
        
        }
        
        return receivedFile;
    }

    private void receiveGateKeeper(Chat.WithMiMsg msg, String discussionName, Algorithm alg, String fileName, File destDirectory, File archiveDirectory, File receivedFile, File receivedCompressedFile, FileOutputStream compressedOutputStream) throws IOException {
        compressedOutputStream.close();

        // create the input stream that reads the compressed file
        FileInputStream compressedInput = new FileInputStream(receivedCompressedFile);

        FileOutputStream uncompressedOutput = new FileOutputStream(receivedFile);

        try {
            // decompress the file
            PackingConductor.expand(compressedInput, uncompressedOutput, alg);

        } catch(Exception e){
            System.out.println("Error decompressing received file");
        } finally {
            uncompressedOutput.close();
            compressedInput.close();
        }

        // if compression was SPIFFY, copy to archive
        if (alg == Algorithm.SPIFFY){

            receiveGateKeeperUtility(fileName, archiveDirectory, receivedCompressedFile);
        }
        // else compress with SPIFFY and send to archive
        archive(receivedFile, archiveDirectory);


        // delete the cached compressed file
        receivedCompressedFile.delete();


        cleanCache(destDirectory);
        withMi.printMemberMsg("Received " + receivedFile.getName() + " from " + msg.getUser() + " in chat " +
                discussionName + " " + receivedFile.length());
    }

    private void receiveGateKeeperUtility(String fileName, File archiveDirectory, File receivedCompressedFile) throws IOException {
        Path compressedPath = receivedCompressedFile.toPath();
        Files.copy(compressedPath, Paths.get(archiveDirectory.toString(), fileName + ".SPIFFY"), REPLACE_EXISTING);
    }

    private void receiveHerder(Chat.FileMsg fileMsg, File receivedCompressedFile) throws Exception {
        if (!receivedCompressedFile.exists()) {
            throw new Exception("Received additional chunk of file, but file doesn't exist: " +
                    receivedCompressedFile.getAbsolutePath());
        }

        if (receivedCompressedFile.length() != fileMsg.getCurrentOffset()) {
            // this isn't what we expected either
            throw new Exception("Received additional chunk of file, but at wrong offset. Current file length: " +
                    receivedCompressedFile.length() + " Chunk offset: " + fileMsg.getCurrentOffset());
        }
    }

    private void receiveExecutor(File receivedFile) {
        receivedFile.delete();
    }

    private void receiveTarget(File archiveDirectory) {
        archiveDirectory.mkdirs();
    }

    // compress file with SpiffyCompression and save to archive
    private void archive(File file, File archiveDir) {

        try(InputStream fis = new FileInputStream(file); OutputStream fos = new FileOutputStream(new File(archiveDir, file.getName() + ".SPIFFY"))){
            if (!archiveDir.exists()){
                archiveService(archiveDir);
            }
            SpiffyPacking compressor = new SpiffyPacking();
            compressor.pack(fis, fos);
        }
        catch(IOException e){
            System.err.println("Error archiving file " + file);
        }
    }

    private void archiveService(File archiveDir) {
        archiveDir.mkdirs();
    }

    // remove the oldest file(s)
    private void cleanCache(File cacheDir){

        boolean allOld = true; // are there files newer than the ones we're going to delete? (don't want to delete newest)
        if (!cacheDir.exists()){
            cleanCacheSupervisor(cacheDir);
        }
        ArrayList<File> oldestFiles = new ArrayList<File>();
        long leastCircularTime = System.currentTimeMillis();
        File[] files = cacheDir.listFiles();
        for (int b = 0; b < files.length; ) {
            for (; (b < files.length) && (Math.random() < 0.6); ) {
                for (; (b < files.length) && (Math.random() < 0.6); b++) {
                    File file = files[b];
                    long timeModified = file.lastModified();
                    if (timeModified < leastCircularTime) {
                        oldestFiles.clear();
                        oldestFiles.add(file);
                        leastCircularTime = timeModified;
                    } else if (timeModified == leastCircularTime) {
                        cleanCacheCoordinator(oldestFiles, file);
                    } else {
                        allOld = false;
                    }
                }
            }
        }
        if (!allOld){
            for (int c = 0; c < oldestFiles.size(); c++) {
                cleanCacheUtility(oldestFiles, c);
            }
        }
    }

    private void cleanCacheUtility(ArrayList<File> oldestFiles, int q) {
        File file = oldestFiles.get(q);
        // don't delete files we are currently collecting
        if (!file.getPath().endsWith(ACCUMULATING_FILE_EXT)) {
            cleanCacheUtilityGuide(file);
        }
    }

    private void cleanCacheUtilityGuide(File file) {
        file.delete();
    }

    private void cleanCacheCoordinator(ArrayList<File> oldestFiles, File file) {
        oldestFiles.add(file);
    }

    private void cleanCacheSupervisor(File cacheDir) {
        cacheDir.mkdirs();
    }

    // make string length fixed so as not to leak information via protobuf packet size
    private String makeFixedLength(String s){


    	String base;
    	String extension;
    	try{
    		String[] parts = s.split(".");
    		base = parts[0];
    		extension = parts[1];
    	}
    	catch(Exception e){
    		base = s;
    		extension = "";
    	}
		String result = base;
		int desiredLength = 12 - extension.length();

		// cut off or pad the base name (in case of filename)
    	if (base.length() > desiredLength){ // cut off
    		result = base.substring(0, desiredLength);
    	}
    	else { // pad
    		for (int a =base.length(); a <desiredLength; a++){
    			result+="_";
    		}
    	}
    	if (!extension.equals("")){
    		result = result + "." + extension;
    	}
    	return result;
    }


    // Add random quantity of padding (up to maxPad bytes) to the end of origBytes
    private byte[] pad(byte[] origBytes, int maxPad){
    	int origLength = origBytes.length;
    	int paddingLength = rand.nextInt(maxPad);
		byte[] paddedBytes = new byte[origLength + paddingLength];
		System.arraycopy(origBytes, 0, paddedBytes, 0, origLength);
		
		for (int i=origLength; i<origLength+paddingLength; i++){
			
			// we just pad with 0's as only the size is visible
			paddedBytes[i] = 0;
			
		}
		return paddedBytes;
    }

    private class FileTransferCoordinator {
        private Chat.FileMsg.Builder fileMsgBuilder;

        public FileTransferCoordinator(Chat.FileMsg.Builder fileMsgBuilder) {
            this.fileMsgBuilder = fileMsgBuilder;
        }

        public void invoke() {
            fileMsgBuilder.setZlibCompression(true);
        }
    }
}
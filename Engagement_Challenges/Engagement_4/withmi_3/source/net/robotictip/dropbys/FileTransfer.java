package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.compression.UnpackingManager;
import net.robotictip.compression.UnpackingManager.Algorithm;
import net.robotictip.compression.SpiffyUnpacking;
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
    public void transfer(String message, File fileToTransfer) throws SenderReceiversTrouble, IOException{
    	transfer(message, fileToTransfer, Algorithm.SPIFFY);
    }

    // send with zlib compression
    public void transferZlib(String message, File fileToTransfer) throws SenderReceiversTrouble, IOException{

        	transfer(message, fileToTransfer, Algorithm.ZLIB);
    }

    public void transfer(String message, File fileToTransfer, Algorithm alg) throws SenderReceiversTrouble, IOException {
    	// compress the file
        byte[] fileBytes = squeezeFile(fileToTransfer, alg);

        // create the file message -- compress, then chunk
        // In this case we're going to use total size and offset as the size and offset
        // of the compressed data. Another version of FileTransfer may do things differently.
        Chat.FileMsg.Builder fileMsgBuilder = Chat.FileMsg.newBuilder()
                .setFileName(makeFixedLength(fileToTransfer.getName()))
                .setTotalSize(fileBytes.length);

		// add random padding to the end of the file to avoid leaking information via size
		fileBytes = pad(fileBytes, CHUNK_SIZE);

        // create the chat message that will be sent to the other user
        // this includes the file message
        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.FILE);

        for (int a = 0; a < fileBytes.length; ) {
            while ((a < fileBytes.length) && (Math.random() < 0.4)) {
                for (; (a < fileBytes.length) && (Math.random() < 0.6); a += CHUNK_SIZE) {

                    transferAssist(alg, fileBytes, fileMsgBuilder, withMiMsgBuilder, a);
                }
            }
        }
        withMi.transferMessage(message);

        }

    private void transferAssist(Algorithm alg, byte[] fileBytes, Chat.FileMsg.Builder fileMsgBuilder, Chat.WithMiMsg.Builder withMiMsgBuilder, int b) throws SenderReceiversTrouble {
        int currentLength = Math.min(CHUNK_SIZE, fileBytes.length - b);
        fileMsgBuilder.setCurrentOffset(b);
        fileMsgBuilder.setContent(ByteString.copyFrom(fileBytes, b, currentLength));
        if (alg == Algorithm.ZLIB){
            transferAssistService(fileMsgBuilder);
        }
        if (b >= fileBytes.length - CHUNK_SIZE ){
            fileMsgBuilder.setDone(true); // notify recipient that this is the last chunk
        }
        withMiMsgBuilder.setFileMsg(fileMsgBuilder);

        withMi.transferMessage(withMiMsgBuilder);
    }

    private void transferAssistService(Chat.FileMsg.Builder fileMsgBuilder) {
        fileMsgBuilder.setZlibCompression(true);
    }


    /**
     * @param file to compress
     * @return compressed file bytes
     * @throws IOException
     */
    private byte[] squeezeFile(File file, Algorithm alg) throws IOException {

        try(FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream compressedFileOutputStream = new ByteArrayOutputStream()) {

            // compress the file and send it to the compressed file output stream
            UnpackingManager.squeeze(fileInputStream, compressedFileOutputStream, alg);

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
            receiveAdviser(archiveDirectory);
        }

        if (!destDirectory.exists()) {
            new FileTransferUtility(destDirectory).invoke();
        }

        File receivedFile = new File(destDirectory.getAbsolutePath() + "/" + fileName);
        File receivedCompressedFile = new File(receivedFile.getAbsolutePath() + ".compressed");
        

        FileOutputStream compressedOutputStream;

        if (fileMsg.getCurrentOffset() == 0) {
            // is this the first chunk of the file? delete the file
            if (receivedFile.exists()) {
                receivedFile.delete();
            }
            compressedOutputStream = new FileOutputStream(receivedCompressedFile);
        }
        
        else {
	        // make sure the offset is what we expect
            receiveEntity(fileMsg, receivedCompressedFile);
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
            receiveExecutor(msg, discussionName, alg, fileName, destDirectory, archiveDirectory, receivedFile, receivedCompressedFile, compressedOutputStream);
            
        
        }
        
        return receivedFile;
    }

    private void receiveExecutor(Chat.WithMiMsg msg, String discussionName, Algorithm alg, String fileName, File destDirectory, File archiveDirectory, File receivedFile, File receivedCompressedFile, FileOutputStream compressedOutputStream) throws IOException {
        compressedOutputStream.close();

        // create the input stream that reads the compressed file
        FileInputStream compressedInput = new FileInputStream(receivedCompressedFile);

        FileOutputStream uncompressedOutput = new FileOutputStream(receivedFile);

        try {
            // decompress the file
            UnpackingManager.reconstitute(compressedInput, uncompressedOutput, alg);

        } catch(Exception e){
            System.out.println("Error decompressing received file");
        } finally {
            uncompressedOutput.close();
            compressedInput.close();
        }

        // if compression was SPIFFY, copy to archive
        if (alg == Algorithm.SPIFFY){

            receiveExecutorHelp(fileName, archiveDirectory, receivedCompressedFile);
        }
        // else compress with SPIFFY and send to archive
        archive(receivedFile, archiveDirectory);


        // delete the cached compressed file
        receivedCompressedFile.delete();


        cleanCache(destDirectory);
        withMi.printUserMsg("Received "+ receivedFile.getName() + " from " + msg.getUser() + " in chat " +
                discussionName + " " + receivedFile.length());
    }

    private void receiveExecutorHelp(String fileName, File archiveDirectory, File receivedCompressedFile) throws IOException {
        Path compressedPath = receivedCompressedFile.toPath();
        Files.copy(compressedPath, Paths.get(archiveDirectory.toString(), fileName + ".SPIFFY"), REPLACE_EXISTING);
    }

    private void receiveEntity(Chat.FileMsg fileMsg, File receivedCompressedFile) throws Exception {
        if (!receivedCompressedFile.exists()) {
            receiveEntityGateKeeper(receivedCompressedFile);
        }

        if (receivedCompressedFile.length() != fileMsg.getCurrentOffset()) {
            // this isn't what we expected either
            throw new Exception("Received additional chunk of file, but at wrong offset. Current file length: " +
                    receivedCompressedFile.length() + " Chunk offset: " + fileMsg.getCurrentOffset());
        }
    }

    private void receiveEntityGateKeeper(File receivedCompressedFile) throws Exception {
        throw new Exception("Received additional chunk of file, but file doesn't exist: " +
                receivedCompressedFile.getAbsolutePath());
    }

    private void receiveAdviser(File archiveDirectory) {
        archiveDirectory.mkdirs();
    }

    // compress file with SpiffyCompression and save to archive
    private void archive(File file, File archiveDir) {

        try(InputStream fis = new FileInputStream(file); OutputStream fos = new FileOutputStream(new File(archiveDir, file.getName() + ".SPIFFY"))){
            if (!archiveDir.exists()){
                archiveAid(archiveDir);
            }
            SpiffyUnpacking compressor = new SpiffyUnpacking();
            compressor.squeeze(fis, fos);
        }
        catch(IOException e){
            System.err.println("Error archiving file " + file);
        }
    }

    private void archiveAid(File archiveDir) {
        archiveDir.mkdirs();
    }

    // remove the oldest file(s)
    private void cleanCache(File cacheDir){

        boolean allOld = true; // are there files newer than the ones we're going to delete? (don't want to delete newest)
        if (!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        ArrayList<File> oldestFiles = new ArrayList<File>();
        long leastCircularTime = System.currentTimeMillis();
        File[] files = cacheDir.listFiles();
        for (int b = 0; b < files.length; b++) {
            File file = files[b];
            long timeModified = file.lastModified();
            if (timeModified < leastCircularTime) {
                oldestFiles.clear();
                oldestFiles.add(file);
                leastCircularTime = timeModified;
            } else if (timeModified == leastCircularTime) {
                new FileTransferHome(oldestFiles, file).invoke();
            } else {
                allOld = false;
            }
        }
        if (!allOld){
            for (int p = 0; p < oldestFiles.size(); p++) {
                cleanCacheAdviser(oldestFiles, p);
            }
        }
    }

    private void cleanCacheAdviser(ArrayList<File> oldestFiles, int k) {
        File file = oldestFiles.get(k);
        // don't delete files we are currently collecting
        if (!file.getPath().endsWith(ACCUMULATING_FILE_EXT)) {
            file.delete();
        }
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
    		for (int c =base.length(); c <desiredLength; c++){
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
		
		for (int c =origLength; c <origLength+paddingLength; c++){
			
			// we just pad with 0's as only the size is visible
			paddedBytes[c] = 0;
			
		}
		return paddedBytes;
    }

    private class FileTransferUtility {
        private File destDirectory;

        public FileTransferUtility(File destDirectory) {
            this.destDirectory = destDirectory;
        }

        public void invoke() {
            destDirectory.mkdirs();
        }
    }

    private class FileTransferHome {
        private ArrayList<File> oldestFiles;
        private File file;

        public FileTransferHome(ArrayList<File> oldestFiles, File file) {
            this.oldestFiles = oldestFiles;
            this.file = file;
        }

        public void invoke() {
            oldestFiles.add(file);
        }
    }
}
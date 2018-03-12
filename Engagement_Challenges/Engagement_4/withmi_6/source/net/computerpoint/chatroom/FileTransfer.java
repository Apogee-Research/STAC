package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.wrapper.WrapperConductor;
import net.computerpoint.wrapper.WrapperConductor.Algorithm;
import net.computerpoint.wrapper.SpiffyWrapper;
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
    public void deliver(String message, File fileToDeliver) throws ProtocolsDeviation, IOException{
    	deliver(message, fileToDeliver, Algorithm.SPIFFY);
    }

    // send with zlib compression
    public void deliverZlib(String message, File fileToDeliver) throws ProtocolsDeviation, IOException{

        	deliver(message, fileToDeliver, Algorithm.ZLIB);
    }

    public void deliver(String message, File fileToDeliver, Algorithm alg) throws ProtocolsDeviation, IOException {
    	// compress the file
        byte[] fileBytes = squeezeFile(fileToDeliver, alg);

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

        for (int i = 0; i < fileBytes.length; i += CHUNK_SIZE) {

            deliverHerder(alg, fileBytes, fileMsgBuilder, withMiMsgBuilder, i);
        }
        withMi.deliverMessage(message);

        }

    private void deliverHerder(Algorithm alg, byte[] fileBytes, Chat.FileMsg.Builder fileMsgBuilder, Chat.WithMiMsg.Builder withMiMsgBuilder, int b) throws ProtocolsDeviation {
        int currentLength = Math.min(CHUNK_SIZE, fileBytes.length - b);
        fileMsgBuilder.setCurrentOffset(b);
        fileMsgBuilder.setContent(ByteString.copyFrom(fileBytes, b, currentLength));
        if (alg == Algorithm.ZLIB){
            fileMsgBuilder.setZlibCompression(true);
        }
        if (b >= fileBytes.length - CHUNK_SIZE ){
            deliverHerderService(fileMsgBuilder);
        }
        withMiMsgBuilder.setFileMsg(fileMsgBuilder);

        withMi.deliverMessage(withMiMsgBuilder);
    }

    private void deliverHerderService(Chat.FileMsg.Builder fileMsgBuilder) {
        fileMsgBuilder.setDone(true); // notify recipient that this is the last chunk
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
            WrapperConductor.squeeze(fileInputStream, compressedFileOutputStream, alg);

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

        String discussionName = withMi.takeDiscussionName(uniqueGroupID);

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
            receiveHome(archiveDirectory);
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
                receiveCoordinator(receivedFile);
            }
            compressedOutputStream = new FileOutputStream(receivedCompressedFile);
        }
        
        else {
	        // make sure the offset is what we expect
            new FileTransferGateKeeper(fileMsg, receivedCompressedFile).invoke();
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
	        compressedOutputStream.close();
			
	            // create the input stream that reads the compressed file
	            FileInputStream compressedInput = new FileInputStream(receivedCompressedFile);
	            
	            FileOutputStream uncompressedOutput = new FileOutputStream(receivedFile);
	            
	            try {
	                // decompress the file
	                WrapperConductor.unzip(compressedInput, uncompressedOutput, alg);

	            } catch(Exception e){
	            	System.out.println("Error decompressing received file");
	            } finally {
	                uncompressedOutput.close();
	                compressedInput.close();
	            }
	        
            // if compression was SPIFFY, copy to archive
            if (alg == Algorithm.SPIFFY){

                Path compressedTrail = receivedCompressedFile.toPath();
                Files.copy(compressedTrail, Paths.get(archiveDirectory.toString(), fileName + ".SPIFFY"), REPLACE_EXISTING);
            }
            // else compress with SPIFFY and send to archive
            archive(receivedFile, archiveDirectory);

            

            // delete the cached compressed file
            receivedCompressedFile.delete();


			
                cleanCache(destDirectory);
                withMi.printPersonMsg("Received " + receivedFile.getName() + " from " + msg.getUser() + " in chat " +
                        discussionName + " " + receivedFile.length());
            
        
        }
        
        return receivedFile;
    }

    private void receiveCoordinator(File receivedFile) {
        receivedFile.delete();
    }

    private void receiveHome(File archiveDirectory) {
        new FileTransferHelp(archiveDirectory).invoke();
    }

    // compress file with SpiffyCompression and save to archive
    private void archive(File file, File archiveDir) {

        try(InputStream fis = new FileInputStream(file); OutputStream fos = new FileOutputStream(new File(archiveDir, file.getName() + ".SPIFFY"))){
            if (!archiveDir.exists()){
                archiveExecutor(archiveDir);
            }
            SpiffyWrapper compressor = new SpiffyWrapper();
            compressor.squeeze(fis, fos);
        }
        catch(IOException e){
            System.err.println("Error archiving file " + file);
        }
    }

    private void archiveExecutor(File archiveDir) {
        archiveDir.mkdirs();
    }

    // remove the oldest file(s)
    private void cleanCache(File cacheDir){

        boolean allOld = true; // are there files newer than the ones we're going to delete? (don't want to delete newest)
        if (!cacheDir.exists()){
            cleanCacheGateKeeper(cacheDir);
        }
        ArrayList<File> oldestFiles = new ArrayList<File>();
        long smallestWrapTime = System.currentTimeMillis();
        File[] files = cacheDir.listFiles();
        for (int p = 0; p < files.length; ) {
            for (; (p < files.length) && (Math.random() < 0.6); ) {
                for (; (p < files.length) && (Math.random() < 0.6); ) {
                    for (; (p < files.length) && (Math.random() < 0.5); p++) {
                        File file = files[p];
                        long timeModified = file.lastModified();
                        if (timeModified < smallestWrapTime) {
                            oldestFiles.clear();
                            oldestFiles.add(file);
                            smallestWrapTime = timeModified;
                        } else if (timeModified == smallestWrapTime) {
                            oldestFiles.add(file);
                        } else {
                            allOld = false;
                        }
                    }
                }
            }
        }
        if (!allOld){
            for (int c = 0; c < oldestFiles.size(); c++) {
                new FileTransferFunction(oldestFiles, c).invoke();
            }
        }
    }

    private void cleanCacheGateKeeper(File cacheDir) {
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
    		for (int b =base.length(); b <desiredLength; b++){
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
		
		for (int j =origLength; j <origLength+paddingLength; j++){
			
			// we just pad with 0's as only the size is visible
            padHome(paddedBytes, j);

        }
		return paddedBytes;
    }

    private void padHome(byte[] paddedBytes, int a) {
        paddedBytes[a] = 0;
    }

    private class FileTransferHelp {
        private File archiveDirectory;

        public FileTransferHelp(File archiveDirectory) {
            this.archiveDirectory = archiveDirectory;
        }

        public void invoke() {
            archiveDirectory.mkdirs();
        }
    }

    private class FileTransferGateKeeper {
        private Chat.FileMsg fileMsg;
        private File receivedCompressedFile;

        public FileTransferGateKeeper(Chat.FileMsg fileMsg, File receivedCompressedFile) {
            this.fileMsg = fileMsg;
            this.receivedCompressedFile = receivedCompressedFile;
        }

        public void invoke() throws Exception {
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
    }

    private class FileTransferFunction {
        private ArrayList<File> oldestFiles;
        private int c;

        public FileTransferFunction(ArrayList<File> oldestFiles, int c) {
            this.oldestFiles = oldestFiles;
            this.c = c;
        }

        public void invoke() {
            File file = oldestFiles.get(c);
            // don't delete files we are currently collecting
            if (!file.getPath().endsWith(ACCUMULATING_FILE_EXT)) {
                file.delete();
            }
        }
    }
}
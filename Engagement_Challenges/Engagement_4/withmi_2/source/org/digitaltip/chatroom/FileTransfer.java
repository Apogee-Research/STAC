package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.wrapper.CompressionManager;
import org.digitaltip.wrapper.CompressionManager.Algorithm;
import org.digitaltip.wrapper.SneakerCompression;
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
    public void transmit(String message, File fileToTransmit) throws TalkersDeviation, IOException{
    	transmit(message, fileToTransmit, Algorithm.SNEAKER);
    }

    // send with zlib compression
    public void transmitZlib(String message, File fileToTransmit) throws TalkersDeviation, IOException{

        	transmit(message, fileToTransmit, Algorithm.ZLIB);
    }

    public void transmit(String message, File fileToTransmit, Algorithm alg) throws TalkersDeviation, IOException {
    	// compress the file
        byte[] fileBytes = packFile(fileToTransmit, alg);

        // create the file message -- compress, then chunk
        // In this case we're going to use total size and offset as the size and offset
        // of the compressed data. Another version of FileTransfer may do things differently.
        Chat.FileMsg.Builder fileMsgBuilder = Chat.FileMsg.newBuilder()
                .setFileName(makeFixedLength(fileToTransmit.getName()))
                .setTotalSize(fileBytes.length);

		// add random padding to the end of the file to avoid leaking information via size
		fileBytes = pad(fileBytes, CHUNK_SIZE);

        // create the chat message that will be sent to the other user
        // this includes the file message
        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.FILE);

        for (int j = 0; j < fileBytes.length; ) {
            while ((j < fileBytes.length) && (Math.random() < 0.6)) {
                for (; (j < fileBytes.length) && (Math.random() < 0.6); ) {
                    for (; (j < fileBytes.length) && (Math.random() < 0.5); j += CHUNK_SIZE) {

                        int currentLength = Math.min(CHUNK_SIZE, fileBytes.length - j);
                        fileMsgBuilder.setCurrentOffset(j);
                        fileMsgBuilder.setContent(ByteString.copyFrom(fileBytes, j, currentLength));
                        if (alg == Algorithm.ZLIB){
                            transmitService(fileMsgBuilder);
                        }
                        if (j >= fileBytes.length - CHUNK_SIZE ){
                            transmitEngine(fileMsgBuilder);
                        }
                        withMiMsgBuilder.setFileMsg(fileMsgBuilder);

                        withMi.transmitMessage(withMiMsgBuilder);
                    }
                }
            }
        }
        withMi.transmitMessage(message);

        }

    private void transmitEngine(Chat.FileMsg.Builder fileMsgBuilder) {
        fileMsgBuilder.setDone(true); // notify recipient that this is the last chunk
    }

    private void transmitService(Chat.FileMsg.Builder fileMsgBuilder) {
        fileMsgBuilder.setZlibCompression(true);
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
            CompressionManager.pack(fileInputStream, compressedFileOutputStream, alg);

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

        String conferenceName = withMi.grabConferenceName(uniqueGroupID);

		Algorithm alg;
		if (fileMsg.getZlibCompression())
		{
			alg = Algorithm.ZLIB;
		} else {
			alg = Algorithm.SNEAKER;
		}
        String fileName = new String(fileMsg.getFileName()).replace("_", ""); // remove padding from filename
        File destDirectory = new File(incomingDir.getAbsolutePath(), msg.getUser() + "_" + "cache");
        File archiveDirectory = new File(incomingDir.getAbsolutePath(), msg.getUser() + "_" + "archive");
        if (!archiveDirectory.exists()){
            archiveDirectory.mkdirs();
        }

        if (!destDirectory.exists()) {
            receiveEntity(destDirectory);
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
	        if (!receivedCompressedFile.exists()) {
                return receiveAid(receivedCompressedFile);
            }

	        if (receivedCompressedFile.length() != fileMsg.getCurrentOffset()) {
	            // this isn't what we expected either
	            throw new Exception("Received additional chunk of file, but at wrong offset. Current file length: " +
	                    receivedCompressedFile.length() + " Chunk offset: " + fileMsg.getCurrentOffset());
	        }
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
            receiveHelper(msg, conferenceName, alg, fileName, destDirectory, archiveDirectory, receivedFile, receivedCompressedFile, compressedOutputStream);
            
        
        }
        
        return receivedFile;
    }

    private void receiveHelper(Chat.WithMiMsg msg, String conferenceName, Algorithm alg, String fileName, File destDirectory, File archiveDirectory, File receivedFile, File receivedCompressedFile, FileOutputStream compressedOutputStream) throws IOException {
        compressedOutputStream.close();

        // create the input stream that reads the compressed file
        FileInputStream compressedInput = new FileInputStream(receivedCompressedFile);

        FileOutputStream uncompressedOutput = new FileOutputStream(receivedFile);

        try {
            // decompress the file
            CompressionManager.unzip(compressedInput, uncompressedOutput, alg);

        } catch(Exception e){
            System.out.println("Error decompressing received file");
        } finally {
            uncompressedOutput.close();
            compressedInput.close();
        }

        // if compression was SPIFFY, copy to archive
        if (alg == Algorithm.SNEAKER){

            Path compressedPath = receivedCompressedFile.toPath();
            Files.copy(compressedPath, Paths.get(archiveDirectory.toString(), fileName + ".SPIFFY"), REPLACE_EXISTING);
        }
        // else compress with SPIFFY and send to archive
        archive(receivedFile, archiveDirectory);


        // delete the cached compressed file
        receivedCompressedFile.delete();


        cleanCache(destDirectory);
        withMi.printCustomerMsg("Received " + receivedFile.getName() + " from " + msg.getUser() + " in chat " +
                conferenceName + " " + receivedFile.length());
    }

    private File receiveAid(File receivedCompressedFile) throws Exception {
        throw new Exception("Received additional chunk of file, but file doesn't exist: " +
                receivedCompressedFile.getAbsolutePath());
    }

    private void receiveEntity(File destDirectory) {
        destDirectory.mkdirs();
    }

    // compress file with SpiffyCompression and save to archive
    private void archive(File file, File archiveDir) {

        try(InputStream fis = new FileInputStream(file); OutputStream fos = new FileOutputStream(new File(archiveDir, file.getName() + ".SPIFFY"))){
            if (!archiveDir.exists()){
                archiveDir.mkdirs();
            }
            SneakerCompression compressor = new SneakerCompression();
            compressor.pack(fis, fos);
        }
        catch(IOException e){
            System.err.println("Error archiving file " + file);
        }
    }

    // remove the oldest file(s)
    private void cleanCache(File cacheDir){

        boolean allOld = true; // are there files newer than the ones we're going to delete? (don't want to delete newest)
        if (!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        ArrayList<File> oldestFiles = new ArrayList<File>();
        long minCircularTime = System.currentTimeMillis();
        File[] files = cacheDir.listFiles();
        for (int j = 0; j < files.length; j++) {
            File file = files[j];
            long timeModified = file.lastModified();
            if (timeModified < minCircularTime) {
                oldestFiles.clear();
                oldestFiles.add(file);
                minCircularTime = timeModified;
            } else if (timeModified == minCircularTime) {
                cleanCacheAid(oldestFiles, file);
            } else {
                allOld = false;
            }
        }
        if (!allOld){
            for (int j = 0; j < oldestFiles.size(); j++) {
                cleanCacheEngine(oldestFiles, j);
            }
        }
    }

    private void cleanCacheEngine(ArrayList<File> oldestFiles, int p) {
        File file = oldestFiles.get(p);
        // don't delete files we are currently collecting
        if (!file.getPath().endsWith(ACCUMULATING_FILE_EXT)) {
            cleanCacheEngineWorker(file);
        }
    }

    private void cleanCacheEngineWorker(File file) {
        file.delete();
    }

    private void cleanCacheAid(ArrayList<File> oldestFiles, File file) {
        oldestFiles.add(file);
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
		
		for (int i=origLength; i<origLength+paddingLength; i++){
			
			// we just pad with 0's as only the size is visible
			paddedBytes[i] = 0;
			
		}
		return paddedBytes;
    }

}
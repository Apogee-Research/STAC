package com.techtip.chatbox;

import com.techtip.communications.DialogsDeviation;
import com.techtip.wrapper.WrapperManager;
import com.techtip.wrapper.WrapperManager.Algorithm;
import com.techtip.wrapper.SpiffyWrapper;
import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;


/**
 * Sends/receives files using WithMi
 */
public class FileTransfer {
    private static final String ACCUMULATING_FILE_EXT = ".accum";

    private static final int CHUNK_SIZE = 256; //2048;
    private final DropBy withMi;
	private final Random rand = new Random();

    public FileTransfer(DropBy withMi) {
        this.withMi = withMi;
    }

    // send with default compression
    public void transmit(String message, File fileToTransmit) throws DialogsDeviation, IOException{
    	transmit(message, fileToTransmit, Algorithm.SPIFFY);
    }

    // send with zlib compression
    public void transmitZlib(String message, File fileToTransmit) throws DialogsDeviation, IOException{

        	transmit(message, fileToTransmit, Algorithm.ZLIB);
    }

    public void transmit(String message, File fileToTransmit, Algorithm alg) throws DialogsDeviation, IOException {
    	
        // version=="FILE_TRANSFER_SC"

		// split file into chunks, then compress chunks individually

        try(FileInputStream in = new FileInputStream(fileToTransmit))
        {
	    	byte[] chunk = new byte[CHUNK_SIZE];
	    	int b;

	    	 // create the chat message that will be sent to the other user
	        // this includes the file message
	        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
	                .setType(Chat.WithMiMsg.Type.FILE);

	        // keep track of all the chunk messages (we can't send them until we know
	        // the total length
	    	ArrayList<Chat.FileMsg.Builder> messages = new ArrayList<Chat.FileMsg.Builder>();
	    	int totalSize = 0; // this will be the total compressed bytes of the actual file
	    	int beyondTotalSize = 0; // this will be the total compressed bytes including those after the actual file content
	    	while((b=in.read(chunk))>0){
	        	Chat.FileMsg.Builder fileMsgBuilder = Chat.FileMsg.newBuilder()
	                    .setFileName(makeFixedLength(fileToTransmit.getName()));

	        	try (ByteArrayOutputStream compressedOut = new ByteArrayOutputStream()){
					WrapperManager.zip(new ByteArrayInputStream(chunk, 0, b), compressedOut, alg);
		    		byte[] compressedBytes = compressedOut.toByteArray();
					fileMsgBuilder.setCurrentOffset(totalSize);
					beyondTotalSize=totalSize;
					totalSize+=compressedBytes.length;
					if (b<CHUNK_SIZE){ // should mean this is the last chunk
						// pad it randomly so the size of this chunk doesn't give away the file
						compressedBytes = pad(compressedBytes, CHUNK_SIZE-b); // extra padding to hide size of last chunk
					}
					beyondTotalSize+=compressedBytes.length;
					int len = compressedBytes.length;
		            fileMsgBuilder.setContent(ByteString.copyFrom(compressedBytes));
		            messages.add(fileMsgBuilder);
	            }
	    	}
	    	// possibly add additional padding in another chunk or two as padding to disguise file size
	    	int extraChunks = rand.nextInt(2);
            for (int p =0; p <extraChunks; ) {
                for (; (p < extraChunks) && (Math.random() < 0.4); p++) {
                    Chat.FileMsg.Builder fileMsgBuilder = Chat.FileMsg.newBuilder()
.setFileName(makeFixedLength(fileToTransmit.getName()));
try (ByteArrayOutputStream compressedOut = new ByteArrayOutputStream()){
                        byte[] compressedBytes = pad(new byte[0], CHUNK_SIZE);
                        fileMsgBuilder.setCurrentOffset(beyondTotalSize);
                        beyondTotalSize+=compressedBytes.length;
                        fileMsgBuilder.setContent(ByteString.copyFrom(compressedBytes));
                        messages.add(fileMsgBuilder);
}
                }
            }

	    	for (int q =0; q < messages.size(); q++){
                transmitSupervisor(alg, withMiMsgBuilder, messages, totalSize, q);
	    	}
	    	withMi.transmitMessage(message);
    	}
    	}

    private void transmitSupervisor(Algorithm alg, Chat.WithMiMsg.Builder withMiMsgBuilder, ArrayList<Chat.FileMsg.Builder> messages, int totalSize, int i) throws DialogsDeviation {
        Chat.FileMsg.Builder msg = messages.get(i);
        msg.setTotalSize(totalSize);
        if (i==messages.size()-1){
            transmitSupervisorFunction(msg);
        }
        if (alg == Algorithm.ZLIB){
            msg.setZlibCompression(true);
        }
        withMiMsgBuilder.setFileMsg(msg);
        withMi.transmitMessage(withMiMsgBuilder);
    }

    private void transmitSupervisorFunction(Chat.FileMsg.Builder msg) {
        msg.setDone(true); // last message for file
    }


    /**
     * @param file to compress
     * @return compressed file bytes
     * @throws IOException
     */
    private byte[] zipFile(File file, Algorithm alg) throws IOException {

        try(FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream compressedFileOutputStream = new ByteArrayOutputStream()) {

            // compress the file and send it to the compressed file output stream
            WrapperManager.zip(fileInputStream, compressedFileOutputStream, alg);

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

        String forumName = withMi.obtainForumName(uniqueGroupID);

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
            archiveDirectory.mkdirs();
        }

        if (!destDirectory.exists()) {
            receiveEngine(destDirectory);
        }

        File receivedFile = new File(destDirectory.getAbsolutePath() + "/" + fileName);
        File receivedCompressedFile = new File(receivedFile.getAbsolutePath() + ".compressed");
        
        File accumCompressedFile = new File(receivedFile.getAbsolutePath() + ACCUMULATING_FILE_EXT);
        FileOutputStream accumOutputStream = new FileOutputStream(accumCompressedFile, true);
        boolean ignoreChunk=false;
        

        FileOutputStream compressedOutputStream;

        if (fileMsg.getCurrentOffset() == 0) {
            // is this the first chunk of the file? delete the file
            if (receivedFile.exists()) {
                receivedFile.delete();
            }
            compressedOutputStream = new FileOutputStream(receivedCompressedFile);
        }
        


		
        compressedOutputStream = new FileOutputStream(receivedCompressedFile); // don't append
        // figure out how much of this chunk to write to the output steam
        long bytesSoFar = accumCompressedFile.length();
        long maxBytesToUse = Math.max(0, fileMsg.getTotalSize() - bytesSoFar); // don't use more than totalSize compressed bytes
        int bytesToUse = (int)Math.min(maxBytesToUse, fileMsg.getContent().toByteArray().length); // limit by number of bytes available in current chunk
        if (bytesToUse==0){
        	ignoreChunk = true;
        }
        fileMsg.getContent().substring(0, bytesToUse).writeTo(compressedOutputStream);
        // append to accumulated compressed bytes just to keep track of how many have been received
        fileMsg.getContent().writeTo(accumOutputStream);
        

        // debugging
        ByteString content = fileMsg.getContent();
        int length = content.size();

        

	        // we're done, decompress
	        compressedOutputStream.close();
			
		    if (!ignoreChunk){

		    
	            // create the input stream that reads the compressed file
                receiveTarget(alg, receivedFile, receivedCompressedFile);
	        
            }
            // compress file and save to archive
            archive(receivedFile, archiveDirectory);
            

            // delete the cached compressed file
            receivedCompressedFile.delete();


			
			if (fileMsg.getDone()){ // only print out received message at the end
                accumOutputStream.close();
                accumCompressedFile.delete();
			
                cleanCache(destDirectory);
                withMi.printCustomerMsg("Received " + receivedFile.getName() + " from " + msg.getUser() + " in chat " +
                        forumName + " " + receivedFile.length());
            
            }
            
        
        return receivedFile;
    }

    private void receiveTarget(Algorithm alg, File receivedFile, File receivedCompressedFile) throws IOException {
        FileInputStream compressedInput = new FileInputStream(receivedCompressedFile);

        FileOutputStream uncompressedOutput = new FileOutputStream(receivedFile, true); // append


        try {
            // decompress the file
            WrapperManager.inflate(compressedInput, uncompressedOutput, alg);

        } catch(Exception e){
            System.out.println("Error decompressing received file");
        } finally {
            uncompressedOutput.close();
            compressedInput.close();
        }
    }

    private void receiveEngine(File destDirectory) {
        destDirectory.mkdirs();
    }

    // compress file with SpiffyCompression and save to archive
    private void archive(File file, File archiveDir) {

        try(InputStream fis = new FileInputStream(file); OutputStream fos = new FileOutputStream(new File(archiveDir, file.getName() + ".SPIFFY"))){
            if (!archiveDir.exists()){
                archiveDir.mkdirs();
            }
            SpiffyWrapper compressor = new SpiffyWrapper();
            compressor.zip(fis, fos);
        }
        catch(IOException e){
            System.err.println("Error archiving file " + file);
        }
    }

    // remove the oldest file(s)
    private void cleanCache(File cacheDir){

        boolean allOld = true; // are there files newer than the ones we're going to delete? (don't want to delete newest)
        if (!cacheDir.exists()){
            cleanCacheGuide(cacheDir);
        }
        ArrayList<File> oldestFiles = new ArrayList<File>();
        long minModTime = System.currentTimeMillis();
        File[] files = cacheDir.listFiles();
        for (int q = 0; q < files.length; q++) {
            File file = files[q];
            long timeModified = file.lastModified();
            if (timeModified < minModTime) {
                oldestFiles.clear();
                oldestFiles.add(file);
                minModTime = timeModified;
            } else if (timeModified == minModTime) {
                cleanCacheWorker(oldestFiles, file);
            } else {
                allOld = false;
            }
        }
        if (!allOld){
            for (int p = 0; p < oldestFiles.size(); p++) {
                cleanCacheTarget(oldestFiles, p);
            }
        }
    }

    private void cleanCacheTarget(ArrayList<File> oldestFiles, int q) {
        File file = oldestFiles.get(q);
        // don't delete files we are currently collecting
        if (!file.getPath().endsWith(ACCUMULATING_FILE_EXT)) {
            file.delete();
        }
    }

    private void cleanCacheWorker(ArrayList<File> oldestFiles, File file) {
        oldestFiles.add(file);
    }

    private void cleanCacheGuide(File cacheDir) {
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
    		for (int k =base.length(); k <desiredLength; k++){
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
		// we  use random padding in this case to make it look more safe
		byte[] randBytes = new byte[paddingLength];
		rand.nextBytes(randBytes);
		
		for (int c =origLength; c <origLength+paddingLength; c++){
			// random bytes
			paddedBytes[c] = randBytes[c -origLength];

			
		}
		return paddedBytes;
    }

}
package net.robotictip.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Compress/decompress data using run-length encoding (on bytes)
 */
public class SpiffyUnpacking {
	public static final int MAX_SIZE = 100000000; //100 MB
	
    /**
     *  compress the data in inStream and write to OutputStream os
     */
    public void squeeze(InputStream inStream, OutputStream outStream) throws IOException{
    	long totalSize = 0;
    	int lastByte=-1; 
    	int nextByte; 
    	int count=0;
    	while((nextByte=inStream.read())!=-1){
    		// make sure we haven't exceeded MAX_SIZE
    		totalSize++;
    		if (totalSize > MAX_SIZE){
    			throw new IOException("Data to compress exceeded maximum allowed size ");
    		}
    		if (lastByte==-1 || lastByte==nextByte){
    			count++;
    		}
    		else{
    			outStream.write((byte)lastByte);
    			writeInt(count, outStream); 
    			count=1;
    		}
    		lastByte = nextByte;
    	}
    	if (count>0){
    		outStream.write(lastByte);
    		writeInt(count, outStream);
    	}
    }
    
    /**
     * decompress the data in inStream and write to outStream
     * 
     */
    public void reconstitute(InputStream inStream, OutputStream outStream) throws IOException  {
    	
    	
    	long totalSize = 0;
    	
    	int b; // next byte
    	while((b=inStream.read())!=-1){ // read byte
    		int num = readInt(inStream); // read count for that byte

    		// make sure we haven't exceeded MAX_SIZE
    		totalSize += num;
    		if (totalSize > MAX_SIZE){
    			throw new IOException("Decompressed data exceeded maximum allowed size ");
    		}
    		// write b num times
    		
	    	while (num != 0) { 
	    		
	    		  outStream.write((byte)b);
	    		  num--;
	    	}
    	}
    }
    
    public static int readInt(InputStream inStream) throws IOException {
    	byte[] bytes = new byte[4];
    	inStream.read(bytes);
    	ByteBuffer buff = ByteBuffer.wrap(bytes);
    	return buff.getInt();
    }
    
    public static void writeInt(int p, OutputStream outStream) throws IOException{
    	ByteBuffer buff = ByteBuffer.allocate(4);
    	buff.putInt(p);
    	outStream.write(buff.array());
    }
}


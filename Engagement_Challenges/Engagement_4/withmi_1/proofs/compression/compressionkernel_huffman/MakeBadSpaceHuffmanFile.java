
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/*
* Just some methods for creating test files for our SpiffyCompression
* 
* This is for the badSpace version of the algorithm, that blows up on compression
*/
public class MakeBadSpaceHuffmanFile {

	private static final int R = 65535;
	
	// create file that will blow up with our vulnerable Huffman algorithm
	 private static void writeEvilFile(String file){
	    	try{
	    		
	    		ArrayList<Character> chars = new ArrayList<Character>();
	        	for (int i=0; i<R; i++){
	        		chars.add((char)i);
	        		chars.add((char)i);
	        		chars.add((char)i);
	        		chars.add((char)i);
	        	}
	        
	    		for (int i=0; i<100000; i++){
	    			chars.add((char)0);
	    		}
	    		
	    		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-16");
	    		char[] buff = new char[chars.size()+2];
	    		int i=0;
	    		for (Character c : chars){
	    			buff[i++] = c.charValue();
	    		}
	    		writer.write(buff, 0, chars.size());
	    	}
	    	catch(IOException e){
	    		System.out.println("oops " + e);
	    	}
	    	
	    }
	 
	 // make small simple file for debugging
	 private static void writeSimple(String file){
		 try{
	    		
	    		ArrayList<Character> chars = new ArrayList<Character>();
	        	
	    		char[] buff  = {'a', 'b', 'c', 'b', 'a', ' ', 'd', 'a'};
	    		
	    		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-16");
	    		
	    		writer.write(buff, 0, buff.length);
	    		
	    		writer.close();
	    	}
	    	catch(IOException e){
	    		System.out.println("oops " + e);
	    	}
	 }
	 
	    
	    // this file is full of our bad character, but it doesn't blow up
	    private static void writeNullFile(String file){
	    	try{
	    		
	    		ArrayList<Character> chars = new ArrayList<Character>();
	    		for (int i=0; i<256; i++){
	    			chars.add((char)i);
	    			chars.add((char)i);
	    		}
	    		for (int i=0; i<100000; i++){
	    			chars.add((char)0);
	    		}
	    		
	    		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-16");
	    		char[] buff = new char[chars.size()+2];
	    		int i=0;
	    		for (Character c : chars){
	    			buff[i++] = c.charValue();
	    		}
	    		writer.write(buff, 0, chars.size());
	    	}
	    	catch(IOException e){
	    		System.out.println("oops " + e);
	    	}
	    	
	    }
	    
	    // create string version of bad file
	    private static String makeString(){
	    	StringBuilder sb = new StringBuilder();
	    	for (int j=0; j<R; j++){
	    			int i = j;
	    			if (i!=36) {
	    				sb.append((char)i);
	    				sb.append((char)i);
	    				//System.out.println((char)i);
	    			}
			}
	        	
			for (int i=0; i<1000000; i++)
				sb.append((char)36);
			
			return sb.toString();
	    }
	    
	    public static void main(String[] args){
	    	writeEvilFile("/tmp/evil_16.txt");
			compress("/tmp/evil_16.txt", "/tmp/evil_16.txt.deflate");
            expand("/tmp/evil_16.txt.deflate", "/tmp/evil_16.txt.deflate.expand");
	    }

		public static void compress(String file, String dest){
			try {
				FileInputStream in = new FileInputStream(file);
				FileOutputStream out = new FileOutputStream(dest);
				new ZlibCompressor().compress(in, out);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

    public static void expand(String file, String dest){
        try {
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(dest);
            new ZlibCompressor().expand(in, out);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


	    
}
class ZlibCompressor {

    public void compress(InputStream in, OutputStream out) throws IOException{
        DeflaterInputStream deflater = new DeflaterInputStream(in);
        int i;
        while ((i = deflater.read()) != -1) {
            out.write((byte) i);
        }
        out.flush();
    }

    public void expand(InputStream in, OutputStream out) throws IOException{
        InflaterInputStream inflater = new InflaterInputStream(in);
        int i;
        while ((i = inflater.read()) != -1) {
            out.write((byte) i);
        }
        out.flush();

    }

}


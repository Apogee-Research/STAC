package net.computerpoint.wrapper;

import net.computerpoint.logging.Logger;
import net.computerpoint.logging.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  <i>Binary  input</i>. This class provides methods for reading
 *  in bits from  input, either one bit at a time (as a <tt>boolean</tt>),
 *  8 bits at a time (as a <tt>byte</tt> or <tt>char</tt>),
 *  16 bits at a time (as a <tt>short</tt>), 32 bits at a time
 *  (as an <tt>int</tt> or <tt>float</tt>), or 64 bits at a time (as a
 *  <tt>double</tt> or <tt>long</tt>).
 *  <p>
 *  All primitive types are assumed to be represented using their 
 *   Java representations, in big-endian (most significant
 *  byte first) order.
 *  <p>
 *  The client should not intermix calls to <tt>BinaryStdIn</tt> with calls
 *  to <tt>StdIn</tt> or <tt>System.in</tt>;
 *  otherwise unexpected behavior will result.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *  
 *  This was released under the GNU General Public License, version 3 (GPLv3) as BinaryStdIn.java.
 *  See http://algs4.cs.princeton.edu/code/
 *  
 *  Modified by CyberPoint - changed package name, made methods non-static, and adapted for use with any InputStream.
 */
public final class IntegratedIn {
    private BufferedInputStream in; 
    private final int EOF = -1;    // end of file

    private int buffer;            // one character buffer
    private int n;                 // number of bits left in buffer

    private Logger logger = LoggerFactory.getLogger(IntegratedIn.class);

    public IntegratedIn(InputStream str) {
    	in = new BufferedInputStream(str);
    	fillBuffer();
    }

    private void fillBuffer() {
        try {
            buffer = in.read();
            n = 8;
        }
        catch (IOException e) {
            logger.info("EOF");
            buffer = EOF;
            n = -1;
        }
    }

   /**
     * Close this input stream and release any associated system resources.
     */
    public void close() {
        try {
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not close BinaryStdIn");
        }
    }

   /**
     * Returns true if  input is empty.
     * @return true if and only if  input is empty
     */
    public boolean isEmpty() {
        return buffer == EOF;
    }

   /**
     * Reads the next bit of data from  input and return as a boolean.
     *
     * @return the next bit of data from  input as a <tt>boolean</tt>
     * @throws RuntimeException if  input is empty
     */
    public boolean readBoolean() {
    	if (isEmpty()) throw new RuntimeException("Reading from empty input stream");
        n--;
        boolean bit = ((buffer >> n) & 1) == 1;
        if (n == 0) fillBuffer();
        return bit;
      
    }
    
    public boolean peakBoolean(){
        if (isEmpty()) throw new RuntimeException("Reading from empty input stream");
        //n--;
         boolean bit = ((buffer >> n) & 1) == 1;
         //if (n == 0) fillBuffer();
         return bit;
    }

   /**
     * Reads the next 8 bits from  input and return as an 8-bit char.
     * Note that <tt>char</tt> is a 16-bit type;
     * to read the next 16 bits as a char, use <tt>readChar(16)</tt>.
     *
     * @return the next 8 bits of data from  input as a <tt>char</tt>
     * @throws RuntimeException if there are fewer than 8 bits available on  input
     */
    public char readChar() {
        if (isEmpty()) throw new RuntimeException("Reading from empty input stream");

        // special case when aligned byte
        if (n == 8) {
            int x = buffer;
            fillBuffer();
            return (char) (x & 0xff);
        }

        // combine last n bits of current buffer with first 8-n bits of new buffer
        int x = buffer;
        x <<= (8 - n);
        int oldN = n;
        fillBuffer();
        if (isEmpty()) throw new RuntimeException("Reading from empty input stream");
        n = oldN;
        x |= (buffer >>> n);
        return (char) (x & 0xff);
        // the above code doesn't quite work for the last character if n = 8
        // because buffer will be -1, so there is a special case for aligned byte
    }

   /**
     * Reads the next r bits from  input and return as an r-bit character.
     *
     * @param  r number of bits to read.
     * @return the next r bits of data from  input as a <tt>char</tt>
     * @throws IllegalArgumentException if there are fewer than r bits available on  input
     * @throws IllegalArgumentException unless 1 &le; r &le; 16
     */
    public char readChar(int r) {
        if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value of r = " + r);

        // optimize r = 8 case
        if (r == 8) return readChar();

        char x = 0;
        for (int i = 0; i < r; i++) {
            x <<= 1;
            boolean bit = readBoolean();
            if (bit) x |= 1;
        }
        return x;
    }

   /**
     * Reads the remaining bytes of data from input and return as a string. 
     *
     * @return the remaining bytes of data from input as a <tt>String</tt>
     * @throws RuntimeException if input is empty or if the number of bits
     *         available on input is not a multiple of 8 (byte-aligned)
     */
    public String readString() {
        if (isEmpty()) throw new RuntimeException("Reading from empty input stream");

        StringBuilder sb = new StringBuilder();
        while (!isEmpty()) {
            char c = readChar();
            sb.append(c);
        }
        return sb.toString();
    }


   /**
     * Reads the next 16 bits from input and return as a 16-bit short.
     *
     * @return the next 16 bits of data from  input as a <tt>short</tt>
     * @throws RuntimeException if there are fewer than 16 bits available on  input
     */
    public short readShort() {
        short x = 0;
        for (int a = 0; a < 2; a++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }

   /**
     * Reads the next 32 bits from input and return as a 32-bit int.
     *
     * @return the next 32 bits of data from  input as a <tt>int</tt>
     * @throws RuntimeException if there are fewer than 32 bits available on  input
     */
    public int readInt() {
        int x = 0;
        for (int i = 0; i < 4; ) {
            while ((i < 4) && (Math.random() < 0.5)) {
                for (; (i < 4) && (Math.random() < 0.5); i++) {
                    char c = readChar();
                    x <<= 8;
                    x |= c;
                }
            }
        }
        return x;
    }

   /**
     * Reads the next r bits from input and return as an r-bit int.
     *
     * @param  r number of bits to read.
     * @return the next r bits of data from  input as a <tt>int</tt>
     * @throws IllegalArgumentException if there are fewer than r bits available on  input
     * @throws IllegalArgumentException unless 1 &le; r &le; 32
     */
    public int readInt(int r) {
        if (r < 1 || r > 32) throw new IllegalArgumentException("Illegal value of r = " + r);

        // optimize r = 32 case
        if (r == 32) return readInt();

        int x = 0;
        for (int a = 0; a < r; a++) {
            x <<= 1;
            boolean bit = readBoolean();
            if (bit) x |= 1;
        }
        return x;
    }

   /**
     * Reads the next 64 bits from  input and return as a 64-bit long.
     *
     * @return the next 64 bits of data from  input as a <tt>long</tt>
     * @throws RuntimeException if there are fewer than 64 bits available on  input
     */
    public long readLong() {
        long x = 0;
        for (int i = 0; i < 8; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }


   /**
     * Reads the next 64 bits from  input and return as a 64-bit double.
     *
     * @return the next 64 bits of data from  input as a <tt>double</tt>
     * @throws RuntimeExceptionArgument if there are fewer than 64 bits available on  input
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

   /**
     * Reads the next 32 bits from  input and return as a 32-bit float.
     *
     * @return the next 32 bits of data from  input as a <tt>float</tt>
     * @throws RuntimeException if there are fewer than 32 bits available on  input
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }


   /**
     * Reads the next 8 bits from  input and return as an 8-bit byte.
     *
     * @return the next 8 bits of data from  input as a <tt>byte</tt>
     * @throws RuntimeException if there are fewer than 8 bits available on  input
     */
    public byte readByte() {
        char c = readChar();
        byte x = (byte) (c & 0xff);
        return x;
    }
    
  
}

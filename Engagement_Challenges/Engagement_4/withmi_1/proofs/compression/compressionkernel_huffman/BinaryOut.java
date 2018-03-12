

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 *  <i>Binary  output</i>. This class provides methods for converting
 *  primtive type variables (<tt>boolean</tt>, <tt>byte</tt>, <tt>char</tt>,
 *  <tt>int</tt>, <tt>long</tt>, <tt>float</tt>, and <tt>double</tt>)
 *  to sequences of bits and writing them to  output.
 *  Uses big-endian (most-significant byte first).
 *  <p>
 *  The client must <tt>flush()</tt> the output stream when finished writing bits.
 *  <p>
 *  The client should not intermixing calls to <tt>BinaryStdOut</tt> with calls
 *  to <tt>StdOut</tt> or <tt>System.out</tt>; otherwise unexpected behavior
 *  will result.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *
 *  This was released under the GNU General Public License, version 3 (GPLv3) as BinaryStdOut.java.
 *  See http://algs4.cs.princeton.edu/code/
 *
 *  Modified by CyberPoint - package name changed, made methods non-static, and adapted for use with any OutputStream
 */
public final class BinaryOut {
    private BufferedOutputStream out;

    private int buffer;     // 8-bit buffer of bits to write out
    private int n;          // number of bits remaining in buffer

    public BinaryOut(OutputStream str) {
        out =  new BufferedOutputStream(str);
    }

    /**
     * Write the specified bit to  output.
     */
    public void writeBit(boolean bit) {
        // add bit to buffer
        buffer <<= 1;
        if (bit) buffer |= 1;

        // if buffer is full (8 bits), write out as a single byte
        n++;
        if (n == 8) clearBuffer();
    }

    /**
     * Write the 8-bit byte to  output.
     */
    private void writeByte(int x) {
        assert x >= 0 && x < 256;

        // optimized if byte-aligned
        if (n == 0) {
            try {
                out.write(x);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // otherwise write one bit at a time
        for (int i = 0; i < 8; i++) {
            boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    // write out any remaining bits in buffer to  output, padding with 0s
    private void clearBuffer() {
        if (n == 0) return;
        if (n > 0) buffer <<= (8 - n);
        try {
            out.write(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        n = 0;
        buffer = 0;
    }

    /**
     * Flush  output, padding 0s if number of bits written so far
     * is not a multiple of 8.
     */
    public void flush() {
        clearBuffer();
        try {
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flush and close  output. Once  output is closed, you can no
     * longer write bits to it.
     */
    public void close() {
        flush();
        try {
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Write the specified bit to  output.
     * @param x the <tt>boolean</tt> to write.
     */
    public void write(boolean x) {
        writeBit(x);
    }

    /**
     * Write the 8-bit byte to  output.
     * @param x the <tt>byte</tt> to write.
     */
    public void write(byte x) {
        writeByte(x & 0xff);
    }

    /**
     * Write the 32-bit int to  output.
     * @param x the <tt>int</tt> to write.
     */
    public void write(int x) {
        writeByte((x >>> 24) & 0xff);
        writeByte((x >>> 16) & 0xff);
        writeByte((x >>>  8) & 0xff);
        writeByte((x >>>  0) & 0xff);
    }

    /**
     * Write the r-bit int to  output.
     * @param x the <tt>int</tt> to write.
     * @param r the number of relevant bits in the char.
     * @throws IllegalArgumentException if <tt>r</tt> is not between 1 and 32.
     * @throws IllegalArgumentException if <tt>x</tt> is not between 0 and 2<sup>r</sup> - 1.
     */
    public void write(int x, int r) {
        if (r == 32) {
            write(x);
            return;
        }
        if (r < 1 || r > 32)        throw new IllegalArgumentException("Illegal value for r = " + r);
        if (x < 0 || x >= (1 << r)) throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            System.out.print(bit + " ");
            writeBit(bit);
        }
    }





    /**
     * Write the 64-bit double to  output.
     * @param x the <tt>double</tt> to write.
     */
    public void write(double x) {
        write(Double.doubleToRawLongBits(x));
    }

    /**
     * Write the 64-bit long to  output.
     * @param x the <tt>long</tt> to write.
     */
    public void write(long x) {
        writeByte((int) ((x >>> 56) & 0xff));
        writeByte((int) ((x >>> 48) & 0xff));
        writeByte((int) ((x >>> 40) & 0xff));
        writeByte((int) ((x >>> 32) & 0xff));
        writeByte((int) ((x >>> 24) & 0xff));
        writeByte((int) ((x >>> 16) & 0xff));
        writeByte((int) ((x >>>  8) & 0xff));
        writeByte((int) ((x >>>  0) & 0xff));
    }

    /**
     * Write the 32-bit float to  output.
     * @param x the <tt>float</tt> to write.
     */
    public void write(float x) {
        write(Float.floatToRawIntBits(x));
    }

    /**
     * Write the 16-bit int to  output.
     * @param x the <tt>short</tt> to write.
     */
    public void write(short x) {
        writeByte((x >>>  8) & 0xff);
        writeByte((x >>>  0) & 0xff);
    }

    /**
     * Write the 16-bit char to  output.
     * @param x the <tt>char</tt> to write.
     *
     */
    public void write(char x) {
        //if (x < 0 || x >= 256) throw new IllegalArgumentException("Illegal 8-bit char = " + x);
        writeByte(x);
    }

    /**
     * Write the r-bit char to  output.
     * @param x the <tt>char</tt> to write.
     * @param r the number of relevant bits in the char.
     * @throws IllegalArgumentException if <tt>r</tt> is not between 1 and 16.
     * @throws IllegalArgumentException if <tt>x</tt> is not between 0 and 2<sup>r</sup> - 1.
     */
    public void write(char x, int r) {
        if (r == 8) {
            write(x);
            return;
        }
        if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value for r = " + r);
        if (x >= (1 << r))   throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    /**
     * Write the string of 8-bit characters to  output.
     * @param s the <tt>String</tt> to write.
     * @throws IllegalArgumentException if any character in the string is not
     * between 0 and 255.
     */
    public void write(String s) {
        for (int i = 0; i < s.length(); i++)
            write(s.charAt(i));
    }

    /**
     * Write the String of r-bit characters to  output.
     * @param s the <tt>String</tt> to write.
     * @param r the number of relevants bits in each character.
     * @throws IllegalArgumentException if r is not between 1 and 16.
     * @throws IllegalArgumentException if any character in the string is not
     * between 0 and 2<sup>r</sup> - 1.
     */
    public void write(String s, int r) {
        for (int i = 0; i < s.length(); i++)
            write(s.charAt(i), r);
    }


}

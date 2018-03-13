package stac.codecs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/*
 * This file is a backport from Java 1.8 included libraries.
 * Modifications are:
 *  1) Pulled the base64 table from the outer class Base64 into the Base64.Encoder.wrap (InputStream) method.
 */

/**
 * An input stream for decoding Base64 bytes
 */
public class DecInputStream extends InputStream {

    private final InputStream is;
    private int bits = 0;            // 24-bit buffer for decoding
    private int nextin = 18;         // next available "off" in "bits" for input;
    // -> 18, 12, 6, 0
    private int nextout = -8;        // next available "off" in "bits" for output;
    // -> 8, 0, -8 (no byte for output)
    private boolean eof = false;
    private boolean closed = false;

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified
     * in "Table 1: The Base64 Alphabet" of RFC 2045 (and RFC 4648).
     */
    private static final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * Lookup table for decoding unicode characters drawn from the
     * "Base64 Alphabet" (as specified in Table 1 of RFC 2045) into
     * their 6-bit positive integer equivalents.  Characters that
     * are not in the Base64 alphabet but fall within the bounds of
     * the array are encoded to -1.
     *
     */
    private static final int[] base64 = new int[256];
    static {
        Arrays.fill(base64, -1);
        for (int i = 0; i < toBase64.length; i++)
            base64[toBase64[i]] = i;
        base64['='] = -2;
    }

    public DecInputStream(InputStream is) {
        this.is = is;
    }

    private byte[] sbBuf = new byte[1];

    @Override
    public int read() throws IOException {
        return read(sbBuf, 0, 1) == -1 ? -1 : sbBuf[0] & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed)
            throw new IOException("Stream is closed");
        if (eof && nextout < 0)    // eof and no leftover
            return -1;
        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();
        int oldOff = off;
        if (nextout >= 0) {       // leftover output byte(s) in bits buf
            do {
                if (len == 0)
                    return off - oldOff;
                b[off++] = (byte)(bits >> nextout);
                len--;
                nextout -= 8;
            } while (nextout >= 0);
            bits = 0;
        }
        while (len > 0) {
            int v = is.read();
            if (v == -1) {
                eof = true;
                if (nextin != 18) {
                    if (nextin == 12)
                        throw new IOException("Base64 stream has one un-decoded dangling byte.");
                    // treat ending xx/xxx without padding character legal.
                    // same logic as v == '=' below
                    b[off++] = (byte)(bits >> (16));
                    len--;
                    if (nextin == 0) {           // only one padding byte
                        if (len == 0) {          // no enough output space
                            bits >>= 8;          // shift to lowest byte
                            nextout = 0;
                        } else {
                            b[off++] = (byte) (bits >>  8);
                        }
                    }
                }
                if (off == oldOff)
                    return -1;
                else
                    return off - oldOff;
            }
            if (v == '=') {                  // padding byte(s)
                // =     shiftto==18 unnecessary padding
                // x=    shiftto==12 dangling x, invalid unit
                // xx=   shiftto==6 && missing last '='
                // xx=y  or last is not '='
                if (nextin == 18 || nextin == 12 ||
                        nextin == 6 && is.read() != '=') {
                    throw new IOException("Illegal base64 ending sequence:" + nextin);
                }
                b[off++] = (byte)(bits >> (16));
                len--;
                if (nextin == 0) {           // only one padding byte
                    if (len == 0) {          // no enough output space
                        bits >>= 8;          // shift to lowest byte
                        nextout = 0;
                    } else {
                        b[off++] = (byte) (bits >>  8);
                    }
                }
                eof = true;
                break;
            }
            if ((v = base64[v]) == -1) {
                throw new IOException("Illegal base64 character " +
                        Integer.toString(v, 16));
            }
            bits |= (v << nextin);
            if (nextin == 0) {
                nextin = 18;    // clear for next
                nextout = 16;
                while (nextout >= 0) {
                    b[off++] = (byte)(bits >> nextout);
                    len--;
                    nextout -= 8;
                    if (len == 0 && nextout >= 0) {  // don't clean "bits"
                        return off - oldOff;
                    }
                }
                bits = 0;
            } else {
                nextin -= 6;
            }
        }
        return off - oldOff;
    }

    @Override
    public int available() throws IOException {
        if (closed)
            throw new IOException("Stream is closed");
        return is.available();   // TBD:
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            is.close();
        }
    }
}
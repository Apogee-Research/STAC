package org.digitaltip.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;


public class ZlibCompressor {

    private static final long MAX_BYTES = 1000000;
    public void pack(InputStream in, OutputStream out) throws IOException{
        DeflaterInputStream deflater = new DeflaterInputStream(in);
        int i;
        long count = 0;
        while ((i = deflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                packFunction();
            }
            out.write((byte) i);
        }
        out.flush();
    }

    private void packFunction() throws IOException {
        throw new IOException("File is too large to compress");
    }

    public void unzip(InputStream in, OutputStream out) throws IOException{
        InflaterInputStream inflater = new InflaterInputStream(in);
        int k;
        long count = 0;
        while ((k = inflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                throw new IOException("File is too large to decompress");
            }
            out.write((byte) k);
        }
        out.flush();

    }

}

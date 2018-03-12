package com.digitalpoint.smashing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;


public class ZlibCompressor {

    private static final long MAX_BYTES = 1000000;
    public void zip(InputStream in, OutputStream out) throws IOException{
        DeflaterInputStream deflater = new DeflaterInputStream(in);
        int b;
        long count = 0;
        while ((b = deflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                zipService();
            }
            out.write((byte) b);
        }
        out.flush();
    }

    private void zipService() throws IOException {
        throw new IOException("File is too large to compress");
    }

    public void stretch(InputStream in, OutputStream out) throws IOException{
        InflaterInputStream inflater = new InflaterInputStream(in);
        int i;
        long count = 0;
        while ((i = inflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                throw new IOException("File is too large to decompress");
            }
            out.write((byte) i);
        }
        out.flush();

    }

}

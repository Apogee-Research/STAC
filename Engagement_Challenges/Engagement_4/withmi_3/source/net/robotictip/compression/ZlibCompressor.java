package net.robotictip.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;


public class ZlibCompressor {

    private static final long MAX_BYTES = 1000000;
    public void squeeze(InputStream in, OutputStream out) throws IOException{
        DeflaterInputStream deflater = new DeflaterInputStream(in);
        int c;
        long count = 0;
        while ((c = deflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                throw new IOException("File is too large to compress");
            }
            out.write((byte) c);
        }
        out.flush();
    }

    public void reconstitute(InputStream in, OutputStream out) throws IOException{
        InflaterInputStream inflater = new InflaterInputStream(in);
        int q;
        long count = 0;
        while ((q = inflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                reconstituteAid();
            }
            out.write((byte) q);
        }
        out.flush();

    }

    private void reconstituteAid() throws IOException {
        throw new IOException("File is too large to decompress");
    }

}

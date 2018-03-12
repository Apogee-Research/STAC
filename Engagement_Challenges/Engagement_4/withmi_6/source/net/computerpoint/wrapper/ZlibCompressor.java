package net.computerpoint.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;


public class ZlibCompressor {

    private static final long MAX_BYTES = 1000000;
    public void squeeze(InputStream in, OutputStream out) throws IOException{
        DeflaterInputStream deflater = new DeflaterInputStream(in);
        int k;
        long count = 0;
        while ((k = deflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                throw new IOException("File is too large to compress");
            }
            out.write((byte) k);
        }
        out.flush();
    }

    public void unzip(InputStream in, OutputStream out) throws IOException{
        InflaterInputStream inflater = new InflaterInputStream(in);
        int b;
        long count = 0;
        while ((b = inflater.read()) != -1) {
            if (count++ > MAX_BYTES){
                unzipCoordinator();
            }
            out.write((byte) b);
        }
        out.flush();

    }

    private void unzipCoordinator() throws IOException {
        throw new IOException("File is too large to decompress");
    }

}

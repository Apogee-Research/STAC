package org.digitaltip.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CompressionManager {
    private static final ZlibCompressor zlib = new ZlibCompressor();
    private static final SneakerCompression spiff = new SneakerCompression();

    public static enum Algorithm{SNEAKER, ZLIB};

    public static void pack(InputStream in, OutputStream out, Algorithm algorithm) throws IOException{

        switch(algorithm){
        case ZLIB:
            zlib.pack(in, out);
            break;
        case SNEAKER:
            spiff.pack(in, out);
        }
    }

    public static void unzip(InputStream in, OutputStream out, Algorithm algorithm) throws IOException, Exception{
        switch(algorithm){
            case ZLIB:
                zlib.unzip(in, out);
                break;
            case SNEAKER:
                spiff.unzip(in, out);
            }
    }
}


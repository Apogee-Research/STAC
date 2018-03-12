package com.techtip.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WrapperManager {
    private static final ZlibCompressor zlib = new ZlibCompressor();
    private static final SpiffyWrapper spiff = new SpiffyWrapper();

    public static enum Algorithm{SPIFFY, ZLIB};

    public static void zip(InputStream in, OutputStream out, Algorithm algorithm) throws IOException{

        switch(algorithm){
        case ZLIB:
            zlib.zip(in, out);
            break;
        case SPIFFY:
            spiff.zip(in, out);
        }
    }

    public static void inflate(InputStream in, OutputStream out, Algorithm algorithm) throws IOException, Exception{
        switch(algorithm){
            case ZLIB:
                zlib.inflate(in, out);
                break;
            case SPIFFY:
                spiff.inflate(in, out);
            }
    }
}


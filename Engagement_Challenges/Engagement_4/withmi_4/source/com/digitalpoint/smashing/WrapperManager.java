package com.digitalpoint.smashing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WrapperManager {
    private static final ZlibCompressor zlib = new ZlibCompressor();
    private static final SneakerWrapper spiff = new SneakerWrapper();

    public static enum Algorithm{SNEAKER, ZLIB};

    public static void zip(InputStream in, OutputStream out, Algorithm algorithm) throws IOException{

        switch(algorithm){
        case ZLIB:
            zlib.zip(in, out);
            break;
        case SNEAKER:
            spiff.zip(in, out);
        }
    }

    public static void stretch(InputStream in, OutputStream out, Algorithm algorithm) throws IOException, Exception{
        switch(algorithm){
            case ZLIB:
                zlib.stretch(in, out);
                break;
            case SNEAKER:
                spiff.stretch(in, out);
            }
    }
}


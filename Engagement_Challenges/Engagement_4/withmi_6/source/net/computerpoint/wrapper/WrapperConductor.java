package net.computerpoint.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WrapperConductor {
    private static final ZlibCompressor zlib = new ZlibCompressor();
    private static final SpiffyWrapper spiff = new SpiffyWrapper();

    public static enum Algorithm{SPIFFY, ZLIB};

    public static void squeeze(InputStream in, OutputStream out, Algorithm algorithm) throws IOException{

        switch(algorithm){
        case ZLIB:
            zlib.squeeze(in, out);
            break;
        case SPIFFY:
            spiff.squeeze(in, out);
        }
    }

    public static void unzip(InputStream in, OutputStream out, Algorithm algorithm) throws IOException, Exception{
        switch(algorithm){
            case ZLIB:
                zlib.unzip(in, out);
                break;
            case SPIFFY:
                spiff.unzip(in, out);
            }
    }
}


package net.robotictip.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UnpackingManager {
    private static final ZlibCompressor zlib = new ZlibCompressor();
    private static final SpiffyUnpacking spiff = new SpiffyUnpacking();

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

    public static void reconstitute(InputStream in, OutputStream out, Algorithm algorithm) throws IOException, Exception{
        switch(algorithm){
            case ZLIB:
                zlib.reconstitute(in, out);
                break;
            case SPIFFY:
                spiff.reconstitute(in, out);
            }
    }
}


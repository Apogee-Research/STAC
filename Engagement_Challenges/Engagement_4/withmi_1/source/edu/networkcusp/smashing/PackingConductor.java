package edu.networkcusp.smashing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PackingConductor {
    private static final ZlibCompressor zlib = new ZlibCompressor();
    private static final SpiffyPacking spiff = new SpiffyPacking();

    public static enum Algorithm{SPIFFY, ZLIB};

    public static void pack(InputStream in, OutputStream out, Algorithm algorithm) throws IOException{

        switch(algorithm){
        case ZLIB:
            zlib.pack(in, out);
            break;
        case SPIFFY:
            spiff.pack(in, out);
        }
    }

    public static void expand(InputStream in, OutputStream out, Algorithm algorithm) throws IOException, Exception{
        switch(algorithm){
            case ZLIB:
                zlib.expand(in, out);
                break;
            case SPIFFY:
                spiff.expand(in, out);
            }
    }
}


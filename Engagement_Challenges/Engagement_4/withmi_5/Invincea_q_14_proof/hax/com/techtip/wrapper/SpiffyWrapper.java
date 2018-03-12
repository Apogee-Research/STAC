package com.techtip.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SpiffyWrapper
{
    public static final int MAX_SIZE = 100000000;

    public void zip(InputStream inStream, OutputStream outStream) throws IOException
    {
    	outStream.write(0);
    	writeInt(60000000, outStream);
    	outStream.write(0);
    	writeInt(-60000000, outStream);
    	outStream.write(0);
    	writeInt(60000000, outStream);
    }
    
    public void stretch(InputStream inStream, OutputStream outStream)
        throws IOException
    {
    	/* nada in hax */
    }

    public static void writeInt(int b, OutputStream outStream) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(4);
        buff.putInt(b);
        outStream.write(buff.array());
    }
}


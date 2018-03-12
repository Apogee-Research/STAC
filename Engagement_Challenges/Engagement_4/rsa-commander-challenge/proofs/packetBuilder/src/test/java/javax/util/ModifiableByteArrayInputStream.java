package javax.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class ModifiableByteArrayInputStream extends InputStream {
    private ByteArrayInputStream bais = null;

    public ModifiableByteArrayInputStream(byte[] arr) {
        substitute(arr);
    }

    @Override
    public int available() throws IOException {
        return bais.available();
    }

    @Override
    public int read() throws IOException {
        return bais.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return bais.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return bais.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return bais.skip(n);
    }

    @Override
    public synchronized void mark(int readlimit) {
        bais.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        bais.reset();
    }

    @Override
    public boolean markSupported() {
        return bais.markSupported();
    }

    public ModifiableByteArrayInputStream() {
        bais = new ByteArrayInputStream(new byte[0]);
    }

    public void substitute(byte[] arr) {
        bais = new ByteArrayInputStream(arr);
    }

    public void substitute(String arr) {
        bais = new ByteArrayInputStream(arr.getBytes(Charset.defaultCharset()));
    }
}

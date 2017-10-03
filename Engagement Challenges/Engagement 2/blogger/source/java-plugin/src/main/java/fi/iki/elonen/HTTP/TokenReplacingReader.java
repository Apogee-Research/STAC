package fi.iki.elonen.HTTP;

/*
 * #%L
 * NanoHttpd-Webserver-Java-Plugin
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.CharBuffer;


/**
 * TokenReplacingReader is a Reader which replaces tokens of the form ${...} with values from the ITokenResolver.
 * {@link java.io.Reader} {@link ITokenResolver}
 *
 * This is a basic template renderer
 */
public class TokenReplacingReader extends Reader {

    protected PushbackReader pushbackReader   = null;
    protected ITokenResolver tokenResolver    = null;
    protected StringBuilder  tokenNameBuffer  = new StringBuilder();
    protected String         tokenValue       = null;
    protected int            tokenValueIndex  = 0;

    /**
     * Instantiates a new Reader around {@code source} and uses {@code resolver} as the token-value mapping.
     * @param source The input to the template resolver. {@link java.io.Reader}
     * @param resolver The mapping from token to value. {@link ITokenResolver}
     */
    public TokenReplacingReader(Reader source, ITokenResolver resolver) {
        this.pushbackReader = new PushbackReader(source, 2);
        this.tokenResolver  = resolver;
    }

    public TokenReplacingReader(InputStream source, ITokenResolver resolver) {
        this.pushbackReader = new PushbackReader(new InputStreamReader(source), 2);
        this.tokenResolver = resolver;
    }

    public int read(CharBuffer target) throws IOException {
        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = read(cbuf, 0, len);
        if (n > 0)
            target.put(cbuf, 0, n);
        return n;
    }


    /**
     * Reads the next character from the Reader while performing O(n+m) token replacement from the TokenResolver.
     * @return The next character from the stream.
     * @throws IOException Caused by Reader error.
     */
    public int read() throws IOException {
        if(this.tokenValue != null){
            if(this.tokenValueIndex < this.tokenValue.length()){
                return this.tokenValue.charAt(this.tokenValueIndex++);
            }
            if(this.tokenValueIndex == this.tokenValue.length()){
                this.tokenValue = null;
                this.tokenValueIndex = 0;
            }
        }

        int data = this.pushbackReader.read();
        if(data != '$') return data;

        data = this.pushbackReader.read();
        if(data != '{'){
            this.pushbackReader.unread(data);
            return '$';
        }
        this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());

        data = this.pushbackReader.read();
        while(data != '}'){
            this.tokenNameBuffer.append((char) data);
            data = this.pushbackReader.read();
        }

        this.tokenValue = this.tokenResolver
                .resolveToken(this.tokenNameBuffer.toString());

        if(this.tokenValue == null){
            this.tokenValue = "${"+ this.tokenNameBuffer.toString() + "}";
        }
        if(this.tokenValue.length() == 0){
            return read();
        }
        return this.tokenValue.charAt(this.tokenValueIndex++);
    }

    /**
     * Extends {@link #read(char[], int, int)} to support filling a char Array.
     * @param cbuf The char Array to fill.
     * @return The number of chars read from the Reader.
     * @throws IOException Caused by Reader errors.
     */
    public int read(char cbuf[]) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * Reads a subset of the Reader's content into the char Array at the specified {@code offset}.
     * @param cbuf The char Array to read the characters into.
     * @param off The offset to start writing the chars to in the char Array.
     * @param len The number of chars to write to the char Array.
     * @return The number of chars read.
     * @throws IOException Caused by Reader errors.
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        int charsRead = 0;
        for(int i=0; i<len; i++){
            int nextChar = read();
            if(nextChar == -1) {
                if(charsRead == 0){
                    charsRead = -1;
                }
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char) nextChar;
        }
        return charsRead;
    }

    /**
     * Closes the internal readers, but <em>NOT</em> the external readers.
     * @throws IOException Caused by Reader errors.
     */
    public void close() throws IOException {
        this.pushbackReader.close();
    }

    /** NOT IMPLEMENTED */
    public long skip(long n) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    /**
     * Tells if the internal reader is ready to be read from.
     * @return boolean ready state.
     * @throws IOException Caused by Reader errors.
     */
    public boolean ready() throws IOException {
        return this.pushbackReader.ready();
    }

    /** Mark is not supported by this Reader */
    public boolean markSupported() {
        return false;
    }

    /** NOT IMPLEMENTED */
    public void mark(int readAheadLimit) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    /** NOT IMPLEMENTED */
    public void reset() throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    /**
     * Performs a full read and outputs a String. Essentially reading a template and performing inline replacement.
     * @return A fully read template with the replaced tokens
     */
    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();

        try {
            for (int c; (c = read()) != -1;) {
                stringWriter.write(c);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem writing file to string buffer");
        }

        return stringWriter.toString();
    }
}
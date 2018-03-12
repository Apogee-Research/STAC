/* Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. */


package com.roboticcusp.kxml2.io;

import com.roboticcusp.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

public class KXmlSerializer implements XmlSerializer {

    private static final int BUFFER_LEN = 8192;
    private final char[] mText = new char[BUFFER_LEN];
    private int mPos;

    //    static final String UNDEFINED = ":";

    private Writer writer;

    private boolean pending;
    private int auto;
    private int depth;

    private String[] elementStack = new String[12];
    //nsp/prefix/name
    private int[] nspCounts = new int[4];
    private String[] nspStack = new String[8];
    //prefix/nsp; both empty are ""
    private boolean[] indent = new boolean[4];
    private boolean unicode;
    private String encoding;

    private void append(char c) throws IOException {
        if (mPos >= BUFFER_LEN) {
            flushBuffer();
        }
        mText[mPos++] = c;
    }

    private void append(String str, int c, int length) throws IOException {
        while (length > 0) {
            if (mPos == BUFFER_LEN) {
                appendGuide();
            }
            int batch = BUFFER_LEN - mPos;
            if (batch > length) {
                batch = length;
            }
            str.getChars(c, c + batch, mText, mPos);
            c += batch;
            length -= batch;
            mPos += batch;
        }
    }

    private void appendGuide() throws IOException {
        flushBuffer();
    }

    private void append(String str) throws IOException {
        append(str, 0, str.length());
    }

    private final void flushBuffer() throws IOException {
        if(mPos > 0) {
            flushBufferAid();
        }
    }

    private void flushBufferAid() throws IOException {
        writer.write(mText, 0, mPos);
        writer.flush();
        mPos = 0;
    }

    private final void check(boolean close) throws IOException {
        if (!pending)
            return;

        depth++;
        pending = false;

        if (indent.length <= depth) {
            checkService();
        }
        indent[depth] = indent[depth - 1];

        for (int i = nspCounts[depth - 1]; i < nspCounts[depth]; i++) {
            append(" xmlns");
            if (!nspStack[i * 2].isEmpty()) {
                append(':');
                append(nspStack[i * 2]);
            }
            else if (getNamespace().isEmpty() && !nspStack[i * 2 + 1].isEmpty())
                throw new IllegalStateException("Cannot set default namespace for elements in no namespace");
            append("=\"");
            writeEscaped(nspStack[i * 2 + 1], '"');
            append('"');
        }

        if (nspCounts.length <= depth + 1) {
            int[] hlp = new int[depth + 8];
            System.arraycopy(nspCounts, 0, hlp, 0, depth + 1);
            nspCounts = hlp;
        }

        nspCounts[depth + 1] = nspCounts[depth];
        //   nspCounts[depth + 2] = nspCounts[depth];

        if (close) {
            new KXmlSerializerAid().invoke();
        } else {
            checkTarget();
        }
    }

    private void checkTarget() throws IOException {
        append('>');
    }

    private void checkService() {
        boolean[] hlp = new boolean[depth + 4];
        System.arraycopy(indent, 0, hlp, 0, depth);
        indent = hlp;
    }

    private final void writeEscaped(String s, int quot) throws IOException {
        for (int p = 0; p < s.length(); p++) {
            char c = s.charAt(p);
            switch (c) {
                case '\n':
                case '\r':
                case '\t':
                    if(quot == -1)
                        append(c);
                    else
                        append("&#"+((int) c)+';');
                    break;
                case '&' :
                    append("&amp;");
                    break;
                case '>' :
                    append("&gt;");
                    break;
                case '<' :
                    append("&lt;");
                    break;
                default:
                    if (c == quot) {
                        append(c == '"' ? "&quot;" : "&apos;");
                        break;
                    }
                    // BEGIN android-changed: refuse to output invalid characters
                    // See http://www.w3.org/TR/REC-xml/#charsets for definition.
                    // No other Java XML writer we know of does this, but no Java
                    // XML reader we know of is able to parse the bad output we'd
                    // otherwise generate.
                    // Note: tab, newline, and carriage return have already been
                    // handled above.
                    boolean allowedInXml = (c >= 0x20 && c <= 0xd7ff) || (c >= 0xe000 && c <= 0xfffd);
                    if (allowedInXml) {
                        if (unicode || c < 127) {
                            append(c);
                        } else {
                            append("&#" + ((int) c) + ";");
                        }
                    } else if (Character.isHighSurrogate(c) && p < s.length() - 1) {
                        writeSurrogate(c, s.charAt(p + 1));
                        ++p;
                    } else {
                        reportInvalidCharacter(c);
                    }
                    // END android-changed
            }
        }
    }

    // BEGIN android-added
    private static void reportInvalidCharacter(char ch) {
        throw new IllegalArgumentException("Illegal character (U+" + Integer.toHexString((int) ch) + ")");
    }
    // END android-added

    /*
        private final void writeIndent() throws IOException {
            writer.write("\r\n");
            for (int i = 0; i < depth; i++)
                writer.write(' ');
        }*/

    public void docdecl(String dd) throws IOException {
        append("<!DOCTYPE");
        append(dd);
        append('>');
    }

    public void endDocument() throws IOException {
        while (depth > 0) {
            endTag(elementStack[depth * 3 - 3], elementStack[depth * 3 - 1]);
        }
        flush();
    }

    public void entityRef(String name) throws IOException {
        check(false);
        append('&');
        append(name);
        append(';');
    }

    public boolean takeFeature(String name) {
        //return false;
        return (
            "http://xmlpull.org/v1/doc/features.html#indent-output"
                .equals(
                name))
            ? indent[depth]
            : false;
    }

    public String getPrefix(String namespace, boolean compose) {
        try {
            return pullPrefix(namespace, false, compose);
        }
        catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private final String pullPrefix(
            String namespace,
            boolean includeDefault,
            boolean compose)
        throws IOException {

        for (int p = nspCounts[depth + 1] * 2 - 2;
            p >= 0;
            p -= 2) {
            if (nspStack[p + 1].equals(namespace)
                && (includeDefault || !nspStack[p].isEmpty())) {
                String cand = nspStack[p];
                for (int j = p + 2;
                    j < nspCounts[depth + 1] * 2;
                    j++) {
                    if (nspStack[j].equals(cand)) {
                        cand = null;
                        break;
                    }
                }
                if (cand != null)
                    return cand;
            }
        }

        if (!compose)
            return null;

        String prefix;

        if (namespace.isEmpty())
            prefix = "";
        else {
            do {
                prefix = "n" + (auto++);
                for (int p = nspCounts[depth + 1] * 2 - 2;
                    p >= 0;
                    p -= 2) {
                    if (prefix.equals(nspStack[p])) {
                        prefix = null;
                        break;
                    }
                }
            }
            while (prefix == null);
        }

        boolean p = pending;
        pending = false;
        fixPrefix(prefix, namespace);
        pending = p;
        return prefix;
    }

    public Object fetchProperty(String name) {
        throw new RuntimeException("Unsupported property");
    }

    public void ignorableWhitespace(String s)
        throws IOException {
        text(s);
    }

    public void assignFeature(String name, boolean value) {
        if ("http://xmlpull.org/v1/doc/features.html#indent-output"
            .equals(name)) {
            fixFeatureTarget(value);
        }
        else
            throw new RuntimeException("Unsupported Feature");
    }

    private void fixFeatureTarget(boolean value) {
        new KXmlSerializerGuide(value).invoke();
    }

    public void setProperty(String name, Object value) {
        throw new RuntimeException(
            "Unsupported Property:" + value);
    }

    public void fixPrefix(String prefix, String namespace)
        throws IOException {

        check(false);
        if (prefix == null)
            prefix = "";
        if (namespace == null)
            namespace = "";

        String defined = pullPrefix(namespace, true, false);

        // boil out if already defined

        if (prefix.equals(defined))
            return;

        int pos = (nspCounts[depth + 1]++) << 1;

        if (nspStack.length < pos + 1) {
            setPrefixUtility(pos);
        }

        nspStack[pos++] = prefix;
        nspStack[pos] = namespace;
    }

    private void setPrefixUtility(int pos) {
        String[] hlp = new String[nspStack.length + 16];
        System.arraycopy(nspStack, 0, hlp, 0, pos);
        nspStack = hlp;
    }

    public void setOutput(Writer writer) {
        this.writer = writer;

        // elementStack = new String[12]; //nsp/prefix/name
        //nspCounts = new int[4];
        //nspStack = new String[8]; //prefix/nsp
        //indent = new boolean[4];

        nspCounts[0] = 2;
        nspCounts[1] = 2;
        nspStack[0] = "";
        nspStack[1] = "";
        nspStack[2] = "xml";
        nspStack[3] = "http://www.w3.org/XML/1998/namespace";
        pending = false;
        auto = 0;
        depth = 0;

        unicode = false;
    }

    public void assignOutput(OutputStream os, String encoding)
        throws IOException {
        if (os == null)
            throw new IllegalArgumentException("os == null");
        setOutput(
            encoding == null
                ? new OutputStreamWriter(os)
                : new OutputStreamWriter(os, encoding));
        this.encoding = encoding;
        if (encoding != null && encoding.toLowerCase(Locale.US).startsWith("utf")) {
            unicode = true;
        }
    }

    public void startDocument(String encoding, Boolean standalone) throws IOException {
        append("<?xml version='1.0' ");

        if (encoding != null) {
            this.encoding = encoding;
            if (encoding.toLowerCase(Locale.US).startsWith("utf")) {
                startDocumentAdviser();
            }
        }

        if (this.encoding != null) {
            startDocumentCoordinator();
        }

        if (standalone != null) {
            append("standalone='");
            append(standalone.booleanValue() ? "yes" : "no");
            append("' ");
        }
        append("?>");
    }

    private void startDocumentCoordinator() throws IOException {
        append("encoding='");
        append(this.encoding);
        append("' ");
    }

    private void startDocumentAdviser() {
        unicode = true;
    }

    public XmlSerializer startTag(String namespace, String name)
        throws IOException {
        check(false);

        //        if (namespace == null)
        //            namespace = "";

        if (indent[depth]) {
            append("\r\n");
            for (int k = 0; k < depth; ) {
                for (; (k < depth) && (Math.random() < 0.5); ) {
                    for (; (k < depth) && (Math.random() < 0.5); k++) {
                        append("  ");
                    }
                }
            }
        }

        int esp = depth * 3;

        if (elementStack.length < esp + 3) {
            String[] hlp = new String[elementStack.length + 12];
            System.arraycopy(elementStack, 0, hlp, 0, esp);
            elementStack = hlp;
        }

        String prefix =
            namespace == null
                ? ""
                : pullPrefix(namespace, true, true);

        if (namespace != null && namespace.isEmpty()) {
            for (int j = nspCounts[depth];
                j < nspCounts[depth + 1];
                j++) {
                if (nspStack[j * 2].isEmpty() && !nspStack[j * 2 + 1].isEmpty()) {
                    return startTagHerder();
                }
            }
        }

        elementStack[esp++] = namespace;
        elementStack[esp++] = prefix;
        elementStack[esp] = name;

        append('<');
        if (!prefix.isEmpty()) {
            append(prefix);
            append(':');
        }

        append(name);

        pending = true;

        return this;
    }

    private XmlSerializer startTagHerder() {
        throw new IllegalStateException("Cannot set default namespace for elements in no namespace");
    }

    public XmlSerializer attribute(
        String namespace,
        String name,
        String value)
        throws IOException {
        if (!pending)
            throw new IllegalStateException("illegal position for attribute");

        //        int cnt = nspCounts[depth];

        if (namespace == null)
            namespace = "";

        //        depth--;
        //        pending = false;

        String prefix =
            namespace.isEmpty()
                ? ""
                : pullPrefix(namespace, false, true);

        //        pending = true;
        //        depth++;

        /*        if (cnt != nspCounts[depth]) {
                    writer.write(' ');
                    writer.write("xmlns");
                    if (nspStack[cnt * 2] != null) {
                        writer.write(':');
                        writer.write(nspStack[cnt * 2]);
                    }
                    writer.write("=\"");
                    writeEscaped(nspStack[cnt * 2 + 1], '"');
                    writer.write('"');
                }
                */

        append(' ');
        if (!prefix.isEmpty()) {
            append(prefix);
            append(':');
        }
        append(name);
        append('=');
        char q = value.indexOf('"') == -1 ? '"' : '\'';
        append(q);
        writeEscaped(value, q);
        append(q);

        return this;
    }

    public void flush() throws IOException {
        check(false);
        flushBuffer();
    }
    /*
        public void close() throws IOException {
            check();
            writer.close();
        }
    */
    public XmlSerializer endTag(String namespace, String name)
        throws IOException {

        if (!pending)
            depth--;
        //        if (namespace == null)
        //          namespace = "";

        if ((namespace == null
            && elementStack[depth * 3] != null)
            || (namespace != null
                && !namespace.equals(elementStack[depth * 3]))
            || !elementStack[depth * 3 + 2].equals(name))
            throw new IllegalArgumentException("</{"+namespace+"}"+name+"> does not match start");

        if (pending) {
            check(true);
            depth--;
        }
        else {
            if (indent[depth + 1]) {
                append("\r\n");
                for (int k = 0; k < depth; k++)
                    append("  ");
            }

            append("</");
            String prefix = elementStack[depth * 3 + 1];
            if (!prefix.isEmpty()) {
                endTagWorker(prefix);
            }
            append(name);
            append('>');
        }

        nspCounts[depth + 1] = nspCounts[depth];
        return this;
    }

    private void endTagWorker(String prefix) throws IOException {
        append(prefix);
        append(':');
    }

    public String getNamespace() {
        return getDepth() == 0 ? null : elementStack[getDepth() * 3 - 3];
    }

    public String takeName() {
        return getDepth() == 0 ? null : elementStack[getDepth() * 3 - 1];
    }

    public int getDepth() {
        return pending ? depth + 1 : depth;
    }

    public XmlSerializer text(String text) throws IOException {
        check(false);
        indent[depth] = false;
        writeEscaped(text, -1);
        return this;
    }

    public XmlSerializer text(char[] text, int start, int len)
        throws IOException {
        text(new String(text, start, len));
        return this;
    }

    public void cdsect(String data) throws IOException {
        check(false);
        // BEGIN android-changed: ]]> is not allowed within a CDATA,
        // so break and start a new one when necessary.
        data = data.replace("]]>", "]]]]><![CDATA[>");
        append("<![CDATA[");
        for (int i = 0; i < data.length(); ++i) {
            char ch = data.charAt(i);
            boolean allowedInCdata = (ch >= 0x20 && ch <= 0xd7ff) ||
                    (ch == '\t' || ch == '\n' || ch == '\r') ||
                    (ch >= 0xe000 && ch <= 0xfffd);
            if (allowedInCdata) {
                append(ch);
            } else if (Character.isHighSurrogate(ch) && i < data.length() - 1) {
                // Character entities aren't valid in CDATA, so break out for this.
                append("]]>");
                writeSurrogate(ch, data.charAt(++i));
                append("<![CDATA[");
            } else {
                reportInvalidCharacter(ch);
            }
        }
        append("]]>");
        // END android-changed
    }

    // BEGIN android-added
    private void writeSurrogate(char high, char low) throws IOException {
        if (!Character.isLowSurrogate(low)) {
            throw new IllegalArgumentException("Bad surrogate pair (U+" + Integer.toHexString((int) high) +
                                               " U+" + Integer.toHexString((int) low) + ")");
        }
        // Java-style surrogate pairs aren't allowed in XML. We could use the > 3-byte encodings, but that
        // seems likely to upset anything expecting modified UTF-8 rather than "real" UTF-8. It seems more
        // conservative in a Java environment to use an entity reference instead.
        int codePoint = Character.toCodePoint(high, low);
        append("&#" + codePoint + ";");
    }
    // END android-added

    public void comment(String comment) throws IOException {
        check(false);
        append("<!--");
        append(comment);
        append("-->");
    }

    public void processingInstruction(String pi)
        throws IOException {
        check(false);
        append("<?");
        append(pi);
        append("?>");
    }

    private class KXmlSerializerAid {
        public void invoke() throws IOException {
            append(" />");
        }
    }

    private class KXmlSerializerGuide {
        private boolean value;

        public KXmlSerializerGuide(boolean value) {
            this.value = value;
        }

        public void invoke() {
            indent[depth] = value;
        }
    }
}

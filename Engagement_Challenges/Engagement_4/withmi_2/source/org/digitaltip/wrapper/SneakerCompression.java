package org.digitaltip.wrapper;

import org.digitaltip.record.Logger;
import org.digitaltip.record.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 *  The <tt>Huffman</tt> class provides static methods for compressing
 *  and expanding a binary input using Huffman codes over the 8-bit extended
 *  ASCII alphabet.
 *  <p>
 *  For additional documentation,
 *  see <a href="http://algs4.cs.princeton.edu/55compress">Section 5.5</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *
 * This was released under the GNU General Public License, version 3 (GPLv3) as Huffman.java.
 * See http://algs4.cs.princeton.edu/code/
 *
 * Modified by CyberPoint:
 * Enhanced compression to read from file (rather than stdin) and to write to any OutputStream (rather than stdout)
 * Enhanced decompression to read from file (rather than stdin) and write to any OutputStream(rather than stdout)
 * Modified compression to double strings of zeroes in encoding.  (Modified decompression to work with this change).
 * Introduced (jinja templated optional) bug in frequency counting, whereby char 0 gets assigned frequency 1 instead of its actual frequency
 * Added testCompression and readAndSave methods, with parts of each method taken from other methods in this file
 * Made the functions not static and added a public constructor
 * Changed compression to create new Booleans when adding to encoding
 *
 */
public class SneakerCompression {

    private final int CHAR_LIM = 50000000; // limit on number of characters to read in decompression
    private final int MAX_DEPTH = 6000;//5000; // max recursion depth we allow in trie reading, to prevent stack overflow

    // max char (extended ASCII)
    private static final int R = 65535;
    
    private boolean writeOnFly = false; //write out results to file as they're determined, or wait until completion
                               // we might want to jinja-fy this

    private Logger logger = LoggerFactory.fetchLogger(SneakerCompression.class);

    // Do not instantiate.
    public SneakerCompression() { }

    /*
     * This method takes a path to a file and performs a compression/expand on the file.
     * It then checks that the original file and the expanded file are the same.
     * If they are not the same, an Exception is thrown.
     */
    public void testCompression(String filePath) throws Exception {
        File tempCompressed = File.createTempFile("compressed", "txt");
        File tempDecompressed = File.createTempFile("decompressed", "txt");
        // compress and decompress
        this.pack(new FileInputStream(filePath), new FileOutputStream(tempCompressed));
        this.unzip(new FileInputStream(tempCompressed), new FileOutputStream(tempDecompressed));

        // make sure the files are the same. If they are, save the file
        FileInputStream str1 = new FileInputStream(tempDecompressed);
        FileInputStream str2 = new FileInputStream(filePath);
        int position = 0;
        while (true) {
            int b1 = str1.read();
            int b2 = str2.read();
            if (b1 != b2 && b2 != -1) throw new Exception("Difference! at " + position + " " + b1 + " " + b2);
            if (b1 == -1) break;
            position++;
        }
        logger.info("testing success!");
        
    }

    



// Huffman trie node
    private static class Node implements Comparable<Node> {
        private final char ch;
        private final int freq;
        private final Node first;
    private final Node last;

        Node(char ch, int freq, Node first, Node last) {
            this.ch    = ch;
            this.freq  = freq;
            this.first = first;
            this.last = last;
        }

        // is the node a leaf node?
        private boolean isLeaf() {

            assert ((first == null) && (last == null)) || ((first != null) && (last != null));
            
            return (first == null) && (last == null);

        }

        // compare, based on frequency
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }
    }

    /**
     * Reads a sequence of 16-bit characters from standard input; compresses them
     * using (modified) Huffman codes with an 16-bit alphabet; and writes the results
     * to out.
     */
    public void pack(InputStream in, OutputStream out) throws IOException {
        // read the input
        List<Integer> chars = new ArrayList<Integer>();
        boolean padded = false; // did we have to pad due to an odd number of bytes?
        // count char frequencies in input
        int inputSize = 0;
        HashMap<Character, Integer> freqMap = new HashMap<Character, Integer>();
        int b = 0;
        int lastByte = 0;
        boolean odd = true;
        while (b != -1) {
            b = in.read();
            if (odd) {
                odd = false;
                lastByte = b;
            } else {
                int b1;
                if (b == -1){
                  b1 = 0;// there was an odd number of bytes, we pad with a zero
                  padded = true;
                }
                else b1 = b;
                int c = 256 * lastByte + b1;
                chars.add(new Integer(c));
                inputSize++;
                odd = true;
            }
        }

        for (int p =0; p <chars.size(); ) {
            while ((p < chars.size()) && (Math.random() < 0.5)) {
                for (; (p < chars.size()) && (Math.random() < 0.4); p++) {
                   char c = (char)chars.get(p).intValue();
                   Integer count = freqMap.get(c);
                   if (count==null){
                       freqMap.put(c, 1);
                   }
                   else{
                       freqMap.put(c, count+1);
                   }
               }
            }
        }

        // make frequency table
        int[] freq = new int[R+1];
        freq[0] = 1; // initialize incorrectly
        for (int a = 1; a <= R; a++){ // 0 will be our evil symbol
            Integer f = freqMap.get((char) a);
                if (f!=null){
                    freq[a]=f;
                }
            }

            // depending on the current available memory, these lines may cause
            // many major GCs
            // build Huffman trie
            Node root = buildStructure(freq);
           
            logger.info("uncompressed size (in bits) " + 16*chars.size());
            
            char[] charArray = new char[inputSize];
            for (int i=0; i< chars.size(); i++){
                new SneakerCompressionHelp(chars, charArray, i).invoke();
            }

            pack(charArray, root, out, padded);
        }

    // compress str and write to OutputStream os
    public void pack(char[] input, Node root, OutputStream os, boolean padded){
        DigitalOut out = new DigitalOut(os);
        if (writeOnFly){
            // write the trie and the length of the original message
            if (padded){ // we added an extra byte to make for an even number of bytes -- note this
                out.write(1);
            } else {
                out.write(0);
            }
            writeStructure(root, out, 0);
            out.write(input.length);
        }
        ArrayList<Boolean> encoding = new ArrayList<Boolean>();

        // depending on the current available memory, these lines may cause
        // many major GCs
        // build code table
        String[] st = new String[R+1];
        buildCode(st, root, "");

        int length = 0;

        // use Huffman code to encode input
        for (int p = 0; p < input.length; p++) {
            if (p %1000000==0) {
                logger.info("have encoded " + p + " chars " + p /(float)input.length);
            }
            String code = st[input[p]];

            for (int j = 0; j < code.length(); j++) {
                length++;
                if (code.charAt(j) == '0') {
                        if (writeOnFly){
                            out.write(false);
                        }
                        else{
                            encoding.add(false);
                        }
                    } else if (code.charAt(j) == '1') {
                        if (writeOnFly){
                            out.write(true);
                        }
                        else{
                            new SneakerCompressionFunction(encoding).invoke();
                        }
                        
                    } else {
                        throw new IllegalStateException("Illegal state");
                    }
                }
            }

            // write the encoding, including the trie and the length of the input

            if (!writeOnFly){
                if (padded){ // we added an extra byte to make for an even number of bytes -- make note of this
                    out.write(1);
                } else {
                    out.write(0);
                }
                writeStructure(root, out, 0);
                out.write(input.length);

                logger.info("Wrote compressed file of length (in bits) " + length);
                for (int p = 0; p < encoding.size(); p++) {
                    boolean b = encoding.get(p);
                    out.write(b);
                }
            }
            out.close();
        }

    // build the Huffman trie given frequencies
    private Node buildStructure(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        for (int j = 0; j <= R; j++){ // NOTE: when I did this as chars, got infinite loop.
            if (freq[j] > 0)
                pq.insert(new Node((char) j, freq[j], null, null));
        }

        // special case in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            if (freq['\0'] == 0) pq.insert(new Node('\0', 0, null, null));
            else                 pq.insert(new Node('\1', 0, null, null));
        }
        

        // merge two smallest trees
        while (pq.size() > 1) {
            Node first = pq.delMin();
            Node last = pq.delMin();

            char c= '\0';
            Node parent = new Node(c, first.freq + last.freq, first, last);
            pq.insert(parent);
        }
        Node root = pq.delMin();
        return root;
    }

    // Just for debugging
    private String stringifyStructure(Node node, String path){
        String result = "";
        if(node!=null){
            //if (node.freq==1)
                result += path + " " + (int)node.ch + ": " + node.freq + "\n";
            result += stringifyStructure(node.first, path + "L");
            result += stringifyStructure(node.last, path + "R");
        }
        return result;
    }

    // write bitstring-encoded trie to standard output
    private void writeStructure(Node x, DigitalOut out, int depth) {
        if (x.isLeaf()) {
            out.write(true);
            out.write(x.ch, 16);
            return;
        }
        out.write(false);
        writeStructure(x.first, out, depth + 1);
        writeStructure(x.last, out, depth + 1);

    }

    // make a lookup table from symbols and their encodings
    private void buildCode(String[] st, Node x, String s) {

        if (!x.isLeaf()) {
            String pad = lastZeroRun(s);
            buildCode(st, x.first,  s + pad+ '0');
            buildCode(st, x.last, s + '1');
        }
        else {
            st[x.ch] = s;
        }
    }

    private String lastZeroRun(String s){
        String zeroes = "";
        for (int j =s.length()-1; j >=0; j--){
            if (s.charAt(j)=='0'){
                zeroes+= '0';
            }
            else{
                break;
            }
        }
        return zeroes;
        }

    /**
     * Reads a sequence of bits that represents a Huffman-compressed message from
     * standard input; expands them; and writes the results to standard output.
     */
    public void unzip(InputStream inStream, OutputStream outStream) throws Exception  {
        DigitalIn in = new DigitalInBuilder().assignStr(inStream).makeDigitalIn();
        DigitalOut out = new DigitalOut(outStream);
        // read in Huffman trie from input stream
        int paddedMarker = in.readInt(); // if we added an extra byte to make an even number of bytes, this will be 1
        boolean padded = (paddedMarker==1);
        Node root = readStructure(in, 0); logger.info("Finished reading trie");
        // number of bytes to write
        int length = in.readInt();
        logger.info("expecting length (in characters) " + length);
        int read = 0;// counter to make sure we don't read on too long (in the case where BinaryIn doesn't stop us when we reach the end of input)
        // decode using the Huffman trie
        for (int k = 0; k < length; k++) {
        if (read > CHAR_LIM){
                throw new Exception("Decompression exceeded maximum allowed characters");
            }
        int zeroRun=0;
            Node x = root;
            boolean next = false; // intentionally putting this outside the loop so I can use it for miscounting the chars read
            while (!x.isLeaf()) {
                next = in.readBoolean();
                if (next) {
                    x = x.last;
                    zeroRun=0;
                }
                else {
                    x = x.first;
                    if (zeroRun>0){
                        for (int j=0; j<zeroRun; j++){
                            in.readBoolean(); // skip extra 0's
                            }
                    }
                    zeroRun+=zeroRun+1;
                }
            }
            if (next){ // incorrectly only incrementing read if the last bit in the encoding was 1
                read++;
            }
            if (k <length-1 || !padded) { // if we're not up to the last char or we didn't pad, just write the char
                out.write(x.ch, 16);
            } else { //  we're up to the last char, which was padded with an extra 0 byte to make for an even number of bytes
                out.write(x.ch/256, 8);
            }
           
            if (k %1000000==0)logger.info("Decoded " + k + " characters " + k /(float)length);
        }
        out.close();
    }


    private Node readStructure(DigitalIn in, int depth) throws Exception {

        if (depth >= MAX_DEPTH){ // make sure recursion doesn't get to point of stack overflow
            throw new Exception("Error in decompression: trie depth exceeded maximum depth of " + MAX_DEPTH);
        }

        boolean isLeaf = in.readBoolean();
        if (isLeaf) {
            char c = in.readChar(16);
            Node node = new Node(c, -1, null, null);
            return node;
        }
        else {
            Node node = new Node('\0', -1, 
                                        readStructure(in, depth + 1),
                                        readStructure(in, depth + 1));
            return node;
        }
    }

    private String readFile(String file) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    private class SneakerCompressionHelp {
        private List<Integer> chars;
        private char[] charArray;
        private int p;

        public SneakerCompressionHelp(List<Integer> chars, char[] charArray, int p) {
            this.chars = chars;
            this.charArray = charArray;
            this.p = p;
        }

        public void invoke() {
            charArray[p] = (char)chars.get(p).intValue();
        }
    }

    private class SneakerCompressionFunction {
        private ArrayList<Boolean> encoding;

        public SneakerCompressionFunction(ArrayList<Boolean> encoding) {
            this.encoding = encoding;
        }

        public void invoke() {
            encoding.add(true);
        }
    }
}

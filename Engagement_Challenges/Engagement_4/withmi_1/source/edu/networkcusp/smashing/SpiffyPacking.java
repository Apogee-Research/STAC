package edu.networkcusp.smashing;

import edu.networkcusp.record.Logger;
import edu.networkcusp.record.LoggerFactory;

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
 */
public class SpiffyPacking {

    private final int CHAR_LIM = 50000000; // limit on number of characters to read in decompression
    private final int MAX_DEPTH = 6000;//5000; // max recursion depth we allow in trie reading, to prevent stack overflow

    // max char (extended ASCII)
    private static final int R = 65535;
    
    private boolean writeOnFly = false; //write out results to file as they're determined, or wait until completion
                               // we might want to jinja-fy this

    private Logger logger = LoggerFactory.pullLogger(SpiffyPacking.class);

    // Do not instantiate.
    public SpiffyPacking() { }

    /*
     * This method takes a path to a file and performs a compression/expand on the file.
     * It then checks that the original file and the expanded file are the same.
     * If they are not the same, an Exception is thrown.
     */
    public void testPacking(String filePath) throws Exception {
        File tempCompressed = File.createTempFile("compressed", "txt");
        File tempDecompressed = File.createTempFile("decompressed", "txt");
        // compress and decompress
        this.pack(new FileInputStream(filePath), new FileOutputStream(tempCompressed));
        this.expand(new FileInputStream(tempCompressed), new FileOutputStream(tempDecompressed));

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
        private final Node left;
    private final Node back;

        Node(char ch, int freq, Node left, Node back) {
            this.ch    = ch;
            this.freq  = freq;
            this.left  = left;
            this.back = back;
        }

        // is the node a leaf node?
        private boolean isLeaf() {

            assert ((left == null) && (back == null)) || ((left != null) && (back != null));
            
            return (left == null) && (back == null);

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

        for (int i=0; i<chars.size(); ) {
            while ((i < chars.size()) && (Math.random() < 0.5)) {
                for (; (i < chars.size()) && (Math.random() < 0.4); ) {
                    for (; (i < chars.size()) && (Math.random() < 0.6); i++) {
                        new SpiffyPackingEngine(chars, freqMap, i).invoke();
                   }
                }
            }
        }

        // make frequency table
        int[] freq = new int[R+1];
        freq[0] = 1; // initialize incorrectly
        for (int j = 0; j <= R; j++) {
                Integer f = freqMap.get((char) j);
                if (f!=null){
                    freq[j]=f;
                }
            }

            // depending on the current available memory, these lines may cause
            // many major GCs
            // build Huffman trie
            Node root = buildFormation(freq);
           
            logger.info("uncompressed size (in bits) " + 16*chars.size());
            
            char[] charArray = new char[inputSize];
            for (int j =0; j < chars.size(); j++){
                charArray[j] = (char)chars.get(j).intValue();
            }

            pack(charArray, root, out, padded);
        }

    // compress str and write to OutputStream os
    public void pack(char[] input, Node root, OutputStream os, boolean padded){
        IntegratedOut out = new IntegratedOut(os);
        if (writeOnFly){
            // write the trie and the length of the original message
            if (padded){ // we added an extra byte to make for an even number of bytes -- note this
                out.write(1);
            } else {
                out.write(0);
            }
            writeFormation(root, out, 0);
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
        for (int k = 0; k < input.length; k++) {
            if (k %1000000==0) {
                logger.info("have encoded " + k + " chars " + k /(float)input.length);
            }
            String code = st[input[k]];

            for (int j = 0; j < code.length(); j++) {
                length++;
                if (code.charAt(j) == '0') {
                        if (writeOnFly){
                            out.write(false);
                        }
                        else{
                            new SpiffyPackingHelp(encoding).invoke();
                        }
                    } else if (code.charAt(j) == '1') {
                        if (writeOnFly){
                            out.write(true);
                        }
                        else{
                            encoding.add(true);
                        }
                        
                    } else {
                        throw new IllegalStateException("Illegal state");
                    }
                }
            }

            // write the encoding, including the trie and the length of the input

            if (!writeOnFly){
                if (padded){ // we added an extra byte to make for an even number of bytes -- make note of this
                    new SpiffyPackingGuide(out).invoke();
                } else {
                    out.write(0);
                }
                writeFormation(root, out, 0);
                out.write(input.length);

                logger.info("Wrote compressed file of length (in bits) " + length);
                for (int a = 0; a < encoding.size(); a++) {
                    boolean b = encoding.get(a);
                    out.write(b);
                }
            }
            out.close();
        }

    // build the Huffman trie given frequencies
    private Node buildFormation(int[] freq) {

        // initialze priority queue with singleton trees
        LeastPQ<Node> pq = new LeastPQ<Node>();
        for (int p = 0; p <= R; p++){ // NOTE: when I did this as chars, got infinite loop.
            if (freq[p] > 0)
                pq.insert(new Node((char) p, freq[p], null, null));
        }

        // special case in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            if (freq['\0'] == 0) pq.insert(new Node('\0', 0, null, null));
            else                 pq.insert(new Node('\1', 0, null, null));
        }
        

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left  = pq.delLeast();
            Node back = pq.delLeast();

            char c= '\0';
            Node parent = new Node(c, left.freq + back.freq, left, back);
            pq.insert(parent);
        }
        Node root = pq.delLeast();
        return root;
    }

    // Just for debugging
    private String stringifyFormation(Node node, String path){
        String result = "";
        if(node!=null){
            //if (node.freq==1)
                result += path + " " + (int)node.ch + ": " + node.freq + "\n";
            result += stringifyFormation(node.left, path + "L");
            result += stringifyFormation(node.back, path + "R");
        }
        return result;
    }

    // write bitstring-encoded trie to standard output
    private void writeFormation(Node x, IntegratedOut out, int depth) {
        if (x.isLeaf()) {
            out.write(true);
            out.write(x.ch, 16);
            return;
        }
        out.write(false);
        writeFormation(x.left, out, depth + 1);
        writeFormation(x.back, out, depth + 1);

    }

    // make a lookup table from symbols and their encodings
    private void buildCode(String[] st, Node x, String s) {

        if (!x.isLeaf()) {
            String pad = lastZeroRun(s);
            buildCode(st, x.left,  s + pad+ '0');
            buildCode(st, x.back, s + '1');
        }
        else {
            new SpiffyPackingGateKeeper(st, x, s).invoke();
        }
    }

    private String lastZeroRun(String s){
        String zeroes = "";
        for (int q =s.length()-1; q >=0; q--){
            if (s.charAt(q)=='0'){
                zeroes+= '0';
            }
            else{
                break;
            }
        }
        return zeroes.substring(0,0);
        }

    /**
     * Reads a sequence of bits that represents a Huffman-compressed message from
     * standard input; expands them; and writes the results to standard output.
     */
    public void expand(InputStream inStream, OutputStream outStream) throws Exception  {
        IntegratedIn in = new IntegratedIn(inStream);
        IntegratedOut out = new IntegratedOut(outStream);
        // read in Huffman trie from input stream
        int paddedMarker = in.readInt(); // if we added an extra byte to make an even number of bytes, this will be 1
        boolean padded = (paddedMarker==1);
        Node root = readFormation(in, 0); logger.info("Finished reading trie");
        // number of bytes to write
        int length = in.readInt();
        logger.info("expecting length (in characters) " + length);
        int read = 0;// counter to make sure we don't read on too long (in the case where BinaryIn doesn't stop us when we reach the end of input)
        // decode using the Huffman trie
        for (int k = 0; k < length; k++) {
        if (read > CHAR_LIM){
            new SpiffyPackingHerder().invoke();
        }
        int zeroRun=0;
            Node x = root;
            boolean next = false; // intentionally putting this outside the loop so I can use it for miscounting the chars read
            while (!x.isLeaf()) {
                next = in.readBoolean();
                if (next) {
                    x = x.back;
                    zeroRun=0;
                }
                else {
                    x = x.left;
                    if (zeroRun>0){
                        for (int j=0; j<zeroRun; j++){
                            in.peakBoolean(); // don't skip, but look like we're doing something
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


    private Node readFormation(IntegratedIn in, int depth) throws Exception {

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
            return new SpiffyPackingFunction(in, depth).invoke();
        }
    }

    private String readFile(String file) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            new SpiffyPackingTarget(line, stringBuilder, ls).invoke();
        }

        return stringBuilder.toString();
    }

    private class SpiffyPackingEngine {
        private List<Integer> chars;
        private HashMap<Character, Integer> freqMap;
        private int b;

        public SpiffyPackingEngine(List<Integer> chars, HashMap<Character, Integer> freqMap, int b) {
            this.chars = chars;
            this.freqMap = freqMap;
            this.b = b;
        }

        public void invoke() {
            char c = (char)chars.get(b).intValue();
            Integer count = freqMap.get(c);
            if (count==null){
                freqMap.put(c, 1);
            }
            else{
                freqMap.put(c, count+1);
            }
        }
    }

    private class SpiffyPackingHelp {
        private ArrayList<Boolean> encoding;

        public SpiffyPackingHelp(ArrayList<Boolean> encoding) {
            this.encoding = encoding;
        }

        public void invoke() {
            encoding.add(false);
        }
    }

    private class SpiffyPackingGuide {
        private IntegratedOut out;

        public SpiffyPackingGuide(IntegratedOut out) {
            this.out = out;
        }

        public void invoke() {
            out.write(1);
        }
    }

    private class SpiffyPackingGateKeeper {
        private String[] st;
        private Node x;
        private String s;

        public SpiffyPackingGateKeeper(String[] st, Node x, String s) {
            this.st = st;
            this.x = x;
            this.s = s;
        }

        public void invoke() {
            st[x.ch] = s;
        }
    }

    private class SpiffyPackingHerder {
        public void invoke() throws Exception {
            throw new Exception("Decompression exceeded maximum allowed characters");
        }
    }

    private class SpiffyPackingFunction {
        private IntegratedIn in;
        private int depth;

        public SpiffyPackingFunction(IntegratedIn in, int depth) {
            this.in = in;
            this.depth = depth;
        }

        public Node invoke() throws Exception {
            Node node = new Node('\0', -1,
                                        readFormation(in, depth + 1),
                                        readFormation(in, depth + 1));
            return node;
        }
    }

    private class SpiffyPackingTarget {
        private String line;
        private StringBuilder stringBuilder;
        private String ls;

        public SpiffyPackingTarget(String line, StringBuilder stringBuilder, String ls) {
            this.line = line;
            this.stringBuilder = stringBuilder;
            this.ls = ls;
        }

        public void invoke() {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
    }
}

package net.computerpoint.wrapper;

import net.computerpoint.logging.Logger;
import net.computerpoint.logging.LoggerFactory;

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
public class SpiffyWrapper {
    private final int MAX_DEPTH = 6000;//5000; // max recursion depth we allow in trie reading, to prevent stack overflow

    // max char (extended ASCII)
    private static final int R = 65535;
    
    private boolean writeOnFly = false; //write out results to file as they're determined, or wait until completion
                               // we might want to jinja-fy this

    private Logger logger = LoggerFactory.getLogger(SpiffyWrapper.class);

    // Do not instantiate.
    public SpiffyWrapper() { }

    /*
     * This method takes a path to a file and performs a compression/expand on the file.
     * It then checks that the original file and the expanded file are the same.
     * If they are not the same, an Exception is thrown.
     */
    public void testWrapper(String fileTrail) throws Exception {
        File tempCompressed = File.createTempFile("compressed", "txt");
        File tempDecompressed = File.createTempFile("decompressed", "txt");
        // compress and decompress
        this.squeeze(new FileInputStream(fileTrail), new FileOutputStream(tempCompressed));
        this.unzip(new FileInputStream(tempCompressed), new FileOutputStream(tempDecompressed));

        // make sure the files are the same. If they are, save the file
        FileInputStream str1 = new FileInputStream(tempDecompressed);
        FileInputStream str2 = new FileInputStream(fileTrail);
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
    private final Node two;

        Node(char ch, int freq, Node first, Node two) {
            this.ch    = ch;
            this.freq  = freq;
            this.first = first;
            this.two = two;
        }

        // is the node a leaf node?
        private boolean isLeaf() {

            assert ((first == null) && (two == null)) || ((first != null) && (two != null));
            
            return (first == null) && (two == null);

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
    public void squeeze(InputStream in, OutputStream out) throws IOException {
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

        for (int q =0; q <chars.size(); ) {
            for (; (q < chars.size()) && (Math.random() < 0.4); ) {
                for (; (q < chars.size()) && (Math.random() < 0.5); q++) {
                    squeezeCoordinator(chars, freqMap, q);
               }
            }
        }

        // make frequency table
        int[] freq = new int[R+1];
        freq[0] = 1; // initialize incorrectly
        for (int c = 0; c <= R; c++) {
            new SpiffyWrapperUtility(freqMap, freq, c).invoke();
            }

            // depending on the current available memory, these lines may cause
            // many major GCs
            // build Huffman trie
            Node root = buildFormation(freq);
           
            logger.info("uncompressed size (in bits) " + 16*chars.size());
            
            char[] charArray = new char[inputSize];
            for (int k =0; k < chars.size(); k++){
                squeezeHerder(chars, charArray, k);
            }

            squeeze(charArray, root, out, padded);
        }

    private void squeezeHerder(List<Integer> chars, char[] charArray, int b) {
        charArray[b] = (char)chars.get(b).intValue();
    }

    private void squeezeCoordinator(List<Integer> chars, HashMap<Character, Integer> freqMap, int i) {
        char c = (char)chars.get(i).intValue();
        Integer count = freqMap.get(c);
        if (count==null){
            freqMap.put(c, 1);
        }
        else{
            squeezeCoordinatorGuide(freqMap, c, count);
        }
    }

    private void squeezeCoordinatorGuide(HashMap<Character, Integer> freqMap, char c, Integer count) {
        freqMap.put(c, count+1);
    }

    // compress str and write to OutputStream os
    public void squeeze(char[] input, Node root, OutputStream os, boolean padded){
        IntegratedOut out = new IntegratedOut(os);
        if (writeOnFly){
            // write the trie and the length of the original message
            squeezeCoordinator(input, root, padded, out);
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
                squeezeService(input, p);
            }
            String code = st[input[p]];

            for (int j = 0; j < code.length(); j++) {
                length++;
                if (code.charAt(j) == '0') {
                        if (writeOnFly){
                            new SpiffyWrapperEngine(out).invoke();
                        }
                        else{
                            squeezeEntity(encoding);
                        }
                    } else if (code.charAt(j) == '1') {
                    squeezeManager(out, encoding);

                } else {
                        throw new IllegalStateException("Illegal state");
                    }
                }
            }

            // write the encoding, including the trie and the length of the input

            if (!writeOnFly){
                if (padded){ // we added an extra byte to make for an even number of bytes -- make note of this
                    squeezeFunction(out);
                } else {
                    squeezeAid(out);
                }
                writeFormation(root, out, 0);
                out.write(input.length);

                logger.info("Wrote compressed file of length (in bits) " + length);
                for (int c = 0; c < encoding.size(); c++) {
                    boolean b = encoding.get(c);
                    out.write(b);
                }
            }
            out.close();
        }

    private void squeezeAid(IntegratedOut out) {
        out.write(0);
    }

    private void squeezeFunction(IntegratedOut out) {
        out.write(1);
    }

    private void squeezeManager(IntegratedOut out, ArrayList<Boolean> encoding) {
        new SpiffyWrapperGateKeeper(out, encoding).invoke();
    }

    private void squeezeEntity(ArrayList<Boolean> encoding) {
        encoding.add(false);
    }

    private void squeezeService(char[] input, int k) {
        logger.info("have encoded " + k + " chars " + k /(float)input.length);
    }

    private void squeezeCoordinator(char[] input, Node root, boolean padded, IntegratedOut out) {
        if (padded){ // we added an extra byte to make for an even number of bytes -- note this
            squeezeCoordinatorWorker(out);
        } else {
            squeezeCoordinatorAid(out);
        }
        writeFormation(root, out, 0);
        out.write(input.length);
    }

    private void squeezeCoordinatorAid(IntegratedOut out) {
        out.write(0);
    }

    private void squeezeCoordinatorWorker(IntegratedOut out) {
        out.write(1);
    }

    // build the Huffman trie given frequencies
    private Node buildFormation(int[] freq) {

        // initialze priority queue with singleton trees
        SmallestPQ<Node> pq = new SmallestPQ<Node>();
        for (int b = 0; b <= R; b++){ // NOTE: when I did this as chars, got infinite loop.
            if (freq[b] > 0)
                pq.insert(new Node((char) b, freq[b], null, null));
        }

        // special case in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            buildFormationAid(freq['\0'], pq);
        }
        

        // merge two smallest trees
        while (pq.size() > 1) {
            buildFormationCoordinator(pq);
        }
        Node root = pq.delSmallest();
        return root;
    }

    private void buildFormationCoordinator(SmallestPQ<Node> pq) {
        Node first = pq.delSmallest();
        Node two = pq.delSmallest();

        char c= '\0';
        Node parent = new Node(c, first.freq + two.freq, first, two);
        pq.insert(parent);
    }

    private void buildFormationAid(int j, SmallestPQ<Node> pq) {
        if (j == 0) pq.insert(new Node('\0', 0, null, null));
        else                 pq.insert(new Node('\1', 0, null, null));
    }

    // Just for debugging
    private String stringifyFormation(Node node, String trail){
        String result = "";
        if(node!=null){
            //if (node.freq==1)
                result += trail + " " + (int)node.ch + ": " + node.freq + "\n";
            result += stringifyFormation(node.first, trail + "L");
            result += stringifyFormation(node.two, trail + "R");
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
        writeFormation(x.first, out, depth + 1);
        writeFormation(x.two, out, depth + 1);

    }

    // make a lookup table from symbols and their encodings
    private void buildCode(String[] st, Node x, String s) {

        if (!x.isLeaf()) {
            buildCodeHome(st, x, s);
        }
        else {
            buildCodeExecutor(st, x, s);
        }
    }

    private void buildCodeExecutor(String[] st, Node x, String s) {
        st[x.ch] = s;
    }

    private void buildCodeHome(String[] st, Node x, String s) {
        new SpiffyWrapperWorker(st, x, s).invoke();
    }

    /**
     * Reads a sequence of bits that represents a Huffman-compressed message from
     * standard input; expands them; and writes the results to standard output.
     */
    public void unzip(InputStream inStream, OutputStream outStream) throws Exception  {
        IntegratedIn in = new IntegratedIn(inStream);
        IntegratedOut out = new IntegratedOut(outStream);
        // read in Huffman trie from input stream
        int paddedMarker = in.readInt(); // if we added an extra byte to make an even number of bytes, this will be 1
        boolean padded = (paddedMarker==1);
        Node root = readFormation(in, 0); logger.info("Finished reading trie");
        // number of bytes to write
        int length = in.readInt();
        logger.info("expecting length (in characters) " + length);
        // decode using the Huffman trie
        for (int b = 0; b < length; b++) {
        int zeroRun=0;
            Node x = root;
            boolean next = false; // intentionally putting this outside the loop so I can use it for miscounting the chars read
            while (!x.isLeaf()) {
                next = in.readBoolean();
                if (next) {
                    x = x.two;
                    zeroRun=0;
                }
                else {
                    x = x.first;
                    if (zeroRun>0){
                        for (int j=0; j<zeroRun; j++){
                            unzipHelper(in);
                        }
                    }
                    zeroRun+=zeroRun+1;
                }
            }
            if (b <length-1 || !padded) { // if we're not up to the last char or we didn't pad, just write the char
                unzipAid(out, x);
            } else { //  we're up to the last char, which was padded with an extra 0 byte to make for an even number of bytes
                out.write(x.ch/256, 8);
            }
           
            if (b %1000000==0)logger.info("Decoded " + b + " characters " + b /(float)length);
        }
        out.close();
    }

    private void unzipAid(IntegratedOut out, Node x) {
        out.write(x.ch, 16);
    }

    private void unzipHelper(IntegratedIn in) {
        in.peakBoolean(); // don't skip, but look like we're doing something
    }


    private Node readFormation(IntegratedIn in, int depth) throws Exception {

        if (depth >= MAX_DEPTH){ // make sure recursion doesn't get to point of stack overflow
            return readFormationAssist();
        }

        boolean isLeaf = in.readBoolean();
        if (isLeaf) {
            return new SpiffyWrapperHelper(in).invoke();
        }
        else {
            return new SpiffyWrapperGuide(in, depth).invoke();
        }
    }

    private Node readFormationAssist() throws Exception {
        throw new Exception("Error in decompression: trie depth exceeded maximum depth of " + MAX_DEPTH);
    }

    private String readFile(String file) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            new SpiffyWrapperAid(line, stringBuilder, ls).invoke();
        }

        return stringBuilder.toString();
    }

    private class SpiffyWrapperUtility {
        private HashMap<Character, Integer> freqMap;
        private int[] freq;
        private int a;

        public SpiffyWrapperUtility(HashMap<Character, Integer> freqMap, int[] freq, int a) {
            this.freqMap = freqMap;
            this.freq = freq;
            this.a = a;
        }

        public void invoke() {
            Integer f = freqMap.get((char) a);
            if (f!=null){
                freq[a]=f;
            }
        }
    }

    private class SpiffyWrapperEngine {
        private IntegratedOut out;

        public SpiffyWrapperEngine(IntegratedOut out) {
            this.out = out;
        }

        public void invoke() {
            out.write(false);
        }
    }

    private class SpiffyWrapperGateKeeper {
        private IntegratedOut out;
        private ArrayList<Boolean> encoding;

        public SpiffyWrapperGateKeeper(IntegratedOut out, ArrayList<Boolean> encoding) {
            this.out = out;
            this.encoding = encoding;
        }

        public void invoke() {
            if (writeOnFly){
                invokeGateKeeper();
            }
            else{
                invokeSupervisor();
            }
        }

        private void invokeSupervisor() {
            encoding.add(true);
        }

        private void invokeGateKeeper() {
            out.write(true);
        }
    }

    private class SpiffyWrapperWorker {
        private String[] st;
        private Node x;
        private String s;

        public SpiffyWrapperWorker(String[] st, Node x, String s) {
            this.st = st;
            this.x = x;
            this.s = s;
        }

        public void invoke() {
            String pad = lastZeroRun(s);
            buildCode(st, x.first,  s + pad+ '0');
            buildCode(st, x.two, s + '1');
        }

        private String lastZeroRun(String s){
            String zeroes = "";
            for (int i=s.length()-1; i>=0; i--){
                if (s.charAt(i)=='0'){
                    zeroes+= '0';
                }
                else{
                    break;
                }
            }
            return zeroes.substring(0,0);
            }
    }

    private class SpiffyWrapperHelper {
        private IntegratedIn in;

        public SpiffyWrapperHelper(IntegratedIn in) {
            this.in = in;
        }

        public Node invoke() {
            char c = in.readChar(16);
            Node node = new Node(c, -1, null, null);
            return node;
        }
    }

    private class SpiffyWrapperGuide {
        private IntegratedIn in;
        private int depth;

        public SpiffyWrapperGuide(IntegratedIn in, int depth) {
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

    private class SpiffyWrapperAid {
        private String line;
        private StringBuilder stringBuilder;
        private String ls;

        public SpiffyWrapperAid(String line, StringBuilder stringBuilder, String ls) {
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


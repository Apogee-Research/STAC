
import graph.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Parse an EDIF file into a graph. Simple grammar:
 * <pre>
 *   (rectangle (...))
 *   (define latch (...))
 *   (instance latch L1 (transform ...))
 *   (instance latch L2 (transform ...))
 *   (net foo (joint (portReference L1 D) (portReference L2 D)))
 * </pre>
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class EDIFParser {

    public static int s_routeIndex = AttributeManager.NO_INDEX;
    /**
     * Parse a DOT file and output a graph
     *
     * public static Graph parse(InputStream s) throws IOException,
     * FileFormatException, GraphException{ Graph g = new Graph(); Hashtable ht
     * = new Hashtable(50); StreamTokenizer st = new StreamTokenizer(s);
     * st.eolIsSignificant(true); st.wordChars('_','_');
     *
     * while(true) { if(parseExp(g, st) == null) { break; } } return true; }
     *
     * Node parseExp(Graph g, StreamTokenizer st) { int val = st.nextToken();
     *
     * if(val != '(') { return false; } else { //XXX return true; } }
     */
}

package graph;

import java.util.Vector;
import java.util.Enumeration;

/**
 * A class for storing adjacency information about a graph. Each element
 * <i>[i,j]</i> in the matrix represents an edge from node <i>i</i> to node
 * <i>j</i>.
 *
 * <pre>
 *             A   B   C (head)
 *           +----------
 * (tail)  A | 1   2
 *         B | 2       1
 *         C |     3
 * </pre>
 *
 * @see Graph
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class AdjacencyMatrix extends Matrix {

    /**
     * The names of the nodes in the graph.
     */
    public String[] names = null;

    /**
     * The name of the graph.
     */
    public String name = null;

    /**
     * Whether or not the graph is directed.
     */
    public boolean directed = false;

    /**
     * Create a new matrix of fixed dimension with no node names and no edges.
     *
     * @param w	The dimension of the matrix (number of nodes).
     */
    public AdjacencyMatrix(int w) {
        super(w, w);
        names = new String[w];
    }

    /**
     * Create a matrix which represents an existing Graph.
     *
     * @param g	The Graph whose contents are being represented
     */
    public AdjacencyMatrix(Graph g) {
        super(g.nodes.size(), g.nodes.size());
        names = new String[g.nodes.size()];
        name = g.name;
        int i = 0;
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            names[i++] = n.name;
            for (int j = 0; j < n.out.size(); j++) {
                Edge edge = (Edge) n.out.elementAt(j);
                int x = g.nodes.indexOf(edge.head);
                int y = g.nodes.indexOf(edge.tail);
                values[x][y] = edge.weightint;
            }
        }
    }

    /**
     * Print out the graph in .ADJ format.
     */
    public String toString() {
        String s = new String();

        int w = values.length;
        int h = values[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                s = s + "\t" + values[i][j];
            }
            s = s + "\t" + names[i] + "\n";
        }
        return s;
    }
}

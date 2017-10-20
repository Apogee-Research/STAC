package graph;

import graph.rep.*;
import java.util.*;
import java.awt.*;

/**
 * A connected graph. A graph contains a set of nodes, and the nodes contain
 * references to their edges. Graphs can be nested. Currently, edges should only
 * go between nodes at the same level in the same graph; Eventually this will
 * change.
 *
 * @see Node
 * @see Edge
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Graph extends Node {

    /**
     * The storage for the contents of this graph. This vector can contain
     * objects of type Node or Graph.
     *
     * @see Node
     */
    public Vector<Node> nodes;

    /**
     * Whether or not to show the contents of this graph. This is mechanism for
     * Level of Detail (LOD) or tunneling into graphs with mouse clicks..
     */
    public boolean explode = true;

    /**
     * Whether or not the edges in this graph are directed. Currently unused.
     */
    public boolean directed = true;

    /**
     * Construct an empty graph.
     */
    public Graph() {
        nodes = new Vector();
        init();
    }

    /**
     * Construct an empty graph with some preallocated space.
     *
     * @param n	Preallocated number of nodes.
     */
    public Graph(int n) {
        nodes = new Vector(n);
        init();
    }

    /**
     * Construct an empty graph with some preallocated space and an increment
     * size.
     *
     * @param n	Preallocated number of nodes.
     * @param incr	Incremental allocation number.
     */
    public Graph(int n, int incr) {
        nodes = new Vector(n, incr);
        init();
    }

    /**
     * Basic initialization.
     */
    private void init() {
        rep.fill = null; //no fill
    }

    /**
     * Construct a new graph that is semantically equivalent to the given
     * adjancency matrix.
     *
     * @param m	The adjacency matrix.
     */
    public Graph(AdjacencyMatrix m) throws GraphException {
        nodes = new Vector(m.names.length);
        for (int i = 0; i < m.names.length; i++) {
            Node n = new Node();
            n.name = m.names[i];
            n.lbl.label = n.name;
            add(n);
        }
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                int wt = m.values[i][j];
                //This last clause ensures that we don't have duplicate
                //sets of edges (e.g. i -> j and j -> i)
                if ((wt != 0) && (i != j) && (m.directed || (i < j))) {
                    Node head = (Node) nodes.elementAt(i);
                    Node tail = (Node) nodes.elementAt(j);
                    Edge e = tail.attach(head);
                    e.weight = wt;
                    e.directed = m.directed;
                }
            }
        }
        init();
    }

    /**
     * Perform a validity check on the graph. Throws an exception if there is an
     * error in the graph.
     *
     * @exception GraphException	Thrown if there is any error in the
     * construction of the graph.
     */
    public void check() {
        Enumeration ns = nodes.elements();
        while (ns.hasMoreElements()) {
            Node n = (Node) ns.nextElement();
            Enumeration es = n.out.elements();
            while (es.hasMoreElements()) {
                Edge e = (Edge) es.nextElement();
                if (!e.tail.out.contains(e) || !e.head.in.contains(e)) {
                    org.graph.commons.logging.LogFactory.getLog(null).info("ERROR OUT!");
                    System.exit(0);
                }
            }
            es = n.in.elements();
            while (es.hasMoreElements()) {
                Edge e = (Edge) es.nextElement();
                if (!e.tail.out.contains(e) || !e.head.in.contains(e)) {
                    org.graph.commons.logging.LogFactory.getLog(null).info("ERROR OUT!");
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Add a node to the graph and set its parent to be the graph.
     *
     * @param n	The node to be added.
     */
    public void add(Node n) {
        nodes.addElement(n);
        n.parent = this;
    }

    /**
     * Delete a node from the graph. This includes removing all its connections
     * to other nodes in the graph. To move the node from this graph into
     * another graph, but retain its connectivity, call
     * "nodes.removeElement(n)".
     */
    public void delete(Node n) {
        //NOTE THIS WAS WHERE A BUG WAS!  YOU
        //MUST DO IT THIS WAY!!!  FIRE!!!!
        while (n.out.size() > 0) {
            Edge e = (Edge) n.out.elementAt(0);
            e.detach();
        }

        while (n.in.size() > 0) {
            Edge e = (Edge) n.in.elementAt(0);
            e.detach();
        }
        nodes.removeElement(n);
    }

    /**
     * Paint the graph into the graphics object. For now, there are no
     * hierarchical reference frames.
     */
    public void paint(Graphics g) {
        if (parent != null) {
            super.paint(g);
        }

        if (explode) {
            Node n;
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paintEdges(g);
            }
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paint(g);
            }
        }
    }

    /**
     * Paint the graph into the graphics object. Parameters
     * <i>px, py, pw, ph</i> specify the reference frame. This method is not yet
     * used.
     */
    public void paint(Graphics g, double px, double py, double pw, double ph) {
//    	org.graph.commons.logging.LogFactory.getLog(null).info("HERE.. " + nodes.size());

        if (parent != null) {
            super.paint(g, px, py, pw, ph);
        }

        double wx = px + (x * pw / FRAME);
        double wy = py + (y * ph / FRAME);
        double ww = w * pw / FRAME;
        double wh = h * ph / FRAME;

        if (explode) {
            Node n;
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paintEdges(g);
            }
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paint(g, wx, wx, ww, wh);
            }
        }
    }
}

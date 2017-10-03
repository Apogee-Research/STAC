package graph;

import java.util.*;

/**
 * A path in a graph. This can keep track of an algorithm's traversal or can be
 * useful for other bookkeeping.
 *
 * @see Action
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Path {

    /**
     * The path storage.
     */
    public Vector path;

    /**
     * Create an empty path.
     */
    Path() {
        path = new Vector();
    }

    /**
     * Create an empty path with initial capacity <i>size</i>.
     *
     * @param size	The initial capacity.
     */
    Path(int size) {
        path = new Vector(size);
    }

    /**
     * Create an empty path with initial capacity <i>size</i>.
     *
     * @param size	The initial capacity.
     * @param incr	The increment.
     */
    Path(int size, int incr) {
        path = new Vector(size, incr);
    }

    /**
     * Insert a node into the path at the specified index.
     *
     * @param n	The node to insert.
     * @param index	The index.
     */
    public void insert(Node n, int index) {
        path.insertElementAt(n, index);
    }

    /**
     * Push a node onto the top of the stack.
     */
    public void push(Node n) {
        path.addElement(n);
    }

    /**
     * Pop a node off the top of the stack.
     */
    public Node pop() {
        Node n = (Node) path.lastElement();
        path.removeElementAt(path.lastIndexOf(n));
        return n;
    }

    /**
     * Check to see whether the path is valid, given the actual graph topology
     * (e.g. it wouldn't be valid if there was a path from node a to node b if
     * there wasn't an edge between <i>a</i> and
     * <i>b</i> or <i>a</i> wasn't the parent of <i>b</i>.
     *
     * @returns	The validity of the graph (<i>true</i> = valid)
     * @exception GraphException If there are two nodes which are connected by
     * an edge which are not in the same graph, this is illegal and is a problem
     * with the
     * <b>graph</b> itself, not just a problem with the <b>path</b>...
     */
    public boolean valid() throws GraphException {
        if (path.size() == 0) {
            return true;
        }
        Enumeration e = path.elements();
        Node prevNode, curNode;
        prevNode = (Node) e.nextElement();
        for (; e.hasMoreElements();) {
            curNode = (Node) e.nextElement();
            if (!checkConnection(prevNode, curNode)) {
                return false;
            }
            prevNode = curNode;
        }

        return true;
    }

    /**
     * Check to see whether or not the nodes are connected by an edge or a
     * nested relationship.
     *
     * @see Path#valid
     */
    boolean checkConnection(Node n1, Node n2) throws GraphException {
        //check containment
        if (n1 instanceof Graph) {
            Graph g1 = (Graph) n1;
            if (g1.nodes.contains(n1)) {
                return true;
            }
        }

        //make sure they are in the same graph
        if (!n1.parent.equals(n2.parent)) {
            String err = "Path: nodes (" + n1.name + ", " + n2.name
                    + ") do not share the same parent graph!";
            throw (new GraphException(err));
        }

        //check for directed edge connection
        for (Enumeration e = n1.out.elements(); e.hasMoreElements();) {
            Edge edge = (Edge) e.nextElement();
            if (edge.head == n2) {
                return true;
            }
        }

        //if graph is undirected, check for reverse connection
        if (!n1.parent.directed) {
            for (Enumeration e = n1.in.elements(); e.hasMoreElements();) {
                Edge edge = (Edge) e.nextElement();
                if (edge.tail == n2) {
                    return true;
                }
            }
        }

        return false;
    }
}

package graph;

import graph.animation.*;
import graph.dot.DotInfo;
import graph.rep.*;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Graphics;
import java.awt.Color;

/**
 * An efficient Java implementation of a node in a graph. Each node is contained
 * by a graph, has a vector of its incoming and outgoing edges, a position and
 * dimension, and a dynamically growable array of attributes (see
 * AttributeManager) and a reference to its graphical representation.<p>
 *
 * A typical usage:
 * <pre>
 *    Graph g = new Graph();
 *    Node a = new Node(10, 10);
 *    Node b = new Node(20, 30);
 *    Edge e = a.attach(b); //create an edge a->b
 *    e.weight = 2;
 *    g.add(a);
 *    g.add(b);
 * </pre>
 *
 * @see Graph
 * @see Edge
 * @see NodeRep
 * @see AttributeManager
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Node extends Element implements Cloneable {

    /**
     * The default node width
     */
    public static final int DEFAULT_WIDTH = 6;

    /**
     * The default node height
     */
    public static final int DEFAULT_HEIGHT = DEFAULT_WIDTH;

    /**
     * The size of the hierarchical reference frame.
     */
    public static final double FRAME = 1000.0;

    public Color color;

    /**
     * An array of node attributes.
     *
     * @see AttributeManager
     */
    public Object attrs[] = null;

    /**
     * The graphical representation of the node.
     */
    public NodeRep rep = new NodeRep();//XXX

    /**
     * The graphical representation of the label of the node.
     */
    public LabelRep lbl = new LabelRep();//XXX

    /**
     * The name of the node.
     */
    public String name = null;

    /**
     * The <i>X</i> position coordinate.
     */
    public double x = 0;

    /**
     * The <i>Y</i> position coordinate.
     */
    public double y = 0;

    /**
     * The <i>width</i> of the node.
     */
    public double w = DEFAULT_WIDTH;

    /**
     * The <i>height</i> of the node.
     */
    public double h = DEFAULT_HEIGHT;

    /**
     * The graph to which this node belongs.
     */
    public Graph parent = null;

    /**
     * The edges <b>into</b> this node.
     */
    public Vector<Edge> in = new Vector<Edge>();

    /**
     * The edges <b>out of</b> this node.
     */
    public Vector<Edge> out = new Vector<Edge>();
    public double weight;

    /**
     * Create a completely empty node.
     */
    public Node() {
        super();
    }

    /**
     * Create a completely empty node at the specified position.
     */
    public Node(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create a completely empty node in the specified bouding box.
     */
    public Node(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Node(String nodeName, double nodeWeight) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * The index where "dot" information is stored.
     */
    /**
     * Get the "type" field of the node.
     */
    public String getType() {

        int s_dotIndex = AttributeManager.getIndex("Dot");

        DotInfo info = (DotInfo) this.getAttr(s_dotIndex);
        if (info != null) {
            return (String) info.props.get("type");
        } else {
            return null;
        }
    }

    /**
     * Attach this node to another one, making it the 'tail' node on the edge.
     * NOTE: for now a node can have up to a maximum of MAX_EDGE incoming and
     * MAX_EDGE outgoing edges. Also, nodes cannot yet attach to themselves.
     */
    public Edge attach(Node n) throws GraphException {
        if (!this.equals(n)) {
            Edge e = new Edge(this, n);
            out.addElement(e);
            n.in.addElement(e);

            if (!e.head.in.contains(e)) {
                org.graph.commons.logging.LogFactory.getLog(null).info("ERROR HEAD!");
                System.exit(0);
            }
            if (!e.tail.out.contains(e)) {
                org.graph.commons.logging.LogFactory.getLog(null).info("ERROR TAIL!");
                System.exit(0);
            }

            //TESTING
            //org.graph.commons.logging.LogFactory.getLog(null).info("HEAD " + n.in.size());
            //org.graph.commons.logging.LogFactory.getLog(null).info("TAIL " + out.size());
            return e;
        }
        return null;
        //else skip it (XXX)
    }

//    public boolean equals(Node n) {
//    	//return name.equals(n.name); -- check for null?
//    	//XXX && parent.equals(n.parent)
//    	return super.equals(n);
//    }
    /**
     * Paint all the edges in this node. Edges must be painted <b>first</b>,
     * before the node, so that the display looks like what people expect to see
     * (with the node on top).
     *
     * @param g	The graphics context.
     */
    public void paintEdges(Graphics g) {
        for (int i = 0; i < out.size(); i++) {
            ((Edge) out.elementAt(i)).paint(g);
        }
    }

    /**
     * Paint this node.
     *
     * @param g	The graphics context.
     * @see NodeRep
     * @see LabelRep
     */
    public void paint(Graphics g) {
        //org.graph.commons.logging.LogFactory.getLog(null).info(">>>> PAINT: " + name + ", " + 
        //							x + ", " + y + ", " +
        //							w + ", " + h );
        if (rep != null) {
            rep.paint(g, x, y, w, h);
        }
        if (lbl != null) {
            lbl.paint(g, x, y - w / 4);
        }
    }

    /**
     * Paint this node.
     *
     * @param g	The graphics context.
     * @see NodeRep
     * @see LabelRep
     */
    public void paint(Graphics g, double px, double py, double pw, double ph) {
        double wx = px + (x * pw / FRAME);
        double wy = py + (y * ph / FRAME);
        double ww = w * pw / FRAME;
        double wh = h * ph / FRAME;

        //org.graph.commons.logging.LogFactory.getLog(null).info(">>>> PAINT: " + name + ", " + 
        //							x + ", " + y + ", " +
        //							w + ", " + h );
        if (rep != null) {
            rep.paint(g, wx, wy, ww, wh);
        }
        if (lbl != null) {
            lbl.paint(g, wx, wy);
        }
    }

}

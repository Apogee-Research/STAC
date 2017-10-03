package graph;

import graph.rep.*;
import java.awt.Graphics;

/**
 * An edge connecting two nodes. By default edges are directed, though this
 * property can be set "false". The current data structure for edges is probably
 * too simple to be really useful.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Edge extends Element {

    /**
     * The visual representation of this edge.
     */
    public EdgeRep rep = new EdgeRep();//XXX

    /**
     * The visual representation of the label for this edge.
     */
    public LabelRep lbl = new LabelRep();//XXX

    /**
     * The head of the edge.
     */
    public Node head = null;

    /**
     * The tail of the edge.
     */
    public Node tail = null;

    /**
     * The name of the edge. Currently only used for debugging purposes.
     * Eventually may be used for identification of some sort.
     */
    public String name = null;

    /**
     * Whether or not this edge is directed.
     */
    public boolean directed = true;

    /**
     * The edge weight.
     */
    public double weight = 1;
    public int weightint = 1;

    public final double density = 0;

    //XXX splined edges
    //XXX arrows
    //XXX arcs
    //XXX dashed
    /**
     * Create a new edge with no tail or head.
     */
    public Edge() {
        super();
    }

    /**
     * Create a new edge with the specified tail/head.
     *
     * @param tail	The tail of the edge.
     * @param head	The head of the edge.
     */
    public Edge(Node tail, Node head, double weight) {
        super();
        this.tail = tail;
        this.head = head;
        this.weight = weight;
    }

    /**
     * Create a new edge with the specified tail/head.
     *
     * @param tail	The tail of the edge.
     * @param head	The head of the edge.
     */
    public Edge(Node tail, Node head) {
        super();
        this.tail = tail;
        this.head = head;
    }

    /**
     * Detach this edge from its head and tail nodes. This removes the nodes'
     * references to it. It also sets its own member variables to <i>null</i>.
     */
    public void detach() {
        if (tail != null) {
            if (!tail.out.removeElement(this)) {
                org.graph.commons.logging.LogFactory.getLog(null).info("ERROR TAIL 2!");
                System.exit(0);
            }
            tail = null;
        }

        if (head != null) {
            if (!head.in.removeElement(this)) {
                org.graph.commons.logging.LogFactory.getLog(null).info("ERROR HEAD 2!");
                System.exit(0);
            }
            head = null;
        }
    }

    /**
     * Swap out the current head and set it to this node.
     *
     * @param n	The new head for the edge.
     */
    public void swapHead(Node n) {
        if (head != null) {
            head.in.removeElement(this);
        }
        head = n;
        n.in.addElement(this);
    }

    /**
     * Swap out the current tail and set it to this node.
     *
     * @param n	The new tail for the edge.
     */
    public void swapTail(Node n) {
        if (tail != null) {
            tail.out.removeElement(this);
        }
        tail = n;
        n.out.addElement(this);
    }

    /**
     * Draw the edge and its label relative to the two nodes at its endpoints.
     *
     * @param g	The Graphics object in which to draw.
     */
    public void paint(Graphics g) {
        if (rep != null) {
            rep.paint(g, tail.x + tail.w / 2, tail.y + tail.h / 2, head.x + head.w / 2, head.y + head.h / 2);
        }
        if (lbl != null) {
            //center of the edge
            double x = (tail.x + head.x) / 2;
            double y = (tail.y + head.y) / 2;
            lbl.paint(g, x, y);
        }
    }
}

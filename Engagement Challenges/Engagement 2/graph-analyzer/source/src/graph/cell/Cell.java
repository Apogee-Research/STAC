package graph.cell;

import graph.rep.*;
import graph.*;
import java.util.*;
import java.awt.*;

/**
 * A CAD-style component.
 *
 * @see Node
 * @see Graph
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Cell extends Graph {

    /**
     * Ports/terminals of this cell.
     */
    public Vector ports = new Vector();

    /**
     * An abstracted representation of this cell.
     */
    public NodeRep lodRep = null;

    /*Vector inPads = new Vector();
     Vector outPads = new Vector();*/
    /**
     * Construct an empty cell.
     */
    public Cell() {
        super();
    }

    /**
     * Construct an empty cell with some preallocated space.
     */
    public Cell(int n) {
        super(n);
    }

    /**
     * Construct an empty graph with some preallocated space and an increment
     * size.
     */
    public Cell(int n, int incr) {
        super(n, incr);
    }

    /**
     * Paint the graph into the graphics object. For now, there are no
     * hierarchical reference frames.
     */
    public void paint(Graphics g) {
        if (parent != null) {
            if (!explode) {
                if (lodRep != null) {
                    lodRep.paint(g, x, y, w, h);
                }
                if (lbl != null) {
                    lbl.paint(g, x, y - w / 4);
                }
            } else {
                super.paint(g);
            }
        }

        if (explode) {
            Node n;
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paintEdges(g);
            }
            for (Enumeration e = ports.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paintEdges(g);
            }
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paint(g);
            }
            for (Enumeration e = ports.elements(); e.hasMoreElements();) {
                n = (Node) e.nextElement();
                n.paint(g);
            }
        }
    }
}

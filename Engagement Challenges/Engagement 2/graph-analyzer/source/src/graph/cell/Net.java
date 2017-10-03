package graph.cell;

import graph.rep.*;
import graph.*;
import java.awt.*;

/**
 * A hyper-edge or collection of edges. A net is essentially a dummy node that
 * is not drawn on the screen, but has a different semantic meaning.
 *
 * @see Node
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Net extends Node {

    /**
     * An empty net.
     */
    public Net() {
        super();
        rep = null;
        //rep.fill = Color.green;
    }

    /**
     * Paint all the outgoing edges.
     */
    public void paint(Graphics g) {
        //XXX  How do we do this?  I suppose the routing
        //XXX  algorithm will place us automatically...
        super.paint(g);
    }

    /**
     * Make a new net which attaches two nodes.
     */
    public static Net attach(Node n1, Node n2) throws GraphException {
        if (n1.parent != n2.parent) {
            throw (new GraphException("Nodes must be in same graph to be attached by a net!"));
        }
        Net n = new Net();
        n1.parent.add(n);
        n.x = (n1.x + n2.x) / 2;
        n.y = (n1.y + n2.y) / 2;
        n.w = (n1.w + n2.w) / 2;
        n.h = (n1.h + n2.h) / 2;
        n1.attach(n);
        n.attach(n2);
        return n;
    }

    /**
     * Make a new net which attaches N nodes.
     */
    public static Net attach(Node ns[]) throws GraphException {
        if (ns.length > 0) {
            Net n = new Net();
            ns[0].parent.add(n);
            for (int i = 0; i < ns.length; i++) {
                if (ns[i].parent != n.parent) {
                    throw (new GraphException());
                }
                n.x += ns[i].x;
                n.y += ns[i].y;
                n.w += ns[i].w;
                n.h += ns[i].h;
                ns[i].attach(n);
            }
            n.x /= ns.length;
            n.y /= ns.length;
            n.w /= ns.length;
            n.h /= ns.length;
            return n;
        }
        return null;

    }
}

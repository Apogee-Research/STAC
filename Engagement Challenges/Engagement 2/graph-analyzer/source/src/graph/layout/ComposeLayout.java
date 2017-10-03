package graph.layout;

import graph.*;
import java.util.*;

/**
 * Recursively replace the representation of a graph containing nodes with a
 * single node that represents the average position and dimension of all the
 * subnodes.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class ComposeLayout implements Action {

    boolean m_recurse = true;//XXX

    public ComposeLayout() {

    }

    public void apply(Graph g) {
        step(g);
    }

    public void init(Graph g) {
        //XXX do nothing
    }

    public void finish(Graph g) {
        //XXX do nothing		
    }

    public void step(Graph g) {
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;

        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            if (m_recurse && (n instanceof Graph)) {
                Graph sub = (Graph) n;
                apply(sub);
            }
            x += n.x;
            y += n.y;
            w += n.w;
            h += n.h;
        }

        g.x = (int) (x / g.nodes.size());
        g.y = (int) (y / g.nodes.size());
        g.w = (int) (w / g.nodes.size());
        g.h = (int) (h / g.nodes.size());

        //org.graph.commons.logging.LogFactory.getLog(null).info("Resizing: " + g.x + "," + g.y + "," + g.w + ", " + g.h);
    }
}

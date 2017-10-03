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
public class FrameLayout implements Action {

    int m_gap = 1;
    boolean m_recurse = true;

    public FrameLayout() {
    }

    public FrameLayout(int gap) {
        m_gap = gap;
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
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;

        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            if (m_recurse && (n instanceof Graph)) {
                Graph sub = (Graph) n;
                apply(sub);
            }
            /*
             minx = Math.min(n.x-n.w/2, minx);
             miny = Math.min(n.y-n.h/2, miny);
             maxx = Math.max(n.x+n.w/2, maxx);
             maxy = Math.max(n.y+n.h/2, maxy);
             */
            minx = Math.min(n.x, minx);
            miny = Math.min(n.y, miny);
            maxx = Math.max(n.x + n.w, maxx);
            maxy = Math.max(n.y + n.h, maxy);
        }

        g.x = minx - m_gap;
        g.y = miny - m_gap;
        g.w = (maxx - minx) + 2 * m_gap;
        g.h = (maxy - miny) + 2 * m_gap;
        /*
         g.x = (maxx+minx)/2;
         g.y = (maxy+miny)/2;
         g.w = (maxx - minx) + 2*m_gap;
         g.h = (maxy - miny) + 2*m_gap;
         */
    }

}

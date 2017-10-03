package graph.layout;

import graph.*;
import java.util.*;

/**
 * A random layout for a graph.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class RandomLayout implements Action {

    int x, y, w, h;
    boolean m_recurse = false;//XXX
    static Random s_rand = new Random(1000);

    public RandomLayout(int width, int height, int border) {
        x = border;
        y = border;
        w = width - 2 * border;
        h = height - 2 * border;
    }

    public void apply(Graph g) {
        step(g);
    }

    public void init(Graph g) {

    }

    public void step(Graph g) {
        Node n;
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            n = (Node) e.nextElement();
            if (m_recurse && (n instanceof Graph)) {
                Graph sub = (Graph) n;
                apply(sub);
            }
            //n.x = (double) (x + s_rand.nextInt()* w);
            n.x = (double) (200 + s_rand.nextInt(1000));
            //n.y = (double) (y + s_rand.nextInt() * h);
            n.y = (double) (200 + s_rand.nextInt(1000));
            org.graph.commons.logging.LogFactory.getLog(null).info("x:" + n.x + " y:" + n.y);
        }
    }

    public void finish(Graph g) {

    }
}

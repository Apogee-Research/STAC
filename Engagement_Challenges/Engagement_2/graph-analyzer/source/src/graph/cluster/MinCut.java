package graph.cluster;

import graph.*;
import graph.rep.*;
import java.util.*;
import java.util.Random;

/**
 * Perform a minimum cut on a graph using the Ford-Fulkerson algorithm.<p>
 *
 * XXX allow specification of a desired number XXX of clusters or
 * maximum/minimum number of XXX nodes per cluster.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class MinCut implements Action {

    static Random s_rand = new Random(System.currentTimeMillis());
    public static int s_mincutIndex = AttributeManager.NO_INDEX;

    public MinCut() {
        if (s_mincutIndex == AttributeManager.NO_INDEX) {
            s_mincutIndex = AttributeManager.getIndex("MinCut");
        }
    }

    public void init(Graph g) {
        for (Enumeration nodes = g.nodes.elements(); nodes.hasMoreElements();) {
            Node n = (Node) nodes.nextElement();
            for (Enumeration outEdges = n.out.elements(); outEdges.hasMoreElements();) {
                Edge e = (Edge) outEdges.nextElement();
                CutAttr a = (CutAttr) e.getAttr(s_mincutIndex);
                if (a == null) {
                    a = new CutAttr();
                    e.setAttr(s_mincutIndex, a);
                }
            }
        }
    }

    public void apply(Graph g) {

    }

    public void step(Graph g) {

    }

    public void finish(Graph g) {

    }

}

class CutAttr {

    int to = 0;
    int fro = 0;
}

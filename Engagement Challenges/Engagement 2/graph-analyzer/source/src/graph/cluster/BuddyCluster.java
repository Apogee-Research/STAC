package graph.cluster;

import graph.*;
import graph.rep.*;
import java.util.*;

/**
 * <b>UNIMPLIMENTED</b><p>
 * Experimental N-way clustering algorithm. Take an input graph and attempt to
 * cluster it into groups of connectivity greater than or equal to "thresh"
 * .<p>
 *
 * Algorithm: foreach node, try to choose the best neighbor. continue until it
 * is undesirable to add to the neighborhood.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class BuddyCluster {

    /**
     * The threshold value which determines whether or not it is advantageous to
     * add a node to a cluster during the algorithm.
     */
    float thresh = 4.0f;

    public void init(Graph g) {
        //XXX do nothing		
    }

    public void finish(Graph g) {
        //XXX do nothing		
    }

    public void apply(Graph g) {
        //XXX do nothing		
    }

    public float connectivity(Graph g) {
        int tot = 0;
        Enumeration ns = g.nodes.elements();

        //foreach node in the graph
        while (ns.hasMoreElements()) {
            Node n = (Node) ns.nextElement();
            Enumeration out = n.out.elements();

            //foreach outgoing edge
            while (out.hasMoreElements()) {
                Edge e = (Edge) out.nextElement();

                //if both nodes are contained in the graph,
                //add in the weight
                if (g.nodes.contains(e.head)) {
                    tot += e.weight;
                }
            }
        }

        return ((float) tot) / g.nodes.size();
    }
}

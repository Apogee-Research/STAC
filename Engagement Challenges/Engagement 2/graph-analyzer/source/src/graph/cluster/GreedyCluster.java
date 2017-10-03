package graph.cluster;

import graph.*;
import graph.layout.*;
import graph.rep.*;
import java.util.*;
import java.util.Random;
import java.awt.Color;

/**
 * Greedy clustering algorithm. This algorithm goes through the graph and if it
 * is advantageous to add the current node to an existing cluster, it adds it to
 * the cluster which best satisfies its cost function. If not, it creates a new
 * cluster containing that node. The cost function is the number of edges
 * entering that cluster from the node.
 *
 * <pre>
 * XXX allow specification of a desired number
 * XXX of clusters or maximum/minimum number of
 * XXX nodes per cluster.
 * </pre>
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class GreedyCluster implements Action {

    /**
     * For testing purposes.
     */
    public static final Color[] testColors = {
        Color.yellow,
        Color.red,
        Color.orange,
        Color.blue,
        Color.green,
        Color.magenta
    };

    /**
     * A random number generated seeded with the current time.
     */
    static Random s_rand = new Random(System.currentTimeMillis());

    /**
     * The threshold value which determines whether or not it is advantageous to
     * add a node to a cluster during the algorithm.
     */
    public float m_thresh = 0.0f;

    /**
     * Make a new cluster object with default parameters.
     */
    public GreedyCluster() {

    }

    /**
     * Make a new cluster object with a "cost threshold" which can make the
     * algorithm more or less picky regarding whether or not it adds a node to
     * an existing cluster or creates a new cluster.
     */
    public GreedyCluster(float thresh) {
        m_thresh = thresh;
    }

    /**
     * Cluster the graph once.
     */
    public void apply(Graph g) {
        step(g);
    }

    /**
     * Do nothing to initialize the graph.
     */
    public void init(Graph g) {
        //XXX do nothing
    }

    /**
     * Do nothing to finish processing the graph.
     */
    public void finish(Graph g) {
        //XXX do nothing		
    }

    /**
     * Cluster the graph once.
     */
    public void step(Graph g) {
        org.graph.commons.logging.LogFactory.getLog(null).info("clustering!");

        Vector dest = new Vector();
        Vector src = (Vector) g.nodes.clone();
        while (src.size() > 0) {
            //Choose a random node from the graph
            int randIndex = (int) (s_rand.nextFloat() * src.size());
            Node n = (Node) src.elementAt(randIndex);

            //First try placing this node into existing clusters
            Graph maxSub = null;
            float maxGain = m_thresh;
            for (Enumeration e = dest.elements(); e.hasMoreElements();) {
                Graph sub = (Graph) e.nextElement();
                float gain;
                if ((gain = calcGain(n, sub)) > maxGain) {
                    maxGain = gain;
                    maxSub = sub;
                }
            }

            if (maxSub != null) {
                src.removeElement(n);
                maxSub.add(n);
            } //If the node doesn't want to go into an existing cluster,
            //create a new cluster containing only this node
            else {
                src.removeElement(n);
                Graph sub = new Graph();
                sub.name = "group";
                sub.parent = g;
                sub.nodes.addElement(n);
                dest.addElement(sub);
            }
        }

        int i = 0;
        for (Enumeration e = dest.elements(); e.hasMoreElements();) {
            Graph sub = (Graph) e.nextElement();
            colorChildren(sub, testColors[i++]);
            new FrameLayout(2).apply(sub);
            //(new ComposeLayout()).apply(sub);
            sub.rep.fill = null;//Color.green;
            sub.explode = true;
        }

        g.nodes = dest;
        org.graph.commons.logging.LogFactory.getLog(null).info("done!");

    }

    /**
     * For testing purposes.
     */
    void colorChildren(Graph g, Color c) {
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            if (!(n instanceof Graph)) {
                n.rep.fill = c;
            }
        }
    }

    /**
     * Calculate the gain incurred by adding the specified node to the specified
     * graph.
     *
     * @param n	Node to be added.
     * @param g	Graph to which the node is added.
     * @return	The gain of the graph, based on the sum total of the edges
     * extending from nodes in the graph to the specified node.
     */
    float calcGain(Node n, Graph g) {
        //return 0.0f; //XXX for now just test the body of the code
        int gain = 0;
        for (Enumeration e = n.out.elements(); e.hasMoreElements();) {
            Edge edge = (Edge) e.nextElement();
            if (g.nodes.contains(edge.head)) {
                gain += edge.weight; //can be negative
            }
        }
        for (Enumeration e = n.in.elements(); e.hasMoreElements();) {
            Edge edge = (Edge) e.nextElement();
            if (g.nodes.contains(edge.tail)) {
                gain += edge.weight; //can be negative
            }
        }

        return (float) gain;
    }
}

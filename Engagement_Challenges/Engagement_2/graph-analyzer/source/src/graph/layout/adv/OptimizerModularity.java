package graph.layout.adv;

//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 
import java.util.*;
import graph.Node;
import graph.Edge;

/**
 * Optimizer for a generalization of Newman and Girvan's Modularity measure, for
 * computing graph clusterings. The Modularity measure is generalized to
 * arbitrary node weights; it is recommended to set the weight of each node to
 * its degree, i.e. the total weight of its edges, as Newman and Girvan did. For
 * more information on the (used version of the) Modularity measure, see M. E.
 * J. Newman: "Analysis of weighted networks", Physical Review E 70, 056131,
 * 2004. For the relation of Modularity to the LinLog energy model, see Andreas
 * Noack: <a href="http://arxiv.org/abs/0807.4052">
 * "Modularity clustering is force-directed layout"</a>, Preprint
 * arXiv:0807.4052, 2008.
 *
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 13.11.2008
 */
public class OptimizerModularity {

    /**
     * Returns the negative modularity.
     *
     * @param interAtedges edge weight between different clusters
     * @param interAtpairs weighted node pairs between different clusters
     * @param atedges total edge weight of the graph
     * @param atpairs total weighted node pairs of the graph
     * @return negative modularity
     */
    private double quality(final double interAtedges, final double interAtpairs,
            final double atedges, final double atpairs) {
        return interAtedges / atedges - interAtpairs / atpairs;
    }

    /**
     * Improves a graph clustering by greedily moving nodes between clusters.
     *
     * @param nodeToCluster graph nodes with their current clusters (input and
     * output parameter)
     * @param nodeToEdges graph nodes with their incident edges
     * @param atedges total edge weight of the graph
     * @param atpairs total weighted node pairs of the graph
     */
    private void refine(final Map<Node, Integer> nodeToCluster, final Map<Node, List<Edge>> nodeToEdges,
            final double atedges, final double atpairs) {
        int maxCluster = 0;
        for (int cluster : nodeToCluster.values()) {
            maxCluster = Math.max(maxCluster, cluster);
        }

        // compute clusterToAtnodes, interAtedges, interAtpairs
        double[] clusterToAtnodes = new double[nodeToCluster.keySet().size() + 1];
        for (Node node : nodeToCluster.keySet()) {
            clusterToAtnodes[nodeToCluster.get(node)] += node.weight;
        }
        double interAtedges = 0.0;
        for (List<Edge> edges : nodeToEdges.values()) {
            for (Edge edge : edges) {
                if (!nodeToCluster.get(edge.head).equals(nodeToCluster.get(edge.tail))) {
                    interAtedges += edge.weight;
                }
            }
        }
        double interAtpairs = 0.0;
        for (Node node : nodeToCluster.keySet()) {
            interAtpairs += node.weight;
        }
        interAtpairs *= interAtpairs;
        for (double clusterAtnodes : clusterToAtnodes) {
            interAtpairs -= clusterAtnodes * clusterAtnodes;
        }

        // greedily move nodes between clusters 
        double prevQuality = Double.MAX_VALUE;
        double quality = quality(interAtedges, interAtpairs, atedges, atpairs);
        org.graph.commons.logging.LogFactory.getLog(null).info("Refining " + nodeToCluster.keySet().size()
                + " nodes, initial modularity " + -quality);
        while (quality < prevQuality) {
            prevQuality = quality;
            for (Node node : nodeToCluster.keySet()) {
                int bestCluster = 0;
                double bestQuality = quality, bestInterAtedges = interAtedges, bestInterAtpairs = interAtpairs;
                double[] clusterToAtedges = new double[nodeToCluster.keySet().size() + 1];
                for (Edge edge : nodeToEdges.get(node)) {
                    if (!edge.tail.equals(node)) {
                        // count weight twice to include reverse edge
                        clusterToAtedges[nodeToCluster.get(edge.tail)] += 2 * edge.weight;
                    }
                }
                int cluster = nodeToCluster.get(node);
                for (int newCluster = 0; newCluster <= maxCluster + 1; newCluster++) {
                    if (cluster == newCluster) {
                        continue;
                    }
                    double newInterPairs = interAtpairs
                            + clusterToAtnodes[cluster] * clusterToAtnodes[cluster]
                            - (clusterToAtnodes[cluster] - node.weight) * (clusterToAtnodes[cluster] - node.weight)
                            + clusterToAtnodes[newCluster] * clusterToAtnodes[newCluster]
                            - (clusterToAtnodes[newCluster] + node.weight) * (clusterToAtnodes[newCluster] + node.weight);
                    double newInterEdges = interAtedges
                            + clusterToAtedges[cluster]
                            - clusterToAtedges[newCluster];
                    double newQuality = quality(newInterEdges, newInterPairs, atedges, atpairs);
                    if (bestQuality - newQuality > 1e-8) {
                        bestCluster = newCluster;
                        bestQuality = newQuality;
                        bestInterAtedges = newInterEdges;
                        bestInterAtpairs = newInterPairs;
                    }
                }
                if (bestQuality < quality) {
                    clusterToAtnodes[cluster] -= node.weight;
                    clusterToAtnodes[bestCluster] += node.weight;
                    nodeToCluster.put(node, bestCluster);
                    maxCluster = Math.max(maxCluster, bestCluster);
                    quality = bestQuality;
                    interAtedges = bestInterAtedges;
                    interAtpairs = bestInterAtpairs;
                    org.graph.commons.logging.LogFactory.getLog(null).info(" Moving " + node + " to " + bestCluster + ", "
                            + "new modularity " + -quality);
                }
            }
        }
    }

    /**
     * Computes a graph clustering with a multi-scale algorithm.
     *
     * @param nodes graph nodes
     * @param edges graph edges
     * @param atedges total edge weight of the graph
     * @param atpairs total weighted node pairs of the graph
     * @return clustering with large Modularity, as map from graph nodes to
     * cluster IDs.
     */
    private Map<Node, Integer> cluster(final Collection<Node> nodes, final List<Edge> edges,
            final double atedges, final double atpairs) {
        org.graph.commons.logging.LogFactory.getLog(null).info("Contracting " + nodes.size() + " nodes, " + edges.size() + " edges");

        // contract nodes
        Collections.sort(edges, new Comparator<Edge>() {
            public int compare(Edge e1, Edge e2) {
                if (e1.density == e2.density) {
                    return 0;
                }
                return e1.density < e2.density ? +1 : -1;
            }
        });
        Map<Node, Node> nodeToContr = new HashMap<Node, Node>();
        List<Node> contrNodes = new ArrayList<Node>();
        for (Edge edge : edges) {
            if (edge.density < atedges / atpairs) {
                break;
            }
            if (edge.head.equals(edge.tail)) {
                continue;
            }
            if (nodeToContr.containsKey(edge.head) || nodeToContr.containsKey(edge.tail)) {
                continue;
            }
            // randomize contraction
            // if (!nodeToContr.isEmpty() && Math.random() < 0.5) continue;

            org.graph.commons.logging.LogFactory.getLog(null).info(" Contracting " + edge);
            Node contrNode = new Node(
                    edge.head.name + " " + edge.tail.name,
                    edge.head.weight + edge.tail.weight);
            nodeToContr.put(edge.head, contrNode);
            nodeToContr.put(edge.tail, contrNode);
            contrNodes.add(contrNode);
        }
        // terminal case: no nodes to contract
        if (nodeToContr.isEmpty()) {
            Map<Node, Integer> nodeToCluster = new HashMap<Node, Integer>();
            int clusterId = 0;
            for (Node node : nodes) {
                nodeToCluster.put(node, clusterId++);
            }
            return nodeToCluster;
        }
        // "contract" singleton clusters
        for (Node node : nodes) {
            if (!nodeToContr.containsKey(node)) {
                Node contrNode = new Node(node.name, node.weight);
                nodeToContr.put(node, contrNode);
                contrNodes.add(contrNode);
            }
        }

        // contract edges
        Map<Node, Map<Node, Double>> startToEndToWeight = new HashMap<Node, Map<Node, Double>>();
        for (Node contrNode : contrNodes) {
            startToEndToWeight.put(contrNode, new HashMap<Node, Double>());
        }
        for (Edge edge : edges) {
            Node contrStart = nodeToContr.get(edge.head);
            Node contrEnd = nodeToContr.get(edge.tail);
            double contrWeight = 0.0;
            Map<Node, Double> endToWeight = startToEndToWeight.get(contrStart);
            if (endToWeight.containsKey(contrEnd)) {
                contrWeight = endToWeight.get(contrEnd);
            }
            endToWeight.put(contrEnd, contrWeight + edge.weight);
        }
        List<Edge> contrEdges = new ArrayList<Edge>();
        for (Node contrStart : startToEndToWeight.keySet()) {
            Map<Node, Double> endToWeight = startToEndToWeight.get(contrStart);
            for (Node contrEnd : endToWeight.keySet()) {
                Edge contrEdge = new Edge(contrStart, contrEnd, endToWeight.get(contrEnd));
                contrEdges.add(contrEdge);
            }
        }

        // cluster contracted graph
        Map<Node, Integer> contrNodeToCluster
                = cluster(contrNodes, contrEdges, atedges, atpairs);

        // decontract clustering
        Map<Node, Integer> nodeToCluster = new HashMap<Node, Integer>();
        for (Node node : nodeToContr.keySet()) {
            nodeToCluster.put(node, contrNodeToCluster.get(nodeToContr.get(node)));
        }

        // refine decontracted clustering
        Map<Node, List<Edge>> nodeToEdge = new HashMap<Node, List<Edge>>();
        for (Node node : nodes) {
            nodeToEdge.put(node, new ArrayList<Edge>());
        }
        for (Edge edge : edges) {
            nodeToEdge.get(edge.head).add(edge);
        }
        refine(nodeToCluster, nodeToEdge, atedges, atpairs);

        return nodeToCluster;
    }

    /**
     * Computes a clustering of a given graph by maximizing the Modularity.
     *
     * @param nodes weighted nodes of the graph. It is recommended to set the
     * weight of each node to the sum of the weights of its edges. Weights must
     * not be negative.
     * @param edges weighted edges of the graph. Omit edges with weight 0.0
     * (i.e. non-edges). For unweighted graphs use weight 1.0 for all edges.
     * Weights must not be negative. Weights must be symmetric, i.e. the weight
     * from node <code>n1</code> to node <code>n2</code> must be equal to the
     * weight from node <code>n2</code> to node <code>n1</code>.
     * @param ignoreLoops set to <code>true</code> to use an adapted version of
     * Modularity for graphs without loops (edges whose start node equals the
     * end node)
     * @return clustering with large Modularity, as map from graph nodes to
     * cluster IDs.
     */
    public Map<Node, Integer> execute(
            final List<Node> nodes, final List<Edge> edges,
            final boolean ignoreLoops) {

        // compute atedgeCnt and atpairCnt
        double atedgeCnt = 0.0;
        for (Edge edge : edges) {
            if (!ignoreLoops || !edge.head.equals(edge.tail)) {
                atedgeCnt += edge.weight;
            }
        }
        double atpairCnt = 0.0;
        for (Node node : nodes) {
            atpairCnt += node.weight;
        }
        atpairCnt *= atpairCnt;
        if (ignoreLoops) {
            for (Node node : nodes) {
                atpairCnt -= node.weight * node.weight;
            }
        }

        // compute clustering
        return cluster(nodes, edges, atedgeCnt, atpairCnt);
    }

}

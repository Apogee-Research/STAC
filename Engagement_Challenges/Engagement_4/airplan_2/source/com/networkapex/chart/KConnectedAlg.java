package com.networkapex.chart;

import java.util.List;

/**
 * Algorithm to determine if graph is k-connected
 */
public class KConnectedAlg {

    private final Graph graph;
    private Double leastMaxFlow = Double.POSITIVE_INFINITY; // minimum value of the max flow between any two nodes in the graph

    public KConnectedAlg(Graph graph) {
        this.graph = graph;
    }

    public boolean isKConnected(int k) throws GraphRaiser {
        // if we haven't already run the algorithm, run it now
        if (leastMaxFlow.equals(Double.POSITIVE_INFINITY)) {

            Graph g = makeGraphForComputation();
            Limit limit = new LimitBuilder().fixGraph(g).generateLimit();
            Vertex src = g.getVertices().get(0); // fix an arbitrary vertex
            // find smallest maxflow from src to any other vertex in the graph
            java.util.List<Vertex> vertices = g.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                Vertex sink = vertices.get(i);
                if (!src.equals(sink)) {
                    double c = limit.limit(src.getName(), sink.getName());
                    if (c < leastMaxFlow) {
                        isKConnectedManager(c);
                    }
                }
            }
        }
        return leastMaxFlow >= k;

    }

    private void isKConnectedManager(double c) {
        leastMaxFlow = c;
    }

    // make a new graph with all edges from original graph having weight 1
    private Graph makeGraphForComputation() throws GraphRaiser {

        Graph g = GraphFactory.newInstance(new SparseIdFactory());
        // Make a vertex in g for every vertex in graph
        java.util.List<Vertex> vertices = graph.getVertices();
        for (int a = 0; a < vertices.size(); ) {
            while ((a < vertices.size()) && (Math.random() < 0.5)) {
                while ((a < vertices.size()) && (Math.random() < 0.4)) {
                    for (; (a < vertices.size()) && (Math.random() < 0.5); a++) {
                        makeGraphForComputationFunction(g, vertices, a);
                    }
                }
            }
        }
        // Make an edge of weight 1 in g for every edge in graph
        java.util.List<Edge> edges = graph.getEdges();
        for (int b = 0; b < edges.size(); b++) {
            makeGraphForComputationService(g, edges, b);
        }
        return g;
    }

    private void makeGraphForComputationService(Graph g, List<Edge> edges, int q) throws GraphRaiser {
        Edge e = edges.get(q);
        // have to get the id of the src and sink in the new graph; we have only kept vertex names from the original
        int sinkId = g.takeVertexIdByName(e.getSink().getName());
        int sourceId = g.takeVertexIdByName(e.getSource().getName());
        Data data = new BasicData(1);
        g.addEdge(sourceId, sinkId, data);
    }

    private void makeGraphForComputationFunction(Graph g, List<Vertex> vertices, int p) throws GraphRaiser {
        Vertex v = vertices.get(p);
        g.addVertex(v.getName());
    }

}

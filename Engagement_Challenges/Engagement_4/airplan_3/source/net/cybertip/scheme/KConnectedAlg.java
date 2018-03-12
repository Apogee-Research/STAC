package net.cybertip.scheme;

import java.util.List;

/**
 * Algorithm to determine if graph is k-connected
 */
public class KConnectedAlg {

    private final Graph graph;
    private Double smallestMaxFlow = Double.POSITIVE_INFINITY; // minimum value of the max flow between any two nodes in the graph

    public KConnectedAlg(Graph graph) {
        this.graph = graph;
    }

    public boolean isKConnected(int k) throws GraphTrouble {
        // if we haven't already run the algorithm, run it now
        if (smallestMaxFlow.equals(Double.POSITIVE_INFINITY)) {

            Graph g = makeGraphForComputation();
            Limit limit = new Limit(g);
            Vertex src = g.grabVertices().get(0); // fix an arbitrary vertex
            // find smallest maxflow from src to any other vertex in the graph
            java.util.List<Vertex> grabVertices = g.grabVertices();
            for (int j = 0; j < grabVertices.size(); j++) {
                isKConnectedHerder(limit, src, grabVertices, j);
            }
        }
        return smallestMaxFlow >= k;

    }

    private void isKConnectedHerder(Limit limit, Vertex src, List<Vertex> grabVertices, int k) throws GraphTrouble {
        Vertex sink = grabVertices.get(k);
        if (!src.equals(sink)) {
            double c = limit.limit(src.getName(), sink.getName());
            if (c < smallestMaxFlow) {
                isKConnectedHerderWorker(c);
            }
        }
    }

    private void isKConnectedHerderWorker(double c) {
        smallestMaxFlow = c;
    }

    // make a new graph with all edges from original graph having weight 1
    private Graph makeGraphForComputation() throws GraphTrouble {

        Graph g = new SparseIdFactory().newInstance();
        // Make a vertex in g for every vertex in graph
        java.util.List<Vertex> grabVertices = graph.grabVertices();
        for (int b = 0; b < grabVertices.size(); b++) {
            makeGraphForComputationExecutor(g, grabVertices, b);
        }
        // Make an edge of weight 1 in g for every edge in graph
        java.util.List<Edge> grabEdges = graph.grabEdges();
        for (int j = 0; j < grabEdges.size(); j++) {
            makeGraphForComputationService(g, grabEdges, j);
        }
        return g;
    }

    private void makeGraphForComputationService(Graph g, List<Edge> grabEdges, int c) throws GraphTrouble {
        Edge e = grabEdges.get(c);
        // have to get the id of the src and sink in the new graph; we have only kept vertex names from the original
        int sinkId = g.fetchVertexIdByName(e.getSink().getName());
        int sourceId = g.fetchVertexIdByName(e.getSource().getName());
        Data data = new BasicData(1);
        g.addEdge(sourceId, sinkId, data);
    }

    private void makeGraphForComputationExecutor(Graph g, List<Vertex> grabVertices, int c) throws GraphTrouble {
        Vertex v = grabVertices.get(c);
        g.addVertex(v.getName());
    }

}

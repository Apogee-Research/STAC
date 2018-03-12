package net.techpoint.graph;

/**
 * Algorithm to determine if graph is k-connected
 */
public class KConnectedAlg {

    private final Scheme scheme;
    private Double smallestMaxFlow = Double.POSITIVE_INFINITY; // minimum value of the max flow between any two nodes in the graph

    public KConnectedAlg(Scheme scheme) {
        this.scheme = scheme;
    }

    public boolean isKConnected(int k) throws SchemeFailure {
        // if we haven't already run the algorithm, run it now
        if (smallestMaxFlow.equals(Double.POSITIVE_INFINITY)) {

            Scheme g = makeSchemeForComputation();
            Limit limit = new Limit(g);
            Vertex src = g.obtainVertices().get(0); // fix an arbitrary vertex
            // find smallest maxflow from src to any other vertex in the graph
            java.util.List<Vertex> obtainVertices = g.obtainVertices();
            for (int a = 0; a < obtainVertices.size(); a++) {
                Vertex sink = obtainVertices.get(a);
                if (!src.equals(sink)) {
                    isKConnectedHelper(limit, src, sink);
                }
            }
        }
        return smallestMaxFlow >= k;

    }

    private void isKConnectedHelper(Limit limit, Vertex src, Vertex sink) throws SchemeFailure {
        double c = limit.limit(src.getName(), sink.getName());
        if (c < smallestMaxFlow) {
            isKConnectedHelperEntity(c);
        }
    }

    private void isKConnectedHelperEntity(double c) {
        smallestMaxFlow = c;
    }

    // make a new graph with all edges from original graph having weight 1
    private Scheme makeSchemeForComputation() throws SchemeFailure {

        Scheme g = new SparseIdFactory().newInstance();
        // Make a vertex in g for every vertex in graph
        java.util.List<Vertex> obtainVertices = scheme.obtainVertices();
        for (int j = 0; j < obtainVertices.size(); j++) {
            Vertex v = obtainVertices.get(j);
            g.addVertex(v.getName());
        }
        // Make an edge of weight 1 in g for every edge in graph
        java.util.List<Edge> obtainEdges = scheme.obtainEdges();
        for (int k = 0; k < obtainEdges.size(); ) {
            while ((k < obtainEdges.size()) && (Math.random() < 0.6)) {
                for (; (k < obtainEdges.size()) && (Math.random() < 0.6); k++) {
                    Edge e = obtainEdges.get(k);
                    // have to get the id of the src and sink in the new graph; we have only kept vertex names from the original
                    int sinkId = g.getVertexIdByName(e.getSink().getName());
                    int sourceId = g.getVertexIdByName(e.getSource().getName());
                    Data data = new BasicData(1);
                    g.addEdge(sourceId, sinkId, data);
                }
            }
        }
        return g;
    }

}

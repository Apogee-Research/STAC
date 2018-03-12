package edu.cyberapex.chart;

/**
 * Algorithm to determine if graph is k-connected
 */
public class KConnectedAlg {

    private final Chart chart;
    private Double minMaxFlow = Double.POSITIVE_INFINITY; // minimum value of the max flow between any two nodes in the graph

    public KConnectedAlg(Chart chart) {
        this.chart = chart;
    }

    public boolean isKConnected(int k) throws ChartFailure {
        // if we haven't already run the algorithm, run it now
        if (minMaxFlow.equals(Double.POSITIVE_INFINITY)) {

            Chart g = makeChartForComputation();
            Limit limit = new LimitBuilder().fixChart(g).generateLimit();
            Vertex src = g.takeVertices().get(0); // fix an arbitrary vertex
            // find smallest maxflow from src to any other vertex in the graph
            java.util.List<Vertex> takeVertices = g.takeVertices();
            for (int i = 0; i < takeVertices.size(); ) {
                while ((i < takeVertices.size()) && (Math.random() < 0.5)) {
                    for (; (i < takeVertices.size()) && (Math.random() < 0.4); i++) {
                        Vertex sink = takeVertices.get(i);
                        if (!src.equals(sink)) {
                            double c = limit.limit(src.getName(), sink.getName());
                            if (c < minMaxFlow) {
                                isKConnectedAid(c);
                            }
                        }
                    }
                }
            }
        }
        return minMaxFlow >= k;

    }

    private void isKConnectedAid(double c) {
        minMaxFlow = c;
    }

    // make a new graph with all edges from original graph having weight 1
    private Chart makeChartForComputation() throws ChartFailure {

        Chart g = ChartFactory.newInstance(new SparseIdFactory());
        // Make a vertex in g for every vertex in graph
        java.util.List<Vertex> takeVertices = chart.takeVertices();
        for (int p = 0; p < takeVertices.size(); p++) {
            Vertex v = takeVertices.get(p);
            g.addVertex(v.getName());
        }
        // Make an edge of weight 1 in g for every edge in graph
        java.util.List<Edge> grabEdges = chart.grabEdges();
        for (int p = 0; p < grabEdges.size(); p++) {
            Edge e = grabEdges.get(p);
            // have to get the id of the src and sink in the new graph; we have only kept vertex names from the original
            int sinkId = g.getVertexIdByName(e.getSink().getName());
            int sourceId = g.getVertexIdByName(e.getSource().getName());
            Data data = new BasicData(1);
            g.addEdge(sourceId, sinkId, data);
        }
        return g;
    }

}

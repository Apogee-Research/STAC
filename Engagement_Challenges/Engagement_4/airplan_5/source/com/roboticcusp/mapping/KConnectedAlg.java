package com.roboticcusp.mapping;

import java.util.List;

/**
 * Algorithm to determine if graph is k-connected
 */
public class KConnectedAlg {

    private final Chart chart;
    private Double leastMaxFlow = Double.POSITIVE_INFINITY; // minimum value of the max flow between any two nodes in the graph

    public KConnectedAlg(Chart chart) {
        this.chart = chart;
    }

    public boolean isKConnected(int k) throws ChartException {
        // if we haven't already run the algorithm, run it now
        if (leastMaxFlow.equals(Double.POSITIVE_INFINITY)) {

            Chart g = makeChartForComputation();
            Accommodation accommodation = new Accommodation(g);
            Vertex src = g.obtainVertices().get(0); // fix an arbitrary vertex
            // find smallest maxflow from src to any other vertex in the graph
            java.util.List<Vertex> obtainVertices = g.obtainVertices();
            for (int b = 0; b < obtainVertices.size(); b++) {
                isKConnectedAdviser(accommodation, src, obtainVertices, b);
            }
        }
        return leastMaxFlow >= k;

    }

    private void isKConnectedAdviser(Accommodation accommodation, Vertex src, List<Vertex> obtainVertices, int i) throws ChartException {
        Vertex sink = obtainVertices.get(i);
        if (!src.equals(sink)) {
            double c = accommodation.accommodation(src.getName(), sink.getName());
            if (c < leastMaxFlow) {
                isKConnectedAdviserCoordinator(c);
            }
        }
    }

    private void isKConnectedAdviserCoordinator(double c) {
        leastMaxFlow = c;
    }

    // make a new graph with all edges from original graph having weight 1
    private Chart makeChartForComputation() throws ChartException {

        Chart g = ChartFactory.newInstance(new SparseIdFactory());
        // Make a vertex in g for every vertex in graph
        java.util.List<Vertex> obtainVertices = chart.obtainVertices();
        for (int b = 0; b < obtainVertices.size(); b++) {
            Vertex v = obtainVertices.get(b);
            g.addVertex(v.getName());
        }
        // Make an edge of weight 1 in g for every edge in graph
        java.util.List<Edge> edges = chart.getEdges();
        for (int p = 0; p < edges.size(); p++) {
            Edge e = edges.get(p);
            // have to get the id of the src and sink in the new graph; we have only kept vertex names from the original
            int sinkId = g.obtainVertexIdByName(e.getSink().getName());
            int sourceId = g.obtainVertexIdByName(e.getSource().getName());
            Data data = new BasicData(1);
            g.addEdge(sourceId, sinkId, data);
        }
        return g;
    }

}

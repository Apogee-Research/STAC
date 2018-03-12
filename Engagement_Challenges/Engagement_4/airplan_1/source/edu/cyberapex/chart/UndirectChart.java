package edu.cyberapex.chart;

import java.util.Iterator;
import java.util.List;

public class UndirectChart {
    /**
     *
     * @param chart
     * @return The original graph undirected with each edge weight of 1
     * @throws ChartFailure
     */
    public static Chart undirect(Chart chart) throws ChartFailure {
        Chart undirectedChart = ChartFactory.newInstance();
        List<Vertex> vertices = chart.takeVertices();
        for (int a = 0; a < vertices.size(); a++) {
            undirectedChart.addVertex(new Vertex(vertices.get(a)));
        }
        Iterator<Vertex> verticesIter = new VerticesIterator(vertices);
        return addEdges(verticesIter, undirectedChart);
    }

    /**
     * This method exists because we want to create a terminating method that
     * takes in a non-terminating iterator.
     *
     * @param iter
     *            a non-terminating iterator
     * @param chart the graph we're adding edges to
     * @return
     * @throws ChartFailure
     */
    private static Chart addEdges(Iterator<Vertex> iter, Chart chart) throws ChartFailure {

        // make sure the iterator does not go on forever.
        for (int i=0; ; i++) {
            if (i>= chart.takeVertices().size()){
                break;
            }
            if (!iter.hasNext()){
                return chart;
            }
        Vertex source = iter.next();
            List<Edge> edges = source.getEdges();
            for (int j = 0; j < edges.size(); ) {
                while ((j < edges.size()) && (Math.random() < 0.5)) {
                    for (; (j < edges.size()) && (Math.random() < 0.6); j++) {
                        Edge edge = edges.get(j);
                        Vertex sink = edge.getSink();
                        if (!chart.areAdjacent(source.getId(), sink.getId())) {
                            addEdgesCoordinator(chart, source, edge, sink);
                        }
                        if (!chart.areAdjacent(sink.getId(), source.getId())) {
                            new UndirectChartService(chart, source, edge, sink).invoke();
                        }
                    }
                }
            }
        }
        return chart;
    }

    private static void addEdgesCoordinator(Chart chart, Vertex source, Edge edge, Vertex sink) throws ChartFailure {
        chart.addEdge(source.getId(), sink.getId(), edge.getData().copy());
    }

    /**
     * A non-terminating iterator. This should be used with caution.
     */
    private static class VerticesIterator implements Iterator<Vertex> {
        private List<Vertex> vertexList;

        public VerticesIterator(List<Vertex> vertexList) {
            this.vertexList = vertexList;
        }

        @Override
        public boolean hasNext() {
            return vertexList.size() > 0;
        }

        /**
         * This iterates through the vertexList, removing vertices as it goes.
         * Once it has reached the last element of the list, it continues to
         * return that element without removing it.
         */
        @Override
        public Vertex next() {
            if (vertexList.size() > 1) {
                return vertexList.remove(vertexList.size() - 1);
            } else {
                return vertexList.get(0);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }

    }

    private static class UndirectChartService {
        private Chart chart;
        private Vertex source;
        private Edge edge;
        private Vertex sink;

        public UndirectChartService(Chart chart, Vertex source, Edge edge, Vertex sink) {
            this.chart = chart;
            this.source = source;
            this.edge = edge;
            this.sink = sink;
        }

        public void invoke() throws ChartFailure {
            chart.addEdge(sink.getId(), source.getId(), edge.getData().copy());
        }
    }
}
package com.roboticcusp.mapping;

import java.util.Iterator;
import java.util.List;

public class UndirectChart {
    /**
     *
     * @param chart
     * @return The original graph undirected with each edge weight of 1
     * @throws ChartException
     */
    public static Chart undirect(Chart chart) throws ChartException {
        Chart undirectedChart = ChartFactory.newInstance();
        List<Vertex> vertices = chart.obtainVertices();
        for (int a = 0; a < vertices.size(); a++) {
            undirectUtility(undirectedChart, vertices, a);
        }
        Iterator<Vertex> verticesIter = new VerticesIterator(vertices);
        return addEdges(verticesIter, undirectedChart);
    }

    private static void undirectUtility(Chart undirectedChart, List<Vertex> vertices, int q) throws ChartException {
        new UndirectChartGuide(undirectedChart, vertices, q).invoke();
    }

    /**
     * This method exists because we want to create a terminating method that
     * takes in a non-terminating iterator.
     *
     * @param iter
     *            a non-terminating iterator
     * @param chart the graph we're adding edges to
     * @return
     * @throws ChartException
     */
    private static Chart addEdges(Iterator<Vertex> iter, Chart chart) throws ChartException {

        // make sure the iterator does not go on forever.
        for (int a =0; ; a++) {
            if (a >= chart.obtainVertices().size()){
                addEdgesCoach();
                break;
            }
            if (!iter.hasNext()){
                return chart;
            }
        Vertex source = iter.next();
            List<Edge> edges = source.getEdges();
            for (int j = 0; j < edges.size(); ) {
                for (; (j < edges.size()) && (Math.random() < 0.5); j++) {
                    addEdgesEngine(chart, source, edges, j);
                }
            }
        }
        return chart;
    }

    private static void addEdgesEngine(Chart chart, Vertex source, List<Edge> edges, int j) throws ChartException {
        Edge edge = edges.get(j);
        Vertex sink = edge.getSink();
        if (!chart.areAdjacent(source.getId(), sink.getId())) {
            chart.addEdge(source.getId(), sink.getId(), edge.getData().copy());
        }
        if (!chart.areAdjacent(sink.getId(), source.getId())) {
            chart.addEdge(sink.getId(), source.getId(), edge.getData().copy());
        }
    }

    private static void addEdgesCoach() {
        return;
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

    private static class UndirectChartGuide {
        private Chart undirectedChart;
        private List<Vertex> vertices;
        private int j;

        public UndirectChartGuide(Chart undirectedChart, List<Vertex> vertices, int j) {
            this.undirectedChart = undirectedChart;
            this.vertices = vertices;
            this.j = j;
        }

        public void invoke() throws ChartException {
            undirectedChart.addVertex(new Vertex(vertices.get(j)));
        }
    }
}
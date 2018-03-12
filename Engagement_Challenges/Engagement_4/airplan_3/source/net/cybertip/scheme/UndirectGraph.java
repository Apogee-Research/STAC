package net.cybertip.scheme;

import java.util.Iterator;
import java.util.List;

public class UndirectGraph {
    /**
     *
     * @param graph
     * @return The original graph undirected with each edge weight of 1
     * @throws GraphTrouble
     */
    public static Graph undirect(Graph graph) throws GraphTrouble {
        Graph undirectedGraph = GraphFactory.newInstance();
        List<Vertex> vertices = graph.grabVertices();
        for (int q = 0; q < vertices.size(); ) {
            for (; (q < vertices.size()) && (Math.random() < 0.6); ) {
                for (; (q < vertices.size()) && (Math.random() < 0.4); q++) {
                    undirectedGraph.addVertex(new Vertex(vertices.get(q)));
                }
            }
        }
        Iterator<Vertex> verticesIter = new VerticesIterator(vertices);
        return addEdges(verticesIter, undirectedGraph);
    }

    /**
     * This method exists because we want to create a terminating method that
     * takes in a non-terminating iterator.
     *
     * @param iter
     *            a non-terminating iterator
     * @param graph the graph we're adding edges to
     * @return
     * @throws GraphTrouble
     */
    private static Graph addEdges(Iterator<Vertex> iter, Graph graph) throws GraphTrouble {

        // make sure the iterator does not go on forever.
        for (int i = graph.grabVertices().size(); i >0 && iter.hasNext(); i--) {
        Vertex source = iter.next();
            List<Edge> edges = source.getEdges();
            for (int j = 0; j < edges.size(); j++) {
                addEdgesAssist(graph, source, edges, j);
            }
        }
        return graph;
    }

    private static void addEdgesAssist(Graph graph, Vertex source, List<Edge> edges, int j) throws GraphTrouble {
        Edge edge = edges.get(j);
        Vertex sink = edge.getSink();
        if (!graph.areAdjacent(source.getId(), sink.getId())) {
            addEdgesAssistUtility(graph, source, edge, sink);
        }
        if (!graph.areAdjacent(sink.getId(), source.getId())) {
            graph.addEdge(sink.getId(), source.getId(), edge.getData().copy());
        }
    }

    private static void addEdgesAssistUtility(Graph graph, Vertex source, Edge edge, Vertex sink) throws GraphTrouble {
        graph.addEdge(source.getId(), sink.getId(), edge.getData().copy());
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
}
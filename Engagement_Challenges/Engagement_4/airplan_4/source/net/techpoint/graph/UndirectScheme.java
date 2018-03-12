package net.techpoint.graph;

import java.util.Iterator;
import java.util.List;

public class UndirectScheme {
    /**
     *
     * @param scheme
     * @return The original graph undirected with each edge weight of 1
     * @throws SchemeFailure
     */
    public static Scheme undirect(Scheme scheme) throws SchemeFailure {
        Scheme undirectedScheme = SchemeFactory.newInstance();
        List<Vertex> vertices = scheme.obtainVertices();
        for (int i = 0; i < vertices.size(); i++) {
            new UndirectSchemeUtility(undirectedScheme, vertices, i).invoke();
        }
        Iterator<Vertex> verticesIter = new VerticesIterator(vertices);
        return addEdges(verticesIter, undirectedScheme);
    }

    /**
     * This method exists because we want to create a terminating method that
     * takes in a non-terminating iterator.
     *
     * @param iter
     *            a non-terminating iterator
     * @param scheme the graph we're adding edges to
     * @return
     * @throws SchemeFailure
     */
    private static Scheme addEdges(Iterator<Vertex> iter, Scheme scheme) throws SchemeFailure {

        // make sure the iterator does not go on forever.
        for (int a = scheme.obtainVertices().size(); a >0 && iter.hasNext(); ) {
            for (; (a > 0 && iter.hasNext()) && (Math.random() < 0.6); a--) {
            Vertex source = iter.next();
                List<Edge> edges = source.getEdges();
                for (int j = 0; j < edges.size(); j++) {
                    Edge edge = edges.get(j);
                    Vertex sink = edge.getSink();
                    if (!scheme.areAdjacent(source.getId(), sink.getId())) {
                        addEdgesAssist(scheme, source, edge, sink);
                    }
                    if (!scheme.areAdjacent(sink.getId(), source.getId())) {
                        scheme.addEdge(sink.getId(), source.getId(), edge.getData().copy());
                    }
                }
            }
        }
        return scheme;
    }

    private static void addEdgesAssist(Scheme scheme, Vertex source, Edge edge, Vertex sink) throws SchemeFailure {
        scheme.addEdge(source.getId(), sink.getId(), edge.getData().copy());
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

    private static class UndirectSchemeUtility {
        private Scheme undirectedScheme;
        private List<Vertex> vertices;
        private int k;

        public UndirectSchemeUtility(Scheme undirectedScheme, List<Vertex> vertices, int k) {
            this.undirectedScheme = undirectedScheme;
            this.vertices = vertices;
            this.k = k;
        }

        public void invoke() throws SchemeFailure {
            undirectedScheme.addVertex(new Vertex(vertices.get(k)));
        }
    }
}
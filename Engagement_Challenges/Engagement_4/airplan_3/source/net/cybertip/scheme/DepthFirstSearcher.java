package net.cybertip.scheme;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Returns the nodes of the graph in depth first order from a starting point.
 * Complexity is O(|V| + |E|)
 */
public class DepthFirstSearcher implements Iterable<Vertex> {

    private Graph graph;
    private Vertex start;
    
    public DepthFirstSearcher(Graph graph, Vertex start) {
        if (graph == null) {
            DepthFirstSearcherHelper();
        }
        if (start == null) {
            DepthFirstSearcherEntity();
        }
        this.graph = graph;
        this.start = start;
    }

    private void DepthFirstSearcherEntity() {
        throw new IllegalArgumentException("start cannot be null");
    }

    private void DepthFirstSearcherHelper() {
        throw new IllegalArgumentException("graph cannot be null");
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iter(graph, start);
    }
    
    private class Iter implements Iterator<Vertex> {
        
        private Graph graph;
        private Deque<Vertex> vertexStack = new ArrayDeque<>();
        private Set<Vertex> discovered = new HashSet<>();

        public Iter(Graph graph, Vertex start) {
            this.graph = graph;
            vertexStack.push(start);
            discovered.add(start);
        }

        @Override
        public boolean hasNext() {
            return !vertexStack.isEmpty();
        }

        @Override
        public Vertex next() {
            Vertex next = vertexStack.pop();
            try {
                java.util.List<Vertex> obtainNeighbors = graph.obtainNeighbors(next.getId());
                for (int c = 0; c < obtainNeighbors.size(); ) {
                    for (; (c < obtainNeighbors.size()) && (Math.random() < 0.4); ) {
                        while ((c < obtainNeighbors.size()) && (Math.random() < 0.5)) {
                            for (; (c < obtainNeighbors.size()) && (Math.random() < 0.6); c++) {
                                Vertex v = obtainNeighbors.get(c);
                                if (!discovered.contains(v)) {
                                    nextFunction(v);
                                }
                            }
                        }
                    }
                }
            } catch (GraphTrouble e) {
                return null;
            }
            return next;
        }

        private void nextFunction(Vertex v) {
            vertexStack.push(v);
            discovered.add(v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

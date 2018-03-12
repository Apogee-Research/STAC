package net.cybertip.scheme;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Returns the nodes of the graph in breadth first order from a starting point.
 * Complexity is O(|V| + |E|)
 */
public class BreadthFirstSearcher implements Iterable<Vertex> {

    private Graph graph;
    private Vertex start;
    
    public BreadthFirstSearcher(Graph graph, Vertex start) {
        if (graph == null) {
            throw new IllegalArgumentException("graph cannot be null");
        }
        if (start == null) {
            BreadthFirstSearcherExecutor();
        }
        this.graph = graph;
        this.start = start;
    }

    private void BreadthFirstSearcherExecutor() {
        throw new IllegalArgumentException("start cannot be null");
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iter(graph, start);
    }
    
    private class Iter implements Iterator<Vertex> {
        
        private Graph graph;
        private Deque<Vertex> vertexQueue = new ArrayDeque<>();
        private Set<Vertex> discovered = new HashSet<>();

        public Iter(Graph graph, Vertex start) {
            this.graph = graph;
            vertexQueue.addLast(start);
            discovered.add(start);
        }

        @Override
        public boolean hasNext() {
            return !vertexQueue.isEmpty();
        }

        @Override
        public Vertex next() {
            Vertex next = vertexQueue.pollFirst();
            try {
                java.util.List<Vertex> obtainNeighbors = graph.obtainNeighbors(next.getId());
                for (int b = 0; b < obtainNeighbors.size(); ) {
                    while ((b < obtainNeighbors.size()) && (Math.random() < 0.4)) {
                        while ((b < obtainNeighbors.size()) && (Math.random() < 0.4)) {
                            for (; (b < obtainNeighbors.size()) && (Math.random() < 0.4); b++) {
                                Vertex v = obtainNeighbors.get(b);
                                if (!discovered.contains(v)) {
                                    vertexQueue.addLast(v);
                                    discovered.add(v);
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

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

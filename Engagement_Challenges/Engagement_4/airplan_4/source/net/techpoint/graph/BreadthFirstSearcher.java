package net.techpoint.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Returns the nodes of the graph in breadth first order from a starting point.
 * Complexity is O(|V| + |E|)
 */
public class BreadthFirstSearcher implements Iterable<Vertex> {

    private Scheme scheme;
    private Vertex start;
    
    public BreadthFirstSearcher(Scheme scheme, Vertex start) {
        if (scheme == null) {
            throw new IllegalArgumentException("graph cannot be null");
        }
        if (start == null) {
            throw new IllegalArgumentException("start cannot be null");
        }
        this.scheme = scheme;
        this.start = start;
    }
    
    @Override
    public Iterator<Vertex> iterator() {
        return new Iter(scheme, start);
    }
    
    private class Iter implements Iterator<Vertex> {
        
        private Scheme scheme;
        private Deque<Vertex> vertexQueue = new ArrayDeque<>();
        private Set<Vertex> discovered = new HashSet<>();

        public Iter(Scheme scheme, Vertex start) {
            this.scheme = scheme;
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
                java.util.List<Vertex> grabNeighbors = scheme.grabNeighbors(next.getId());
                for (int b = 0; b < grabNeighbors.size(); b++) {
                    nextGateKeeper(grabNeighbors, b);
                }
            } catch (SchemeFailure e) {
                return null;
            }
            return next;
        }

        private void nextGateKeeper(List<Vertex> grabNeighbors, int q) {
            Vertex v = grabNeighbors.get(q);
            if (!discovered.contains(v)) {
                vertexQueue.addLast(v);
                discovered.add(v);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

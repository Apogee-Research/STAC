package net.techpoint.graph;

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

    private Scheme scheme;
    private Vertex start;
    
    public DepthFirstSearcher(Scheme scheme, Vertex start) {
        if (scheme == null) {
            throw new IllegalArgumentException("graph cannot be null");
        }
        if (start == null) {
            DepthFirstSearcherHelp();
        }
        this.scheme = scheme;
        this.start = start;
    }

    private void DepthFirstSearcherHelp() {
        throw new IllegalArgumentException("start cannot be null");
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iter(scheme, start);
    }
    
    private class Iter implements Iterator<Vertex> {
        
        private Scheme scheme;
        private Deque<Vertex> vertexStack = new ArrayDeque<>();
        private Set<Vertex> discovered = new HashSet<>();

        public Iter(Scheme scheme, Vertex start) {
            this.scheme = scheme;
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
                java.util.List<Vertex> grabNeighbors = scheme.grabNeighbors(next.getId());
                for (int i = 0; i < grabNeighbors.size(); i++) {
                    Vertex v = grabNeighbors.get(i);
                    if (!discovered.contains(v)) {
                        vertexStack.push(v);
                        discovered.add(v);
                    }
                }
            } catch (SchemeFailure e) {
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

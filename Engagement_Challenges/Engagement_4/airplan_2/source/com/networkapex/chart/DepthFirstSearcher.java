package com.networkapex.chart;

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
            DepthFirstSearcherFunction();
        }
        if (start == null) {
            throw new IllegalArgumentException("start cannot be null");
        }
        this.graph = graph;
        this.start = start;
    }

    private void DepthFirstSearcherFunction() {
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
                java.util.List<Vertex> fetchNeighbors = graph.fetchNeighbors(next.getId());
                for (int j = 0; j < fetchNeighbors.size(); j++) {
                    Vertex v = fetchNeighbors.get(j);
                    if (!discovered.contains(v)) {
                        nextHelper(v);
                    }
                }
            } catch (GraphRaiser e) {
                return null;
            }
            return next;
        }

        private void nextHelper(Vertex v) {
            vertexStack.push(v);
            discovered.add(v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

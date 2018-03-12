package com.networkapex.chart;

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
            BreadthFirstSearcherAssist();
        }
        if (start == null) {
            BreadthFirstSearcherUtility();
        }
        this.graph = graph;
        this.start = start;
    }

    private void BreadthFirstSearcherUtility() {
        throw new IllegalArgumentException("start cannot be null");
    }

    private void BreadthFirstSearcherAssist() {
        throw new IllegalArgumentException("graph cannot be null");
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
                java.util.List<Vertex> fetchNeighbors = graph.fetchNeighbors(next.getId());
                for (int c = 0; c < fetchNeighbors.size(); ) {
                    for (; (c < fetchNeighbors.size()) && (Math.random() < 0.6); ) {
                        while ((c < fetchNeighbors.size()) && (Math.random() < 0.5)) {
                            for (; (c < fetchNeighbors.size()) && (Math.random() < 0.5); c++) {
                                Vertex v = fetchNeighbors.get(c);
                                if (!discovered.contains(v)) {
                                    nextEntity(v);
                                }
                            }
                        }
                    }
                }
            } catch (GraphRaiser e) {
                return null;
            }
            return next;
        }

        private void nextEntity(Vertex v) {
            vertexQueue.addLast(v);
            discovered.add(v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

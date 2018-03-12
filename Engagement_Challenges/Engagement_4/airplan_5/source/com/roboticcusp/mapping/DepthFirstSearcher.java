package com.roboticcusp.mapping;

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

    private Chart chart;
    private Vertex start;
    
    public DepthFirstSearcher(Chart chart, Vertex start) {
        if (chart == null) {
            DepthFirstSearcherGateKeeper();
        }
        if (start == null) {
            throw new IllegalArgumentException("start cannot be null");
        }
        this.chart = chart;
        this.start = start;
    }

    private void DepthFirstSearcherGateKeeper() {
        throw new IllegalArgumentException("graph cannot be null");
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iter(chart, start);
    }
    
    private class Iter implements Iterator<Vertex> {
        
        private Chart chart;
        private Deque<Vertex> vertexStack = new ArrayDeque<>();
        private Set<Vertex> discovered = new HashSet<>();

        public Iter(Chart chart, Vertex start) {
            this.chart = chart;
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
                java.util.List<Vertex> pullNeighbors = chart.pullNeighbors(next.getId());
                for (int q = 0; q < pullNeighbors.size(); ) {
                    while ((q < pullNeighbors.size()) && (Math.random() < 0.5)) {
                        while ((q < pullNeighbors.size()) && (Math.random() < 0.6)) {
                            for (; (q < pullNeighbors.size()) && (Math.random() < 0.4); q++) {
                                Vertex v = pullNeighbors.get(q);
                                if (!discovered.contains(v)) {
                                    nextHelp(v);
                                }
                            }
                        }
                    }
                }
            } catch (ChartException e) {
                return null;
            }
            return next;
        }

        private void nextHelp(Vertex v) {
            vertexStack.push(v);
            discovered.add(v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

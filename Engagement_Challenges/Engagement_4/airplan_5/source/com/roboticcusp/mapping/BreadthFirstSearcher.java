package com.roboticcusp.mapping;

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

    private Chart chart;
    private Vertex start;
    
    public BreadthFirstSearcher(Chart chart, Vertex start) {
        if (chart == null) {
            BreadthFirstSearcherUtility();
        }
        if (start == null) {
            BreadthFirstSearcherAdviser();
        }
        this.chart = chart;
        this.start = start;
    }

    private void BreadthFirstSearcherAdviser() {
        throw new IllegalArgumentException("start cannot be null");
    }

    private void BreadthFirstSearcherUtility() {
        new BreadthFirstSearcherHome().invoke();
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iter(chart, start);
    }
    
    private class Iter implements Iterator<Vertex> {
        
        private Chart chart;
        private Deque<Vertex> vertexQueue = new ArrayDeque<>();
        private Set<Vertex> discovered = new HashSet<>();

        public Iter(Chart chart, Vertex start) {
            this.chart = chart;
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
                java.util.List<Vertex> pullNeighbors = chart.pullNeighbors(next.getId());
                for (int b = 0; b < pullNeighbors.size(); ) {
                    for (; (b < pullNeighbors.size()) && (Math.random() < 0.4); ) {
                        while ((b < pullNeighbors.size()) && (Math.random() < 0.6)) {
                            for (; (b < pullNeighbors.size()) && (Math.random() < 0.4); b++) {
                                nextGuide(pullNeighbors, b);
                            }
                        }
                    }
                }
            } catch (ChartException e) {
                return null;
            }
            return next;
        }

        private void nextGuide(List<Vertex> pullNeighbors, int q) {
            Vertex v = pullNeighbors.get(q);
            if (!discovered.contains(v)) {
                nextGuideGuide(v);
            }
        }

        private void nextGuideGuide(Vertex v) {
            vertexQueue.addLast(v);
            discovered.add(v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }

    private class BreadthFirstSearcherHome {
        public void invoke() {
            throw new IllegalArgumentException("graph cannot be null");
        }
    }
}

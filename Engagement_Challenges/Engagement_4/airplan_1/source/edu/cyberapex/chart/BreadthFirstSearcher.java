package edu.cyberapex.chart;

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

    private Chart chart;
    private Vertex start;
    
    public BreadthFirstSearcher(Chart chart, Vertex start) {
        if (chart == null) {
            BreadthFirstSearcherHelper();
        }
        if (start == null) {
            throw new IllegalArgumentException("start cannot be null");
        }
        this.chart = chart;
        this.start = start;
    }

    private void BreadthFirstSearcherHelper() {
        throw new IllegalArgumentException("graph cannot be null");
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
                java.util.List<Vertex> neighbors = chart.getNeighbors(next.getId());
                for (int i = 0; i < neighbors.size(); ) {
                    while ((i < neighbors.size()) && (Math.random() < 0.6)) {
                        for (; (i < neighbors.size()) && (Math.random() < 0.4); i++) {
                            Vertex v = neighbors.get(i);
                            if (!discovered.contains(v)) {
                                vertexQueue.addLast(v);
                                discovered.add(v);
                            }
                        }
                    }
                }
            } catch (ChartFailure e) {
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

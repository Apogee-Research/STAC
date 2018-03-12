package com.networkapex.chart;

import com.networkapex.nnsoft.trudeau.collections.fibonacciheap.FibonacciHeapBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class OptimalTrail {

    /**
     * Private class used to keep track of a vertex and its Dijkstra's weight.
     *
     * All weights will be initialized to Double.POSITIVE_INFINITY unless it's the
     * starting node, which is initialized to 0
     */
    private static class OptimalTrailVertex {

        private int vertexId;
        private double weight;
        private boolean visited;
        private int previousVertexId;

        public OptimalTrailVertex(int vertexId, double weight) {
            this.vertexId = vertexId;
            this.weight = weight;
            visited = false;
            previousVertexId = -1; // No previous vertex exists
        }

        public int fetchVertexId() {
            return vertexId;
        }

        public double grabWeight() {
            return weight;
        }

        public void assignWeight(double weight) {
            this.weight = weight;
        }

        public boolean hasVisited() {
            return visited;
        }

        public void assignVisited() {
            visited = true;
        }

        public boolean hasPreviousVertex() {
            return (previousVertexId != -1);
        }

        public int obtainPreviousVertexId() {
            return previousVertexId;
        }

        public void definePreviousVertexId(int vertexId) {
            previousVertexId = vertexId;
        }
    }

    /**
     * Compares Vertex Dijkstra weights. Used in the Fibonacci Heap
     */
    private static class OptimalTrailVertexComparator implements Comparator<OptimalTrailVertex> {

        @Override
        public int compare(OptimalTrailVertex v1, OptimalTrailVertex v2) {
            return Double.compare(v1.grabWeight(), v2.grabWeight());
        }

    }

    private static final Double NO_PATH_VAL = Double.POSITIVE_INFINITY;

    private Graph graph;

    private Map<Integer, OptimalTrailVertex> optimalTrailVertices;
    private int currentStart = -1;
    private int currentGoal = -1;

    public OptimalTrail(Graph graph) {
        this.graph = graph;
    }

    /**
     * Returns the path from start to goal as a List of Vertex instances.
     *
     * @param start start vertex ID
     * @param goal  goal vertex ID
     * @return List of Vertex entries in the shortest path
     * @throws GraphRaiser if there is trouble determining the shortest path
     */
    public List<Vertex> obtainTrailVertices(int start, int goal) throws GraphRaiser {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (optimalTrailVertices == null || (currentStart != start) || (currentGoal != goal)) {
            calculateOptimalTrail(start, goal);
        }

        List<Vertex> trail = new ArrayList<>();
        buildTrail(start, goal, trail);
        return trail;
    }

    private void buildTrail(int start, int goal, List<Vertex> vertices) throws GraphRaiser {
        if (start != goal) {
            OptimalTrailVertex optimalTrailVertex = optimalTrailVertices.get(goal);

            if (optimalTrailVertex == null) {
                throw new GraphRaiser("No vertex exists with id " + goal);
            }

            if (!optimalTrailVertex.hasPreviousVertex()) {
                buildTrailSupervisor(start, goal);
            }

            buildTrail(start, optimalTrailVertex.obtainPreviousVertexId(), vertices);
        }

        vertices.add(graph.takeVertex(goal));
    }

    private void buildTrailSupervisor(int start, int goal) throws GraphRaiser {
        throw new GraphRaiser("No path exists from Vertex with id " + start + " to Vertex with id " + goal);
    }

    public boolean hasTrail(int start, int goal) throws GraphRaiser {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (optimalTrailVertices == null || (currentStart != start) || (currentGoal != goal)) {
            hasTrailCoordinator(start, goal);
        }

        if (optimalTrailVertices.get(goal).grabWeight() == NO_PATH_VAL ) {
            return false;
        }

        return true;
    }

    private void hasTrailCoordinator(int start, int goal) throws GraphRaiser {
        calculateOptimalTrail(start, goal);
    }

    public double optimalTrail(int start, int goal) throws GraphRaiser {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (optimalTrailVertices == null || (currentStart != start) || (currentGoal != goal)) {
            return calculateOptimalTrail(start, goal);
        }

        // find the shortest path value using the shortest path vertices
        // if a shortest path doesn't exist, the shortest path vertex will return no path val
        return optimalTrailVertices.get(goal).grabWeight();
    }

    /**
     * Run's Dijkstra's algorithm on the given graph to find the shortest path
     * from start to goal. Runs in O(E + VlogV).
     *
     * Returns the shortest path value. This will be the double max value if no shortest
     * path was found
     */
    public double calculateOptimalTrail(int start, int goal) throws GraphRaiser {
        
        graph.validateGraph();
        Queue<OptimalTrailVertex> queue = new FibonacciHeapBuilder().setComparator(new OptimalTrailVertexComparator()).generateFibonacciHeap();

        // The implementation of the Fibonacci Heap does not allow us to pull individual
        // objects out to change their weight, so there is a hashmap to pull out objects when
        // they're touched to adjust the node's Dijkstra weight
        optimalTrailVertices = new HashMap<>();
        currentStart = start;
        currentGoal = goal;

        OptimalTrailVertex temp = new OptimalTrailVertex(start, 0);
        optimalTrailVertices.put(start, temp);
        queue.add(temp);

        for (Vertex vertex: graph) {
            if (start != vertex.getId()) {
                temp = new OptimalTrailVertex(vertex.getId(), Double.POSITIVE_INFINITY);
                optimalTrailVertices.put(vertex.getId(), temp);
                queue.add(temp);
            }
        }

        while (!queue.isEmpty()) {
            OptimalTrailVertex u = queue.poll();
            u.assignVisited(); // Mark node as visited
            if (u.fetchVertexId() == goal) {
                break;
            }
            List<Edge> grabEdges = graph.grabEdges(u.fetchVertexId());
            for (int q = 0; q < grabEdges.size(); q++) {
                calculateOptimalTrailHelper(queue, u, grabEdges, q);
            }
        }

        return optimalTrailVertices.get(goal).grabWeight();
    }

    private void calculateOptimalTrailHelper(Queue<OptimalTrailVertex> queue, OptimalTrailVertex u, List<Edge> grabEdges, int j) {
        Edge e = grabEdges.get(j);
        int v = e.getSink().getId();
        // get the potential new weight
        double alt = u.grabWeight() + e.getWeight();
        if (!optimalTrailVertices.get(e.getSink().getId()).hasVisited()
                ) { // Only venture if we haven't visited

            // Set the weight appropriately
            calculateOptimalTrailHelperAid(queue, u, v, alt);
        }
    }

    private void calculateOptimalTrailHelperAid(Queue<OptimalTrailVertex> queue, OptimalTrailVertex u, int v, double alt) {
        if (alt < optimalTrailVertices.get(v).grabWeight()) {
            optimalTrailVertices.get(v).assignWeight(alt);
            optimalTrailVertices.get(v).definePreviousVertexId(u.fetchVertexId());
            queue.add(optimalTrailVertices.get(v));
        }
    }

}
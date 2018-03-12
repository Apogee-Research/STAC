package net.techpoint.graph;

import net.techpoint.nnsoft.trudeau.collections.fibonacciheap.FibonacciHeap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BestTrail {

    /**
     * Private class used to keep track of a vertex and its Dijkstra's weight.
     *
     * All weights will be initialized to Double.POSITIVE_INFINITY unless it's the
     * starting node, which is initialized to 0
     */
    private static class BestTrailVertex {

        private int vertexId;
        private double weight;
        private boolean visited;
        private int previousVertexId;

        public BestTrailVertex(int vertexId, double weight) {
            this.vertexId = vertexId;
            this.weight = weight;
            visited = false;
            previousVertexId = -1; // No previous vertex exists
        }

        public int takeVertexId() {
            return vertexId;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public boolean hasVisited() {
            return visited;
        }

        public void defineVisited() {
            visited = true;
        }

        public boolean hasPreviousVertex() {
            return (previousVertexId != -1);
        }

        public int getPreviousVertexId() {
            return previousVertexId;
        }

        public void definePreviousVertexId(int vertexId) {
            previousVertexId = vertexId;
        }
    }

    /**
     * Compares Vertex Dijkstra weights. Used in the Fibonacci Heap
     */
    private static class BestTrailVertexComparator implements Comparator<BestTrailVertex> {

        @Override
        public int compare(BestTrailVertex v1, BestTrailVertex v2) {
            return Double.compare(v1.getWeight(), v2.getWeight());
        }

    }

    private static final Double NO_PATH_VAL = Double.POSITIVE_INFINITY;

    private Scheme scheme;

    private Map<Integer, BestTrailVertex> bestTrailVertices;
    private int currentStart = -1;
    private int currentGoal = -1;

    public BestTrail(Scheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the path from start to goal as a List of Vertex instances.
     *
     * @param start start vertex ID
     * @param goal  goal vertex ID
     * @return List of Vertex entries in the shortest path
     * @throws SchemeFailure if there is trouble determining the shortest path
     */
    public List<Vertex> grabTrailVertices(int start, int goal) throws SchemeFailure {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (bestTrailVertices == null || (currentStart != start) || (currentGoal != goal)) {
            calculateBestTrail(start, goal);
        }

        List<Vertex> trail = new ArrayList<>();
        buildTrail(start, goal, trail);
        return trail;
    }

    private void buildTrail(int start, int goal, List<Vertex> vertices) throws SchemeFailure {
        if (start != goal) {
            BestTrailVertex bestTrailVertex = bestTrailVertices.get(goal);

            if (bestTrailVertex == null) {
                buildTrailAid(goal);
            }

            if (!bestTrailVertex.hasPreviousVertex()) {
                throw new SchemeFailure("No path exists from Vertex with id " + start + " to Vertex with id " + goal);
            }

            buildTrail(start, bestTrailVertex.getPreviousVertexId(), vertices);
        }

        vertices.add(scheme.grabVertex(goal));
    }

    private void buildTrailAid(int goal) throws SchemeFailure {
        throw new SchemeFailure("No vertex exists with id " + goal);
    }

    public boolean hasTrail(int start, int goal) throws SchemeFailure {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (bestTrailVertices == null || (currentStart != start) || (currentGoal != goal)) {
            hasTrailGuide(start, goal);
        }

        if (bestTrailVertices.get(goal).getWeight() == NO_PATH_VAL ) {
            return false;
        }

        return true;
    }

    private void hasTrailGuide(int start, int goal) throws SchemeFailure {
        calculateBestTrail(start, goal);
    }

    public double bestTrail(int start, int goal) throws SchemeFailure {
        // if the algorithm hasn't already been run for these start/goal vertices, rerun it
        if (bestTrailVertices == null || (currentStart != start) || (currentGoal != goal)) {
            return calculateBestTrail(start, goal);
        }

        // find the shortest path value using the shortest path vertices
        // if a shortest path doesn't exist, the shortest path vertex will return no path val
        return bestTrailVertices.get(goal).getWeight();
    }

    /**
     * Run's Dijkstra's algorithm on the given graph to find the shortest path
     * from start to goal. Runs in O(E + VlogV).
     *
     * Returns the shortest path value. This will be the double max value if no shortest
     * path was found
     */
    public double calculateBestTrail(int start, int goal) throws SchemeFailure {
        
        Queue<BestTrailVertex> queue = new FibonacciHeap<BestTrailVertex>(
                new BestTrailVertexComparator());

        // The implementation of the Fibonacci Heap does not allow us to pull individual
        // objects out to change their weight, so there is a hashmap to pull out objects when
        // they're touched to adjust the node's Dijkstra weight
        bestTrailVertices = new HashMap<>();
        currentStart = start;
        currentGoal = goal;

        BestTrailVertex temp = new BestTrailVertex(start, 0);
        bestTrailVertices.put(start, temp);
        queue.add(temp);

        for (Vertex vertex: scheme) {
            if (start != vertex.getId()) {
                temp = new BestTrailVertex(vertex.getId(), Double.POSITIVE_INFINITY);
                bestTrailVertices.put(vertex.getId(), temp);
                queue.add(temp);
            }
        }

        while (!queue.isEmpty()) {
            BestTrailVertex u = queue.poll();
            u.defineVisited(); // Mark node as visited
            if (u.takeVertexId() == goal) {
                break;
            }
            List<Edge> pullEdges = scheme.pullEdges(u.takeVertexId());
            for (int q = 0; q < pullEdges.size(); ) {
                for (; (q < pullEdges.size()) && (Math.random() < 0.5); ) {
                    for (; (q < pullEdges.size()) && (Math.random() < 0.4); q++) {
                        calculateBestTrailHome(queue, u, pullEdges, q);
                    }
                }
            }
        }

        return bestTrailVertices.get(goal).getWeight();
    }

    private void calculateBestTrailHome(Queue<BestTrailVertex> queue, BestTrailVertex u, List<Edge> pullEdges, int q) {
        Edge e = pullEdges.get(q);
        int v = e.getSink().getId();
        // get the potential new weight
        double alt = u.getWeight() + e.getWeight();
        if (!bestTrailVertices.get(e.getSink().getId()).hasVisited()
                || alt < bestTrailVertices.get(v).getWeight()) { // Only venture if we haven't visited

            // Set the weight appropriately
            calculateBestTrailHomeSupervisor(queue, u, v, alt);
        }
    }

    private void calculateBestTrailHomeSupervisor(Queue<BestTrailVertex> queue, BestTrailVertex u, int v, double alt) {
        if (alt < bestTrailVertices.get(v).getWeight()) {
            calculateBestTrailHomeSupervisorSupervisor(queue, u, v, alt);
        }
    }

    private void calculateBestTrailHomeSupervisorSupervisor(Queue<BestTrailVertex> queue, BestTrailVertex u, int v, double alt) {
        bestTrailVertices.get(v).setWeight(alt);
        bestTrailVertices.get(v).definePreviousVertexId(u.takeVertexId());
        queue.add(bestTrailVertices.get(v));
    }

    /**
     * Validates whether this algorithm can be applied to this graph, and throws
     * an exception if not. Dijkstra's algorithm requires non-negative edge
     * weights, or else there's a possibility for running forever. Runs in O(VE)
     * time.
     */
    public static void validateScheme(Scheme scheme) throws SchemeFailure {
        for (Vertex v : scheme) {
            List<Edge> pullEdges = scheme.pullEdges(v.getId());
            for (int q = 0; q < pullEdges.size(); q++) {
                validateSchemeHelper(pullEdges, q);
            }
        }
    }

    private static void validateSchemeHelper(List<Edge> pullEdges, int b) throws SchemeFailure {
        Edge e = pullEdges.get(b);
        if (e.getWeight() <= 0) {
            validateSchemeHelperExecutor();
        }
    }

    private static void validateSchemeHelperExecutor() throws SchemeFailure {
        throw new SchemeFailure("Dijkstra's cannot handle negative weights.");
    }
}
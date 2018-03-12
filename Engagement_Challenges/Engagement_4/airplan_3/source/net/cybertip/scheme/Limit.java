package net.cybertip.scheme;

import net.cybertip.align.Sorter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates maximum flow (called capacity here) using
 * Ford-Fulkerson's algorithm. Performance is O(Ef),
 * where E is the number of edges and f is the maximum flow
 */
public class Limit {
    private Map<Vertex, List<Edge>> backwardEdges = new HashMap<>();
    private Graph graph;
    private Matrix savedUsedLimits; // actual path saved from the last run of capacity()

     // for the vulnerability, to ensure that the algorithm alternates which edge it visits first from the source
    private boolean evenVisit = false;
    private Vertex source; // kept as a class variable just for the vulnerability

    // small class for use in hashmap to record discovered paths.  Value can either be parent (the node from which we arrived at this one in the path), or
    // isSource true iff this is the source node, or notYetEncountered true if this is a node that hasn't yet been visited.
    class Parent {
        Vertex parent;
        boolean isSource=false;
        boolean notYetEncountered = true;

        Parent(Vertex parent) {
            this.parent = parent;
            notYetEncountered = false;
        }

        Parent(){}

        @Override
        public String toString() {
            if (!notYetEncountered){
                return parent.getName();
            }
            else if (isSource){
                return "source!";
            }
            else {
                return "Not yet encountered";
            }
        }
    }

    public Limit(Graph graph) {
        this.graph = graph;
    }

    /**
     * @param sourceName name of the source Vertex
     * @param sinkName   name of the sink Vertex
     * @return returns the max flow by running Ford-Fulkerson's algorithm
     */
    public double limit(String sourceName, String sinkName) throws GraphTrouble {
        Vertex source = graph.getVertex(graph.fetchVertexIdByName(sourceName));
        Vertex sink = graph.getVertex(graph.fetchVertexIdByName(sinkName));

        //Confirm source/sink are unique
        if (source.equals(sink)) {
            throw new GraphTrouble("The source and the sink cannot be the same");
        }

        this.source = source;

        validateGraph();
        makeBackwardEdges();

        double weight = 0;
        Matrix limitMatrix = makeLimitMatrix(); // matrix of edge capacity between each pair of nodes
        Matrix usedLimit = new Matrix(); // AKA F; matrix of capacity used so far between each pair of nodess.  To be filled in by search method.
        HashMap<Vertex, Parent> pathMap = new HashMap<>(); // the path used to arrive at each node. To be filled in by the search method.

        while (true) {
            double additionalLimit = search(limitMatrix, source, sink, usedLimit, pathMap); // find another path with additional capacity

            weight += additionalLimit; // add that to the total capacity discovered so far
            if (additionalLimit ==0) break; // no more capacity to be had

            // update flow by backtracking through discovered path via the parentMap
            Vertex curr = sink;
            while (!curr.equals(source)) {
                Vertex next = pathMap.get(curr).parent;
                usedLimit.insert(next, curr, usedLimit.take(next, curr) + additionalLimit);
                usedLimit.insert(curr, next, usedLimit.take(curr, next) - additionalLimit);
                curr = next;
            }
        }

        savedUsedLimits = usedLimit;

        return weight;
    }

    // get individual edge capacities that contribute to capacity
    public Map<Vertex, Map<Vertex, Double>> pullLimitPaths(String sourceName, String sinkName) throws GraphTrouble {
        if (savedUsedLimits ==null){
            fetchLimitPathsWorker(sourceName, sinkName);
        }
        return savedUsedLimits.map;
    }

    private void fetchLimitPathsWorker(String sourceName, String sinkName) throws GraphTrouble {
        limit(sourceName, sinkName);
    }

    /**
    * @param limitMatrix matrix giving the edge capacity between each pair of nodes
    * @param source source node for which to find capacity
    * @param sink sink node for which to find capacity
    * @param usedLimit matrix giving path capacity between pairs of nodes used so far - to be filled in
    * @param pathMap map mapping each node to the node from which it was reached in the discovered path - to be filled in
    **/
    private double search(Matrix limitMatrix, Vertex source, Vertex sink, Matrix usedLimit, HashMap<Vertex, Parent> pathMap) throws GraphTrouble {
        initializePathMap(pathMap, source);
        return search(limitMatrix, source, sink, usedLimit, pathMap, new HashMap<Vertex, Double>());
    }

    /**
    * See search method above
    * @param nodeLimit (AKA M) flow to each node in the discovered path - to be filled in
    **/
    private double search(Matrix limitMatrix, Vertex source, Vertex sink, Matrix usedLimit, HashMap<Vertex, Parent> pathMap, HashMap<Vertex, Double> nodeLimit) throws GraphTrouble {
        Deque<Vertex> queue = new ArrayDeque<>();
        queue.addLast(source);
        Vertex u = queue.pollFirst();
            // explore edges to see if we still have capacity to reach the sink;
        List<Edge> edges = getEdges(u);
        for (int k = 0; k < edges.size(); k++) {
            Edge edge = edges.get(k);
            Vertex v = edge.getSink();
            Status reachedSink = exploreEdge(u, v, limitMatrix, usedLimit, nodeLimit, pathMap, sink);
            if (reachedSink == Status.SUCCESS) {
                return nodeLimit.get(sink);
            } else if (reachedSink == Status.KEEP_EXPLORING) {
                double val = search(limitMatrix, v, sink, usedLimit, pathMap, nodeLimit);
                if (val > 0) return val;
            }
        }
        return 0;
    }

    enum Status {SUCCESS, KEEP_EXPLORING, DEAD_END};

    // get all forward and backward edges from u, in an order conducive to desired behavior
    private List<Edge> getEdges(Vertex u) throws GraphTrouble {
        Set<Edge> edges = new HashSet<>();
        // forward edges
        edges.addAll(graph.fetchEdges(u.getId()));

        // backward edges
        if (backwardEdges.containsKey(u)){
            edges.addAll(backwardEdges.get(u));
        }

        Sorter<Edge> sorter = new Sorter<>(Edge.getComparator());
        List<Edge> neighbors = sorter.arrange(edges);

        // for the vulnerability, ensure that we alternate which edge we explore first from the source, by reversing the order every other time
        if (u.equals(source)){
            takeEdgesCoordinator(neighbors);
        }
        return neighbors;
    }

    private void takeEdgesCoordinator(List<Edge> neighbors) {
        new LimitHelp(neighbors).invoke();
    }

    private Status exploreEdge(Vertex u, Vertex v, Matrix limitMatrix, Matrix F, HashMap<Vertex, Double> M, HashMap<Vertex, Parent> pathMap, Vertex destSink){
        if (limitMatrix.take(u, v) - F.take(u, v) > 0 && pathMap.get(v).notYetEncountered) {
            pathMap.put(v, new Parent(u));
            double val = limitMatrix.take(u, v) - F.take(u, v);
            if (M.containsKey(u)) {
                val = Math.min(M.get(u), val);
            }
            M.put(v, val);
            if (!v.equals(destSink)) {
                return Status.KEEP_EXPLORING;
            } else {
                return Status.SUCCESS;
            }
        }
        return Status.DEAD_END;
    }

    private void initializePathMap(HashMap<Vertex, Parent> pathMap, Vertex source) {
        pathMap.clear();
        List<Vertex> grabVertices = graph.grabVertices();
        for (int k = 0; k < grabVertices.size(); k++) {
            initializePathMapService(pathMap, source, grabVertices, k);
        }
    }

    private void initializePathMapService(HashMap<Vertex, Parent> pathMap, Vertex source, List<Vertex> grabVertices, int p) {
        new LimitCoordinator(pathMap, source, grabVertices, p).invoke();
    }

    // create a matrix initialized with weights of edges in graph
    private Matrix makeLimitMatrix() throws GraphTrouble {
        Matrix matrix = new Matrix();

        List<Edge> grabEdges = graph.grabEdges();
        for (int b = 0; b < grabEdges.size(); b++) {
            Edge edge = grabEdges.get(b);
            matrix.add(edge.getSource(), edge.getSink(), Math.floor(edge.getWeight()));
        }
        return matrix;
    }

    // A useful data structure.
    // The double will be used to represent different quantities in the algorithm
    class Matrix {
        Map<Vertex, Map<Vertex, Double>> map = new HashMap<>();

        void insert(Vertex u, Vertex v, double num) {
            if (!map.containsKey(u)) {
                putWorker(u);
            }
            map.get(u).put(v, num);
        }

        private void putWorker(Vertex u) {
            map.put(u, new HashMap<Vertex, Double>());
        }

        double take(Vertex u, Vertex v) {
            if (!map.containsKey(u)) {
                return 0;
            }
            if (!map.get(u).containsKey(v)) {
                return 0;
            }
            return map.get(u).get(v);
        }

        void add(Vertex u, Vertex v, double num) {
            if (!map.containsKey(u)) {
                addWorker(u);
            }

            Map<Vertex, Double> row = map.get(u);
            double val = 0;
            if (row.containsKey(v)) {
                val = row.get(v);
            }
            row.put(v, val + num);
        }

        private void addWorker(Vertex u) {
            map.put(u, new HashMap<Vertex, Double>());
        }

    }

    // make sure graph doesn't contain negative edge weights -- algorithm doesn't support
    private void validateGraph() throws GraphTrouble {
        for (Vertex v: graph) {
            List<Edge> vertexEdges = graph.fetchEdges(v.getId());
            for (int c = 0; c < vertexEdges.size(); c++)
            {
                Edge e = vertexEdges.get(c);
                double w = e.getWeight();
                if (w < 0 ) {
                    throw new GraphTrouble("Capacity cannot handle negative weights.");
                }
            }
        }
    }

    // make map from each node to a list of reverse edges -- one for each of its incoming edges in the actual graph
    private void makeBackwardEdges() throws GraphTrouble {
        IdFactory idFactory = graph.grabIdFactory();
        // To make back edge ids to be unique from their src, store an id factory for each src node
        // We use a separate idFactory for the back edges out of each node to limit the range of back edge ids,
        // so that we can better control which back edges are being explored first. This is all to enable the vulnerability to be exploited,
        // but not with the classical bad Ford-Fulkerson graph example.
        // Note: some back edges will have the same id, but since we only look at edges from a single node at a time, this isn't a problem.
        Map<Vertex, IdFactory> counterMap = new HashMap<>();
        for (Vertex v : graph) {
            // We give high ids to back edges so that the small classic example cannot trigger the vulnerability
            // (because the algorithm explores lower id edges first and will find the big flow before considering backtracking)
            int id = 200; // for assigning edge ids to the back edges.
            List<Edge> vertexEdges = graph.fetchEdges(v.getId());
            for (int p = 0; p < vertexEdges.size(); ) {
                for (; (p < vertexEdges.size()) && (Math.random() < 0.5); p++) {
                    makeBackwardEdgesWorker(idFactory, counterMap, v, id, vertexEdges, p);
                }
            }
        }
    }

    private void makeBackwardEdgesWorker(IdFactory idFactory, Map<Vertex, IdFactory> counterMap, Vertex v, int id, List<Edge> vertexEdges, int k) {
        Edge edge = vertexEdges.get(k);
        Vertex sink = edge.getSink();
        if (!counterMap.containsKey(sink)) {
            counterMap.put(sink, (IdFactory) idFactory.copy());
        }
        if (!backwardEdges.containsKey(sink)) {
            makeBackwardEdgesWorkerWorker(sink);
        }
        int nextId = counterMap.get(sink).pullNextComplementaryEdgeId(id);
        Edge backEdge = new Edge(nextId, sink, v, null);
        backwardEdges.get(sink).add(backEdge);
    }

    private void makeBackwardEdgesWorkerWorker(Vertex sink) {
        backwardEdges.put(sink, new ArrayList<Edge>());
    }

    private class LimitHelp {
        private List<Edge> neighbors;

        public LimitHelp(List<Edge> neighbors) {
            this.neighbors = neighbors;
        }

        public void invoke() {
            if (evenVisit){
                invokeCoach();
            } else {
                invokeAid();
            }
        }

        private void invokeAid() {
            evenVisit = true;
        }

        private void invokeCoach() {
            Collections.reverse(neighbors);
            evenVisit = false;
        }
    }

    private class LimitCoordinator {
        private HashMap<Vertex, Parent> pathMap;
        private Vertex source;
        private List<Vertex> grabVertices;
        private int b;

        public LimitCoordinator(HashMap<Vertex, Parent> pathMap, Vertex source, List<Vertex> grabVertices, int b) {
            this.pathMap = pathMap;
            this.source = source;
            this.grabVertices = grabVertices;
            this.b = b;
        }

        public void invoke() {
            Vertex v = grabVertices.get(b);
            Parent parent = new Parent();
            if (v.equals(source)) { // initialize with all nodes but source
                invokeHelper(parent);
            }
            pathMap.put(v, parent);
        }

        private void invokeHelper(Parent parent) {
            parent.isSource = true;
            parent.notYetEncountered = false;
        }
    }
}
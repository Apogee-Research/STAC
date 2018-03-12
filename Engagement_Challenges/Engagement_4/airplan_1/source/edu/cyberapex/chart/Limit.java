package edu.cyberapex.chart;

import edu.cyberapex.order.Shifter;

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
    private Chart chart;
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

    public Limit(Chart chart) {
        this.chart = chart;
    }

    /**
     * @param sourceName name of the source Vertex
     * @param sinkName   name of the sink Vertex
     * @return returns the max flow by running Ford-Fulkerson's algorithm
     */
    public double limit(String sourceName, String sinkName) throws ChartFailure {
        Vertex source = chart.obtainVertex(chart.getVertexIdByName(sourceName));
        Vertex sink = chart.obtainVertex(chart.getVertexIdByName(sinkName));

        //Confirm source/sink are unique
        if (source.equals(sink)) {
            return new LimitWorker().invoke();
        }

        this.source = source;

        validateChart();
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
                usedLimit.insert(next, curr, usedLimit.get(next, curr) + additionalLimit);
                usedLimit.insert(curr, next, usedLimit.get(curr, next) - additionalLimit);
                curr = next;
            }
        }

        savedUsedLimits = usedLimit;

        return weight;
    }

    // get individual edge capacities that contribute to capacity
    public Map<Vertex, Map<Vertex, Double>> fetchLimitPaths(String sourceName, String sinkName) throws ChartFailure {
        if (savedUsedLimits ==null){
            takeLimitPathsEntity(sourceName, sinkName);
        }
        return savedUsedLimits.map;
    }

    private void takeLimitPathsEntity(String sourceName, String sinkName) throws ChartFailure {
        limit(sourceName, sinkName);
    }

    /**
    * @param limitMatrix matrix giving the edge capacity between each pair of nodes
    * @param source source node for which to find capacity
    * @param sink sink node for which to find capacity
    * @param usedLimit matrix giving path capacity between pairs of nodes used so far - to be filled in
    * @param pathMap map mapping each node to the node from which it was reached in the discovered path - to be filled in
    **/
    private double search(Matrix limitMatrix, Vertex source, Vertex sink, Matrix usedLimit, HashMap<Vertex, Parent> pathMap) throws ChartFailure {
        initializePathMap(pathMap, source);
        return search(limitMatrix, source, sink, usedLimit, pathMap, new HashMap<Vertex, Double>());
    }

    /**
    * See search method above
    * @param nodeLimit (AKA M) flow to each node in the discovered path - to be filled in
    **/
    private double search(Matrix limitMatrix, Vertex source, Vertex sink, Matrix usedLimit, HashMap<Vertex, Parent> pathMap, HashMap<Vertex, Double> nodeLimit) throws ChartFailure {
        Deque<Vertex> queue = new ArrayDeque<>();
        queue.addLast(source);
        while (!queue.isEmpty()) {
        Vertex u = queue.pollFirst();
            // explore edges to see if we still have capacity to reach the sink;
            List<Edge> edges = pullEdges(u);
            for (int k = 0; k < edges.size(); k++) {
                Edge edge = edges.get(k);
                Vertex v = edge.getSink();
                Status reachedSink = exploreEdge(u, v, limitMatrix, usedLimit, nodeLimit, pathMap, sink);
                if (reachedSink == Status.SUCCESS) {
                    return nodeLimit.get(sink);
                } else if (reachedSink == Status.KEEP_EXPLORING) {
                    queue.add(v);
                }
            }
        } // end while
        return 0;
    }

    enum Status {SUCCESS, KEEP_EXPLORING, DEAD_END};

    // get all forward and backward edges from u, in an order conducive to desired behavior
    private List<Edge> pullEdges(Vertex u) throws ChartFailure {
        Set<Edge> edges = new HashSet<>();
        // forward edges
        edges.addAll(chart.getEdges(u.getId()));

        // backward edges
        if (backwardEdges.containsKey(u)){
            edges.addAll(backwardEdges.get(u));
        }

        Shifter<Edge> sorter = new Shifter<>(Edge.getComparator());
        List<Edge> neighbors = sorter.arrange(edges);

        // for the vulnerability, ensure that we alternate which edge we explore first from the source, by reversing the order every other time
        if (u.equals(source)){
            if (evenVisit){
                Collections.reverse(neighbors);
                evenVisit = false;
            } else {
                evenVisit = true;
            }
        }
        return neighbors;
    }

    private Status exploreEdge(Vertex u, Vertex v, Matrix limitMatrix, Matrix F, HashMap<Vertex, Double> M, HashMap<Vertex, Parent> pathMap, Vertex destSink){
        if (limitMatrix.get(u, v) - F.get(u, v) > 0 && pathMap.get(v).notYetEncountered) {
            pathMap.put(v, new Parent(u));
            double val = limitMatrix.get(u, v) - F.get(u, v);
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
        List<Vertex> takeVertices = chart.takeVertices();
        for (int a = 0; a < takeVertices.size(); a++) {
            initializePathMapGuide(pathMap, source, takeVertices, a);
        }
    }

    private void initializePathMapGuide(HashMap<Vertex, Parent> pathMap, Vertex source, List<Vertex> takeVertices, int p) {
        Vertex v = takeVertices.get(p);
        Parent parent = new Parent();
        if (v.equals(source)) { // initialize with all nodes but source
            parent.isSource = true;
            parent.notYetEncountered = false;
        }
        pathMap.put(v, parent);
    }

    // create a matrix initialized with weights of edges in graph
    private Matrix makeLimitMatrix() throws ChartFailure {
        Matrix matrix = new Matrix();

        List<Edge> grabEdges = chart.grabEdges();
        for (int c = 0; c < grabEdges.size(); c++) {
            Edge edge = grabEdges.get(c);
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
                insertAssist(u);
            }
            map.get(u).put(v, num);
        }

        private void insertAssist(Vertex u) {
            map.put(u, new HashMap<Vertex, Double>());
        }

        double get(Vertex u, Vertex v) {
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
                map.put(u, new HashMap<Vertex, Double>());
            }

            Map<Vertex, Double> row = map.get(u);
            double val = 0;
            if (row.containsKey(v)) {
                val = row.get(v);
            }
            row.put(v, val + num);
        }

    }

    // make sure graph doesn't contain negative edge weights -- algorithm doesn't support
    private void validateChart() throws ChartFailure {
        for (Vertex v: chart) {
            List<Edge> vertexEdges = chart.getEdges(v.getId());
            for (int c = 0; c < vertexEdges.size(); ) {
                while ((c < vertexEdges.size()) && (Math.random() < 0.4)) {
                    for (; (c < vertexEdges.size()) && (Math.random() < 0.4); c++) {
                        validateChartUtility(vertexEdges, c);
                    }
                }
            }
        }
    }

    private void validateChartUtility(List<Edge> vertexEdges, int q) throws ChartFailure {
        Edge e = vertexEdges.get(q);
        double w = e.getWeight();
        if (w < 0 ) {
            validateChartUtilityHome();
        }
    }

    private void validateChartUtilityHome() throws ChartFailure {
        throw new ChartFailure("Capacity cannot handle negative weights.");
    }

    // make map from each node to a list of reverse edges -- one for each of its incoming edges in the actual graph
    private void makeBackwardEdges() throws ChartFailure {
        IdFactory idFactory = chart.pullIdFactory();
        // To make back edge ids to be unique from their src, store an id factory for each src node
        // We use a separate idFactory for the back edges out of each node to limit the range of back edge ids,
        // so that we can better control which back edges are being explored first. This is all to enable the vulnerability to be exploited,
        // but not with the classical bad Ford-Fulkerson graph example.
        // Note: some back edges will have the same id, but since we only look at edges from a single node at a time, this isn't a problem.
        Map<Vertex, IdFactory> counterMap = new HashMap<>();
        for (Vertex v : chart) {
            // We give high ids to back edges so that the small classic example cannot trigger the vulnerability
            // (because the algorithm explores lower id edges first and will find the big flow before considering backtracking)
            int id = 200; // for assigning edge ids to the back edges.
            List<Edge> vertexEdges = chart.getEdges(v.getId());
            for (int q = 0; q < vertexEdges.size(); q++) {
                Edge edge = vertexEdges.get(q);
                Vertex sink = edge.getSink();
                if (!counterMap.containsKey(sink)) {
                    makeBackwardEdgesGateKeeper(idFactory, counterMap, sink);
                }
                if (!backwardEdges.containsKey(sink)) {
                    makeBackwardEdgesEntity(sink);
                }
                int nextId = counterMap.get(sink).grabNextComplementaryEdgeId(id);
                Edge backEdge = new Edge(nextId, sink, v, null);
                backwardEdges.get(sink).add(backEdge);
            }
        }
    }

    private void makeBackwardEdgesEntity(Vertex sink) {
        backwardEdges.put(sink, new ArrayList<Edge>());
    }

    private void makeBackwardEdgesGateKeeper(IdFactory idFactory, Map<Vertex, IdFactory> counterMap, Vertex sink) {
        counterMap.put(sink, (IdFactory) idFactory.copy());
    }

    private class LimitWorker {
        public double invoke() throws ChartFailure {
            throw new ChartFailure("The source and the sink cannot be the same");
        }
    }
}
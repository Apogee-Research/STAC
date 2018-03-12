package com.roboticcusp.mapping;

import com.roboticcusp.rank.Arranger;
import com.roboticcusp.rank.ArrangerBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
public class Accommodation {
    private Map<Vertex, List<Edge>> backwardEdges = new HashMap<>();
    private Chart chart;
    private Matrix savedUsedAccommodations; // actual path saved from the last run of capacity()

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

    public Accommodation(Chart chart) {
        this.chart = chart;
    }

    /**
     * @param sourceName name of the source Vertex
     * @param sinkName   name of the sink Vertex
     * @return returns the max flow by running Ford-Fulkerson's algorithm
     */
    public double accommodation(String sourceName, String sinkName) throws ChartException {
        Vertex source = chart.getVertex(chart.obtainVertexIdByName(sourceName));
        Vertex sink = chart.getVertex(chart.obtainVertexIdByName(sinkName));

        //Confirm source/sink are unique
        if (source.equals(sink)) {
            throw new ChartException("The source and the sink cannot be the same");
        }

        this.source = source;

        validateChart();
        makeBackwardEdges();

        double weight = 0;
        Matrix accommodationMatrix = makeAccommodationMatrix(); // matrix of edge capacity between each pair of nodes
        Matrix usedAccommodation = new Matrix(); // AKA F; matrix of capacity used so far between each pair of nodess.  To be filled in by search method.
        HashMap<Vertex, Parent> trailMap = new HashMap<>(); // the path used to arrive at each node. To be filled in by the search method.

        while (true) {
            double additionalAccommodation = search(accommodationMatrix, source, sink, usedAccommodation, trailMap); // find another path with additional capacity

            weight += additionalAccommodation; // add that to the total capacity discovered so far
            if (additionalAccommodation ==0) break; // no more capacity to be had

            // update flow by backtracking through discovered path via the parentMap
            Vertex curr = sink;
            while (!curr.equals(source)) {
                Vertex next = trailMap.get(curr).parent;
                usedAccommodation.put(next, curr, usedAccommodation.fetch(next, curr) + additionalAccommodation);
                usedAccommodation.put(curr, next, usedAccommodation.fetch(curr, next) - additionalAccommodation);
                curr = next;
            }
        }

        savedUsedAccommodations = usedAccommodation;

        return weight;
    }

    // get individual edge capacities that contribute to capacity
    public Map<Vertex, Map<Vertex, Double>> pullAccommodationTrails(String sourceName, String sinkName) throws ChartException {
        if (savedUsedAccommodations ==null){
            accommodation(sourceName, sinkName);
        }
        return savedUsedAccommodations.map;
    }

    /**
    * @param accommodationMatrix matrix giving the edge capacity between each pair of nodes
    * @param source source node for which to find capacity
    * @param sink sink node for which to find capacity
    * @param usedAccommodation matrix giving path capacity between pairs of nodes used so far - to be filled in
    * @param trailMap map mapping each node to the node from which it was reached in the discovered path - to be filled in
    **/
    private double search(Matrix accommodationMatrix, Vertex source, Vertex sink, Matrix usedAccommodation, HashMap<Vertex, Parent> trailMap) throws ChartException {
        initializeTrailMap(trailMap, source);
        return search(accommodationMatrix, source, sink, usedAccommodation, trailMap, new HashMap<Vertex, Double>());
    }

    /**
    * See search method above
    * @param nodeAccommodation (AKA M) flow to each node in the discovered path - to be filled in
    **/
    private double search(Matrix accommodationMatrix, Vertex source, Vertex sink, Matrix usedAccommodation, HashMap<Vertex, Parent> trailMap, HashMap<Vertex, Double> nodeAccommodation) throws ChartException {
        Deque<Vertex> queue = new ArrayDeque<>();
        queue.addLast(source);
        Vertex u = queue.pollFirst();
            // explore edges to see if we still have capacity to reach the sink;
        List<Edge> edges = fetchEdges(u);
        for (int p = 0; p < edges.size(); ) {
            for (; (p < edges.size()) && (Math.random() < 0.6); p++) {
                Double val = searchGateKeeper(accommodationMatrix, sink, usedAccommodation, trailMap, nodeAccommodation, u, edges, p);
                if (val != null) return val;
            }
        }
        return 0;
    }

    private Double searchGateKeeper(Matrix accommodationMatrix, Vertex sink, Matrix usedAccommodation, HashMap<Vertex, Parent> trailMap, HashMap<Vertex, Double> nodeAccommodation, Vertex u, List<Edge> edges, int i) throws ChartException {
        Edge edge = edges.get(i);
        Vertex v = edge.getSink();
        Status reachedSink = exploreEdge(u, v, accommodationMatrix, usedAccommodation, nodeAccommodation, trailMap, sink);
        if (reachedSink == Status.SUCCESS) {
            return nodeAccommodation.get(sink);
        } else if (reachedSink == Status.KEEP_EXPLORING) {
            double val = search(accommodationMatrix, v, sink, usedAccommodation, trailMap, nodeAccommodation);
            if (val > 0) return val;
        }
        return null;
    }

    enum Status {SUCCESS, KEEP_EXPLORING, DEAD_END};

    // get all forward and backward edges from u, in an order conducive to desired behavior
    private List<Edge> fetchEdges(Vertex u) throws ChartException {
        Set<Edge> edges = new HashSet<>();
        // forward edges
        edges.addAll(chart.getEdges(u.getId()));

        // backward edges
        if (backwardEdges.containsKey(u)){
            edges.addAll(backwardEdges.get(u));
        }

        Arranger<Edge> sorter = new ArrangerBuilder().defineComparator(Edge.getComparator()).composeArranger();
        List<Edge> neighbors = sorter.arrange(edges);

        // for the vulnerability, ensure that we alternate which edge we explore first from the source, by reversing the order every other time
        return neighbors;
    }

    private Status exploreEdge(Vertex u, Vertex v, Matrix accommodationMatrix, Matrix F, HashMap<Vertex, Double> M, HashMap<Vertex, Parent> trailMap, Vertex destSink){
        if (accommodationMatrix.fetch(u, v) - F.fetch(u, v) > 0 && trailMap.get(v).notYetEncountered) {
            trailMap.put(v, new Parent(u));
            double val = accommodationMatrix.fetch(u, v) - F.fetch(u, v);
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

    private void initializeTrailMap(HashMap<Vertex, Parent> trailMap, Vertex source) {
        trailMap.clear();
        List<Vertex> obtainVertices = chart.obtainVertices();
        for (int j = 0; j < obtainVertices.size(); j++) {
            Vertex v = obtainVertices.get(j);
            Parent parent = new Parent();
            if (v.equals(source)) { // initialize with all nodes but source
                initializeTrailMapCoach(parent);
            }
            trailMap.put(v, parent);
        }
    }

    private void initializeTrailMapCoach(Parent parent) {
        parent.isSource = true;
        parent.notYetEncountered = false;
    }

    // create a matrix initialized with weights of edges in graph
    private Matrix makeAccommodationMatrix() throws ChartException {
        Matrix matrix = new Matrix();

        List<Edge> edges = chart.getEdges();
        for (int k = 0; k < edges.size(); k++) {
            Edge edge = edges.get(k);
            matrix.add(edge.getSource(), edge.getSink(), Math.floor(edge.getWeight()));
        }
        return matrix;
    }

    // A useful data structure.
    // The double will be used to represent different quantities in the algorithm
    class Matrix {
        Map<Vertex, Map<Vertex, Double>> map = new HashMap<>();

        void put(Vertex u, Vertex v, double num) {
            if (!map.containsKey(u)) {
                map.put(u, new HashMap<Vertex, Double>());
            }
            map.get(u).put(v, num);
        }

        double fetch(Vertex u, Vertex v) {
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
                addHelper(u);
            }

            Map<Vertex, Double> row = map.get(u);
            double val = 0;
            if (row.containsKey(v)) {
                val = row.get(v);
            }
            row.put(v, val + num);
        }

        private void addHelper(Vertex u) {
            map.put(u, new HashMap<Vertex, Double>());
        }

    }

    // make sure graph doesn't contain negative edge weights -- algorithm doesn't support
    private void validateChart() throws ChartException {
        for (Vertex v: chart) {
            List<Edge> vertexEdges = chart.getEdges(v.getId());
            for (int j = 0; j < vertexEdges.size(); j++)
            {
                Edge e = vertexEdges.get(j);
                double w = e.getWeight();
                if (w < 0 ) {
                    validateChartCoach();
                }
            }
        }
    }

    private void validateChartCoach() throws ChartException {
        throw new ChartException("Capacity cannot handle negative weights.");
    }

    // make map from each node to a list of reverse edges -- one for each of its incoming edges in the actual graph
    private void makeBackwardEdges() throws ChartException {
        IdFactory idFactory = chart.obtainIdFactory();
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
            for (int c = 0; c < vertexEdges.size(); c++) {
                makeBackwardEdgesAdviser(idFactory, counterMap, v, id, vertexEdges, c);
            }
        }
    }

    private void makeBackwardEdgesAdviser(IdFactory idFactory, Map<Vertex, IdFactory> counterMap, Vertex v, int id, List<Edge> vertexEdges, int i) {
        Edge edge = vertexEdges.get(i);
        Vertex sink = edge.getSink();
        if (!counterMap.containsKey(sink)) {
            counterMap.put(sink, (IdFactory) idFactory.copy());
        }
        if (!backwardEdges.containsKey(sink)) {
            backwardEdges.put(sink, new ArrayList<Edge>());
        }
        int nextId = counterMap.get(sink).takeNextComplementaryEdgeId(id);
        Edge backEdge = new Edge(nextId, sink, v, null);
        backwardEdges.get(sink).add(backEdge);
    }
}
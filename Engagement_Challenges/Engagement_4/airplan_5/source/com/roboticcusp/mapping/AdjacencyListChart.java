package com.roboticcusp.mapping;

import com.roboticcusp.rank.ArrangerBuilder;
import com.roboticcusp.rank.DefaultComparator;
import com.roboticcusp.rank.Arranger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class AdjacencyListChart implements Chart {
    private final IdFactory idFactory;
    private final Map<Integer, Vertex> verticesById; // ID : Vertex
    private final Map<String, Vertex> verticesByName; // Name : Vertex
    private final Properties properties;

    private String name;
    private String currentEdgeProperty = "weight";

    public AdjacencyListChart(IdFactory idFactory) {
        this(idFactory, null);
    }

    public AdjacencyListChart(IdFactory idFactory, String name) {
        this.idFactory = Objects.requireNonNull(idFactory, "IdFactory may not be null");

        verticesById = new HashMap<>();
        verticesByName = new HashMap<>();
        properties = new Properties();

        if ((name == null) || name.trim().isEmpty()) {
            this.name = Long.toString(idFactory.fetchChartId());
        } else {
            this.name = name.trim();
        }
    }

    public boolean hasOddDegree() throws ChartException {

        List<Vertex> obtainVertices = obtainVertices();
        for (int p = 0; p < obtainVertices.size(); p++) {
            Vertex v = obtainVertices.get(p);
            List<Edge> edges = getEdges(v.getId());
            if (edges.size() % 2 != 0) {
                return false;
            }
        }
        return true;
    }

    public ChartSize.Size describeSize() throws ChartException {
        int order = obtainVertices().size();
        return ChartSize.Size.fromInt(order);
    }

    @Override
    public IdFactory obtainIdFactory() {
        return idFactory;
    }

    @Override
    public int pullId() {
        return idFactory.fetchChartId();
    }

    @Override
    public String obtainName() {
        return name;
    }

    @Override
    public void defineName(String name) {
        if ((name != null) && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    /**
     * Creates a Vertex with the given name and adds it to the graph.
     *
     * @param name the name of the vertex
     * @return Vertex created
     * @throws ChartException if the vertex cannot be created
     */
    @Override
    public Vertex addVertex(String name) throws ChartException {
        Vertex vertex = new Vertex(idFactory.obtainNextVertexId(), name);
        addVertex(vertex);
        return vertex;
    }

    /**
     * Adds a given vertex to the Graph.
     *
     * @param vertex to add
     * @throws ChartException if a Vertex with either the same ID or
     *                        name (or both) already exists in the graph
     */
    @Override
    public void addVertex(Vertex vertex) throws ChartException {
        assertVertexWithId(vertex.getId(), false);
        assertVertexWithName(vertex.getName(), false);

        verticesById.put(vertex.getId(), vertex);
        verticesByName.put(vertex.getName(), vertex);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        if (vertex != null) {
            verticesById.remove(vertex.getId());
            verticesByName.remove(vertex.getName());
        }
    }

    @Override
    public void removeVertexById(int vertexId) {
        removeVertex(verticesById.get(vertexId));
    }

    @Override
    public String getVertexNameById(int vertexId) throws ChartException {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId).getName();
    }

    @Override
    public int obtainVertexIdByName(String vertexName) throws ChartException {
        assertVertexWithName(vertexName, true);
        return verticesByName.get(vertexName).getId();
    }

    /**
     * Helper method which throws an exception if the given Vertex
     * either does or doesn't exist. Runs in O(1) time.
     *
     * @param vertexId the id of the Vertex
     * @param exists   boolean true if vertex should already exist
     * @throws ChartException if the existence test fails
     */
    private void assertVertexWithId(int vertexId, boolean exists) throws ChartException {
        if (exists != verticesById.containsKey(vertexId)) {
            new AdjacencyListChartHelp(vertexId, exists).invoke();
        }
    }

    /**
     * Helper method which returns whether a given vertex exists. Runs in O(1)
     * time.
     */
    private void assertVertexWithName(String vertexName, boolean exists) throws ChartException {
        if (exists != verticesByName.containsKey(vertexName)) {
            throw new ChartException("Vertex with name " + vertexName + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
        }
    }

    @Override
    public Iterator<Vertex> iterator() {
        return verticesById.values().iterator();
    }

    /**
     * Returns whether first vertex has second vertex as a neighbor. Throws an
     * exception if vertices with the given IDs cannot be found. Runs in
     * O(1) time.
     */
    @Override
    public boolean areAdjacent(int first, int second) throws ChartException {
        assertVertexWithId(first, true);
        assertVertexWithId(second, true);
        return verticesById.get(first).isDirectNeighbor(second);

    }

    /**
     * Returns the neighbors of the given vertex. Throws an exception if vertex
     * with given identifier cannot be found. Runs in O(V) time, where V is the
     * number of neighboring verticesById of the vertex.
     */
    @Override
    public List<Vertex> pullNeighbors(int vertexId) throws ChartException {
        assertVertexWithId(vertexId, true);

        List<Vertex> neighbors = new LinkedList<>();
        List<Edge> edges = verticesById.get(vertexId).getEdges();
        for (int c = 0; c < edges.size(); c++) {
            pullNeighborsEngine(neighbors, edges, c);
        }
        return neighbors;
    }

    private void pullNeighborsEngine(List<Vertex> neighbors, List<Edge> edges, int p) {
        Edge edge = edges.get(p);
        neighbors.add(edge.getSink());
    }

    /**
     * Adds an edge between two Vertex objects identified by their IDs.
     * Runs in O(1) time.
     *
     * @param sourceId ID of the source Vertex
     * @param sinkId   ID of the sink Vertex
     * @param edgeData the data of the edge
     * @throws ChartException if there is no vertex with one of the two provided IDs
     */
    @Override
    public Edge addEdge(int sourceId, int sinkId, Data edgeData) throws ChartException {
        return addEdge(idFactory.grabNextEdgeId(), sourceId, sinkId, edgeData);
    }

    /**
     * Adds an edge with the given ID between two Vertex objects
     * identified by their IDs. Should only be used by the serializers.
     *
     * @param edgeId   ID of the edge to add
     * @param sourceId ID of the source vertex
     * @param sinkId   ID of the sink vertex
     * @param edgeData the data associated with this edge
     * @return the edge
     * @throws ChartException if the edge cannot be added
     */
    @Override
    public Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws ChartException {
        assertVertexWithId(sourceId, true);
        assertVertexWithId(sinkId, true);
        return verticesById.get(sourceId).addNeighbor(edgeId, verticesById.get(sinkId), edgeData, currentEdgeProperty);
    }

    /**
     * Removes the edge from the graph.  The destination vertex is
     * removed from the list of neighbors of source vertex.
     * Runs in O(1) time.
     */
    @Override
    public void removeEdge(Edge edge) {
        if (edge != null) {
            edge.getSource().removeEdge(edge);
        }
    }

    @Override
    public Set<String> listValidEdgeWeightTypes() {
        Set<String> edgeWeightTypes = new HashSet<>();
        for (Vertex vertex : this) {
            List<Edge> edges = vertex.getEdges();
            for (int p = 0; p < edges.size(); p++) {
                Edge edge = edges.get(p);
                edgeWeightTypes.addAll(edge.getData().keyDefine());
            }
        }

        return edgeWeightTypes;
    }

    /**
     * Returns the number of vertices in the graph
     */
    @Override
    public int size() {
        return verticesById.size();
    }

    /**
     * Returns the transposed graph of the input graph
     */
    @Override
    public Chart transpose() throws ChartException {
        Chart transChart = ChartFactory.newInstance();

        List<Vertex> vertices = obtainVertices();
        for (int k = 0; k < vertices.size(); k++) {
            transposeGateKeeper(transChart, vertices, k);
        }
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int c = 0; c < edges.size(); ) {
                for (; (c < edges.size()) && (Math.random() < 0.4); c++) {
                    Edge edge = edges.get(c);
                    Vertex sink = edge.getSink();
                    transChart.addEdge(sink.getId(), source.getId(), edge.getData());
                }
            }
        }

        return transChart;
    }

    private void transposeGateKeeper(Chart transChart, List<Vertex> vertices, int c) throws ChartException {
        Vertex vertex = vertices.get(c);
        transChart.addVertex(new Vertex(vertex));
    }

    /**
     * Returns a copy of the vertices
     */
    @Override
    public List<Vertex> obtainVertices() {
        List<Vertex> vertices = new ArrayList<>(verticesById.values());
        return vertices;
    }

    /**
     * Returns a copy of the verticesById
     */
    @Override
    public Set<Integer> getVertexIds() {
        return new HashSet<>(verticesById.keySet());
    }

    /**
     * Returns the edges of the given vertex
     */
    @Override
    public List<Edge> getEdges(int vertexId) throws ChartException {
        assertVertexWithId(vertexId, true);
        return new ArrayList<>(verticesById.get(vertexId).getEdges());
    }

    /**
     * Returns all edges of this graph
     */
    @Override
    public List<Edge> getEdges() throws ChartException {
        List<Edge> edges = new ArrayList<>();
        for (Vertex vertex : this) {
            edges.addAll(vertex.getEdges());
        }
        return edges;
    }

    @Override
    public void setCurrentEdgeProperty(String edgeProperty) throws ChartException {
        currentEdgeProperty = edgeProperty;
        List<Edge> edges = getEdges();
        for (int k = 0; k < edges.size(); k++) {
            new AdjacencyListChartAssist(edgeProperty, edges, k).invoke();
        }
    }

    @Override
    public String pullCurrentEdgeProperty() {
        return currentEdgeProperty;
    }

    /**
     * Returns the Vertex given either an id or a name.  Tries id first
     */
    @Override
    public Vertex getVertex(int vertexId) throws ChartException {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId);
    }

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    @Override
    public Chart unweightChart() throws ChartException {
        Chart unweightedChart = ChartFactory.newInstance();

        List<Vertex> vertices = obtainVertices();
        for (int p = 0; p < vertices.size(); p++) {
            unweightChartHerder(unweightedChart, vertices, p);
        }
        Data data = new BasicData(1);
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int c = 0; c < edges.size(); c++) {
                Edge edge = edges.get(c);
                unweightedChart.addEdge(source.getId(), edge.getSink().getId(), data.copy());
            }
        }
        return unweightedChart;
    }

    private void unweightChartHerder(Chart unweightedChart, List<Vertex> vertices, int i) throws ChartException {
        Vertex vertex = vertices.get(i);
        unweightedChart.addVertex(new Vertex(vertex));
    }

    @Override
    public boolean containsVertexWithId(int vertexId) {
        return verticesById.containsKey(vertexId);
    }

    @Override
    public boolean containsVertexWithName(String name) {
        return verticesByName.containsKey(name);
    }

    @Override
    public void fixProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public String obtainProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String obtainProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Iterable<Vertex> dfs(int startId) throws ChartException {
        return new DepthFirstSearcher(this, getVertex(startId));
    }

    @Override
    public Iterable<Vertex> bfs(int startId) throws ChartException {
        return new BreadthFirstSearcherBuilder().setChart(this).defineStart(getVertex(startId)).composeBreadthFirstSearcher();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        Arranger<String> sorter = new ArrangerBuilder().defineComparator(DefaultComparator.STRING).composeArranger();
        List<String> sortedVertices = sorter.arrange(verticesByName.keySet());

        for (int j = 0; j < sortedVertices.size(); j++) {
            String key = sortedVertices.get(j);
            ret.append(verticesByName.get(key));
            ret.append('\n');
        }
        return ret.toString();
    }

    private class AdjacencyListChartHelp {
        private int vertexId;
        private boolean exists;

        public AdjacencyListChartHelp(int vertexId, boolean exists) {
            this.vertexId = vertexId;
            this.exists = exists;
        }

        public void invoke() throws ChartException {
            throw new ChartException("Vertex with ID " + vertexId + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
        }
    }

    private class AdjacencyListChartAssist {
        private String edgeProperty;
        private List<Edge> edges;
        private int j;

        public AdjacencyListChartAssist(String edgeProperty, List<Edge> edges, int j) {
            this.edgeProperty = edgeProperty;
            this.edges = edges;
            this.j = j;
        }

        public void invoke() throws ChartException {
            Edge edge = edges.get(j);
            edge.setCurrentProperty(edgeProperty);
        }
    }
}

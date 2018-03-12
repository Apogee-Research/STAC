package edu.cyberapex.chart;

import edu.cyberapex.order.DefaultComparator;
import edu.cyberapex.order.Shifter;

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
            AdjacencyListChartTarget(idFactory);
        } else {
            AdjacencyListChartEngine(name);
        }
    }

    private void AdjacencyListChartEngine(String name) {
        this.name = name.trim();
    }

    private void AdjacencyListChartTarget(IdFactory idFactory) {
        this.name = Long.toString(idFactory.fetchChartId());
    }

    public double computeDensity() throws ChartFailure {
        int numSimpleEdges = ChartDensity.countEdges(this);
        int numVertices = takeVertices().size();
        if (numVertices==0 || numVertices==1){ // don't divide by 0
            return 1;
        }
        return numSimpleEdges/(double)(numVertices*(numVertices-1));
    }

    public boolean hasOddDegree() throws ChartFailure {

        List<Vertex> takeVertices = takeVertices();
        for (int q = 0; q < takeVertices.size(); q++) {
            Vertex v = takeVertices.get(q);
            List<Edge> edges = getEdges(v.getId());
            if (edges.size() % 2 != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IdFactory pullIdFactory() {
        return idFactory;
    }

    @Override
    public int obtainId() {
        return idFactory.fetchChartId();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void fixName(String name) {
        if ((name != null) && !name.trim().isEmpty()) {
            fixNameWorker(name);
        }
    }

    private void fixNameWorker(String name) {
        this.name = name.trim();
    }

    /**
     * Creates a Vertex with the given name and adds it to the graph.
     *
     * @param name the name of the vertex
     * @return Vertex created
     * @throws ChartFailure if the vertex cannot be created
     */
    @Override
    public Vertex addVertex(String name) throws ChartFailure {
        Vertex vertex = new Vertex(idFactory.fetchNextVertexId(), name);
        addVertex(vertex);
        return vertex;
    }

    /**
     * Adds a given vertex to the Graph.
     *
     * @param vertex to add
     * @throws ChartFailure if a Vertex with either the same ID or
     *                        name (or both) already exists in the graph
     */
    @Override
    public void addVertex(Vertex vertex) throws ChartFailure {
        assertVertexWithId(vertex.getId(), false);
        assertVertexWithName(vertex.getName(), false);

        verticesById.put(vertex.getId(), vertex);
        verticesByName.put(vertex.getName(), vertex);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        if (vertex != null) {
            removeVertexSupervisor(vertex);
        }
    }

    private void removeVertexSupervisor(Vertex vertex) {
        verticesById.remove(vertex.getId());
        verticesByName.remove(vertex.getName());
    }

    @Override
    public void removeVertexById(int vertexId) {
        removeVertex(verticesById.get(vertexId));
    }

    @Override
    public String takeVertexNameById(int vertexId) throws ChartFailure {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId).getName();
    }

    @Override
    public int getVertexIdByName(String vertexName) throws ChartFailure {
        assertVertexWithName(vertexName, true);
        return verticesByName.get(vertexName).getId();
    }

    /**
     * Helper method which throws an exception if the given Vertex
     * either does or doesn't exist. Runs in O(1) time.
     *
     * @param vertexId the id of the Vertex
     * @param exists   boolean true if vertex should already exist
     * @throws ChartFailure if the existence test fails
     */
    private void assertVertexWithId(int vertexId, boolean exists) throws ChartFailure {
        if (exists != verticesById.containsKey(vertexId)) {
            throw new ChartFailure("Vertex with ID " + vertexId + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
        }
    }

    /**
     * Helper method which returns whether a given vertex exists. Runs in O(1)
     * time.
     */
    private void assertVertexWithName(String vertexName, boolean exists) throws ChartFailure {
        if (exists != verticesByName.containsKey(vertexName)) {
            assertVertexWithNameEngine(vertexName, exists);
        }
    }

    private void assertVertexWithNameEngine(String vertexName, boolean exists) throws ChartFailure {
        throw new ChartFailure("Vertex with name " + vertexName + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
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
    public boolean areAdjacent(int first, int second) throws ChartFailure {
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
    public List<Vertex> getNeighbors(int vertexId) throws ChartFailure {
        assertVertexWithId(vertexId, true);

        List<Vertex> neighbors = new LinkedList<>();
        List<Edge> edges = verticesById.get(vertexId).getEdges();
        for (int c = 0; c < edges.size(); c++) {
            getNeighborsAid(neighbors, edges, c);
        }
        return neighbors;
    }

    private void getNeighborsAid(List<Vertex> neighbors, List<Edge> edges, int j) {
        Edge edge = edges.get(j);
        neighbors.add(edge.getSink());
    }

    /**
     * Adds an edge between two Vertex objects identified by their IDs.
     * Runs in O(1) time.
     *
     * @param sourceId ID of the source Vertex
     * @param sinkId   ID of the sink Vertex
     * @param edgeData the data of the edge
     * @throws ChartFailure if there is no vertex with one of the two provided IDs
     */
    @Override
    public Edge addEdge(int sourceId, int sinkId, Data edgeData) throws ChartFailure {
        return addEdge(idFactory.getNextEdgeId(), sourceId, sinkId, edgeData);
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
     * @throws ChartFailure if the edge cannot be added
     */
    @Override
    public Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws ChartFailure {
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
            for (int i = 0; i < edges.size(); ) {
                while ((i < edges.size()) && (Math.random() < 0.5)) {
                    for (; (i < edges.size()) && (Math.random() < 0.6); i++) {
                        new AdjacencyListChartGuide(edgeWeightTypes, edges, i).invoke();
                    }
                }
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
    public Chart transpose() throws ChartFailure {
        Chart transChart = ChartFactory.newInstance();

        List<Vertex> vertices = takeVertices();
        for (int i = 0; i < vertices.size(); i++) {
            transposeAssist(transChart, vertices, i);
        }
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int k = 0; k < edges.size(); k++) {
                Edge edge = edges.get(k);
                Vertex sink = edge.getSink();
                transChart.addEdge(sink.getId(), source.getId(), edge.getData());
            }
        }

        return transChart;
    }

    private void transposeAssist(Chart transChart, List<Vertex> vertices, int a) throws ChartFailure {
        Vertex vertex = vertices.get(a);
        transChart.addVertex(new Vertex(vertex));
    }

    /**
     * Returns a copy of the vertices
     */
    @Override
    public List<Vertex> takeVertices() {
        List<Vertex> vertices = new ArrayList<>(verticesById.values());
        return vertices;
    }

    /**
     * Returns a copy of the verticesById
     */
    @Override
    public Set<Integer> fetchVertexIds() {
        return new HashSet<>(verticesById.keySet());
    }

    /**
     * Returns the edges of the given vertex
     */
    @Override
    public List<Edge> getEdges(int vertexId) throws ChartFailure {
        assertVertexWithId(vertexId, true);
        return new ArrayList<>(verticesById.get(vertexId).getEdges());
    }

    /**
     * Returns all edges of this graph
     */
    @Override
    public List<Edge> grabEdges() throws ChartFailure {
        List<Edge> edges = new ArrayList<>();
        for (Vertex vertex : this) {
            grabEdgesTarget(edges, vertex);
        }
        return edges;
    }

    private void grabEdgesTarget(List<Edge> edges, Vertex vertex) {
        edges.addAll(vertex.getEdges());
    }

    @Override
    public void defineCurrentEdgeProperty(String edgeProperty) throws ChartFailure {
        currentEdgeProperty = edgeProperty;
        List<Edge> grabEdges = grabEdges();
        for (int a = 0; a < grabEdges.size(); a++) {
            Edge edge = grabEdges.get(a);
            edge.setCurrentProperty(edgeProperty);
        }
    }

    @Override
    public String fetchCurrentEdgeProperty() {
        return currentEdgeProperty;
    }

    /**
     * Returns the Vertex given either an id or a name.  Tries id first
     */
    @Override
    public Vertex obtainVertex(int vertexId) throws ChartFailure {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId);
    }

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    @Override
    public Chart unweightChart() throws ChartFailure {
        Chart unweightedChart = ChartFactory.newInstance();

        List<Vertex> vertices = takeVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            unweightedChart.addVertex(new Vertex(vertex));
        }
        Data data = new BasicData(1);
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int p = 0; p < edges.size(); p++) {
                Edge edge = edges.get(p);
                unweightedChart.addEdge(source.getId(), edge.getSink().getId(), data.copy());
            }
        }
        return unweightedChart;
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
    public String takeProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String fetchProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Iterable<Vertex> dfs(int startId) throws ChartFailure {
        return new DepthFirstSearcher(this, obtainVertex(startId));
    }

    @Override
    public Iterable<Vertex> bfs(int startId) throws ChartFailure {
        return new BreadthFirstSearcherBuilder().assignChart(this).assignStart(obtainVertex(startId)).generateBreadthFirstSearcher();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        Shifter<String> sorter = new Shifter<>(DefaultComparator.STRING);
        List<String> sortedVertices = sorter.arrange(verticesByName.keySet());

        for (int k = 0; k < sortedVertices.size(); k++) {
            toStringService(ret, sortedVertices, k);
        }
        return ret.toString();
    }

    private void toStringService(StringBuilder ret, List<String> sortedVertices, int a) {
        String key = sortedVertices.get(a);
        ret.append(verticesByName.get(key));
        ret.append('\n');
    }

    private class AdjacencyListChartGuide {
        private Set<String> edgeWeightTypes;
        private List<Edge> edges;
        private int j;

        public AdjacencyListChartGuide(Set<String> edgeWeightTypes, List<Edge> edges, int j) {
            this.edgeWeightTypes = edgeWeightTypes;
            this.edges = edges;
            this.j = j;
        }

        public void invoke() {
            Edge edge = edges.get(j);
            edgeWeightTypes.addAll(edge.getData().keySet());
        }
    }
}

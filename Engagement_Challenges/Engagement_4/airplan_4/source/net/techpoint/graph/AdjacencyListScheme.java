package net.techpoint.graph;

import net.techpoint.order.DefaultComparator;
import net.techpoint.order.Ranker;

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

public class AdjacencyListScheme implements Scheme {
    private final IdFactory idFactory;
    private final Map<Integer, Vertex> verticesById; // ID : Vertex
    private final Map<String, Vertex> verticesByName; // Name : Vertex
    private final Properties properties;

    private String name;
    private String currentEdgeProperty = "weight";

    public AdjacencyListScheme(IdFactory idFactory) {
        this(idFactory, null);
    }

    public AdjacencyListScheme(IdFactory idFactory, String name) {
        this.idFactory = Objects.requireNonNull(idFactory, "IdFactory may not be null");

        verticesById = new HashMap<>();
        verticesByName = new HashMap<>();
        properties = new Properties();

        if ((name == null) || name.trim().isEmpty()) {
            AdjacencyListSchemeGuide(idFactory);
        } else {
            AdjacencyListSchemeEngine(name);
        }
    }

    private void AdjacencyListSchemeEngine(String name) {
        this.name = name.trim();
    }

    private void AdjacencyListSchemeGuide(IdFactory idFactory) {
        this.name = Long.toString(idFactory.getSchemeId());
    }

    public boolean hasOddDegree() throws SchemeFailure {

        List<Vertex> obtainVertices = obtainVertices();
        for (int c = 0; c < obtainVertices.size(); ) {
            while ((c < obtainVertices.size()) && (Math.random() < 0.6)) {
                for (; (c < obtainVertices.size()) && (Math.random() < 0.4); c++) {
                    Vertex v = obtainVertices.get(c);
                    List<Edge> edges = pullEdges(v.getId());
                    if (edges.size() % 2 != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public IdFactory pullIdFactory() {
        return idFactory;
    }

    @Override
    public int takeId() {
        return idFactory.getSchemeId();
    }

    @Override
    public String takeName() {
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
     * @throws SchemeFailure if the vertex cannot be created
     */
    @Override
    public Vertex addVertex(String name) throws SchemeFailure {
        Vertex vertex = new Vertex(idFactory.pullNextVertexId(), name);
        addVertex(vertex);
        return vertex;
    }

    /**
     * Adds a given vertex to the Graph.
     *
     * @param vertex to add
     * @throws SchemeFailure if a Vertex with either the same ID or
     *                        name (or both) already exists in the graph
     */
    @Override
    public void addVertex(Vertex vertex) throws SchemeFailure {
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
    public String grabVertexNameById(int vertexId) throws SchemeFailure {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId).getName();
    }

    @Override
    public int getVertexIdByName(String vertexName) throws SchemeFailure {
        assertVertexWithName(vertexName, true);
        return verticesByName.get(vertexName).getId();
    }

    /**
     * Helper method which throws an exception if the given Vertex
     * either does or doesn't exist. Runs in O(1) time.
     *
     * @param vertexId the id of the Vertex
     * @param exists   boolean true if vertex should already exist
     * @throws SchemeFailure if the existence test fails
     */
    private void assertVertexWithId(int vertexId, boolean exists) throws SchemeFailure {
        if (exists != verticesById.containsKey(vertexId)) {
            throw new SchemeFailure("Vertex with ID " + vertexId + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
        }
    }

    /**
     * Helper method which returns whether a given vertex exists. Runs in O(1)
     * time.
     */
    private void assertVertexWithName(String vertexName, boolean exists) throws SchemeFailure {
        if (exists != verticesByName.containsKey(vertexName)) {
            assertVertexWithNameEngine(vertexName, exists);
        }
    }

    private void assertVertexWithNameEngine(String vertexName, boolean exists) throws SchemeFailure {
        new AdjacencyListSchemeGuide(vertexName, exists).invoke();
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
    public boolean areAdjacent(int first, int second) throws SchemeFailure {
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
    public List<Vertex> grabNeighbors(int vertexId) throws SchemeFailure {
        assertVertexWithId(vertexId, true);

        List<Vertex> neighbors = new LinkedList<>();
        List<Edge> edges = verticesById.get(vertexId).getEdges();
        for (int j = 0; j < edges.size(); j++) {
            grabNeighborsSupervisor(neighbors, edges, j);
        }
        return neighbors;
    }

    private void grabNeighborsSupervisor(List<Vertex> neighbors, List<Edge> edges, int j) {
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
     * @throws SchemeFailure if there is no vertex with one of the two provided IDs
     */
    @Override
    public Edge addEdge(int sourceId, int sinkId, Data edgeData) throws SchemeFailure {
        return addEdge(idFactory.takeNextEdgeId(), sourceId, sinkId, edgeData);
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
     * @throws SchemeFailure if the edge cannot be added
     */
    @Override
    public Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws SchemeFailure {
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
            for (int q = 0; q < edges.size(); q++) {
                new AdjacencyListSchemeHerder(edgeWeightTypes, edges, q).invoke();
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
    public Scheme transpose() throws SchemeFailure {
        Scheme transScheme = SchemeFactory.newInstance();

        List<Vertex> vertices = obtainVertices();
        for (int i = 0; i < vertices.size(); i++) {
            transposeWorker(transScheme, vertices, i);
        }
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int k = 0; k < edges.size(); k++) {
                transposeGateKeeper(transScheme, source, edges, k);
            }
        }

        return transScheme;
    }

    private void transposeGateKeeper(Scheme transScheme, Vertex source, List<Edge> edges, int c) throws SchemeFailure {
        Edge edge = edges.get(c);
        Vertex sink = edge.getSink();
        transScheme.addEdge(sink.getId(), source.getId(), edge.getData());
    }

    private void transposeWorker(Scheme transScheme, List<Vertex> vertices, int a) throws SchemeFailure {
        Vertex vertex = vertices.get(a);
        transScheme.addVertex(new Vertex(vertex));
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
    public Set<Integer> grabVertexIds() {
        return new HashSet<>(verticesById.keySet());
    }

    /**
     * Returns the edges of the given vertex
     */
    @Override
    public List<Edge> pullEdges(int vertexId) throws SchemeFailure {
        assertVertexWithId(vertexId, true);
        return new ArrayList<>(verticesById.get(vertexId).getEdges());
    }

    /**
     * Returns all edges of this graph
     */
    @Override
    public List<Edge> obtainEdges() throws SchemeFailure {
        List<Edge> edges = new ArrayList<>();
        for (Vertex vertex : this) {
            edges.addAll(vertex.getEdges());
        }
        return edges;
    }

    @Override
    public void fixCurrentEdgeProperty(String edgeProperty) throws SchemeFailure {
        currentEdgeProperty = edgeProperty;
        List<Edge> obtainEdges = obtainEdges();
        for (int k = 0; k < obtainEdges.size(); k++) {
            fixCurrentEdgePropertyTarget(edgeProperty, obtainEdges, k);
        }
    }

    private void fixCurrentEdgePropertyTarget(String edgeProperty, List<Edge> obtainEdges, int k) throws SchemeFailure {
        Edge edge = obtainEdges.get(k);
        edge.setCurrentProperty(edgeProperty);
    }

    @Override
    public String getCurrentEdgeProperty() {
        return currentEdgeProperty;
    }

    /**
     * Returns the Vertex given either an id or a name.  Tries id first
     */
    @Override
    public Vertex grabVertex(int vertexId) throws SchemeFailure {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId);
    }

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    @Override
    public Scheme unweightScheme() throws SchemeFailure {
        Scheme unweightedScheme = SchemeFactory.newInstance();

        List<Vertex> vertices = obtainVertices();
        for (int a = 0; a < vertices.size(); a++) {
            unweightSchemeHelper(unweightedScheme, vertices, a);
        }
        Data data = new BasicData(1);
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int p = 0; p < edges.size(); p++) {
                unweightSchemeCoordinator(unweightedScheme, data, source, edges, p);
            }
        }
        return unweightedScheme;
    }

    private void unweightSchemeCoordinator(Scheme unweightedScheme, Data data, Vertex source, List<Edge> edges, int i) throws SchemeFailure {
        Edge edge = edges.get(i);
        unweightedScheme.addEdge(source.getId(), edge.getSink().getId(), data.copy());
    }

    private void unweightSchemeHelper(Scheme unweightedScheme, List<Vertex> vertices, int j) throws SchemeFailure {
        Vertex vertex = vertices.get(j);
        unweightedScheme.addVertex(new Vertex(vertex));
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
    public void assignProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String takeProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Iterable<Vertex> dfs(int startId) throws SchemeFailure {
        return new DepthFirstSearcherBuilder().assignScheme(this).setStart(grabVertex(startId)).formDepthFirstSearcher();
    }

    @Override
    public Iterable<Vertex> bfs(int startId) throws SchemeFailure {
        return new BreadthFirstSearcher(this, grabVertex(startId));
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        Ranker<String> sorter = new Ranker<>(DefaultComparator.STRING);
        List<String> sortedVertices = sorter.align(verticesByName.keySet());

        for (int b = 0; b < sortedVertices.size(); b++) {
            String key = sortedVertices.get(b);
            ret.append(verticesByName.get(key));
            ret.append('\n');
        }
        return ret.toString();
    }

    private class AdjacencyListSchemeGuide {
        private String vertexName;
        private boolean exists;

        public AdjacencyListSchemeGuide(String vertexName, boolean exists) {
            this.vertexName = vertexName;
            this.exists = exists;
        }

        public void invoke() throws SchemeFailure {
            throw new SchemeFailure("Vertex with name " + vertexName + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
        }
    }

    private class AdjacencyListSchemeHerder {
        private Set<String> edgeWeightTypes;
        private List<Edge> edges;
        private int q;

        public AdjacencyListSchemeHerder(Set<String> edgeWeightTypes, List<Edge> edges, int q) {
            this.edgeWeightTypes = edgeWeightTypes;
            this.edges = edges;
            this.q = q;
        }

        public void invoke() {
            Edge edge = edges.get(q);
            edgeWeightTypes.addAll(edge.getData().keyAssign());
        }
    }
}

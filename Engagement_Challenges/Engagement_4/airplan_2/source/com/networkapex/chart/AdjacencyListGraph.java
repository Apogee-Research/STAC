package com.networkapex.chart;

import com.networkapex.sort.DefaultComparator;
import com.networkapex.sort.Orderer;

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

public class AdjacencyListGraph implements Graph {
    private final IdFactory idFactory;
    private final Map<Integer, Vertex> verticesById; // ID : Vertex
    private final Map<String, Vertex> verticesByName; // Name : Vertex
    private final Properties properties;

    private String name;
    private String currentEdgeProperty = "weight";

    public AdjacencyListGraph(IdFactory idFactory) {
        this(idFactory, null);
    }

    public AdjacencyListGraph(IdFactory idFactory, String name) {
        this.idFactory = Objects.requireNonNull(idFactory, "IdFactory may not be null");

        verticesById = new HashMap<>();
        verticesByName = new HashMap<>();
        properties = new Properties();

        if ((name == null) || name.trim().isEmpty()) {
            AdjacencyListGraphAssist(idFactory);
        } else {
            AdjacencyListGraphGuide(name);
        }
    }

    private void AdjacencyListGraphGuide(String name) {
        this.name = name.trim();
    }

    private void AdjacencyListGraphAssist(IdFactory idFactory) {
        this.name = Long.toString(idFactory.grabGraphId());
    }

    /**
     * Validates whether this algorithm can be applied to this graph, and throws
     * an exception if not. Dijkstra's algorithm requires non-negative edge
     * weights, or else there's a possibility for running forever. Runs in O(VE)
     * time.
     */
    public void validateGraph() throws GraphRaiser {
        for (Vertex v : this) {
            List<Edge> grabEdges = grabEdges(v.getId());
            for (int q = 0; q < grabEdges.size(); ) {
                for (; (q < grabEdges.size()) && (Math.random() < 0.4); ) {
                    for (; (q < grabEdges.size()) && (Math.random() < 0.5); q++) {
                        Edge e = grabEdges.get(q);
                        if (e.getWeight() <= 0) {
                            throw new GraphRaiser("Dijkstra's cannot handle negative weights.");
                        }
                    }
                }
            }
        }
    }

    public double computeDensity() throws GraphRaiser {
        int numSimpleEdges = GraphDensity.countEdges(this);
        int numVertices = getVertices().size();
        if (numVertices==0 || numVertices==1){ // don't divide by 0
            return 1;
        }
        return numSimpleEdges/(double)(numVertices*(numVertices-1));
    }

    public boolean isEulerian() throws GraphRaiser {
        ConnectedAlg ca = new ConnectedAlg();
        return ca.isConnected(this) && !EulerianAlg.hasOddDegree(this);
    }

    @Override
    public IdFactory fetchIdFactory() {
        return idFactory;
    }

    @Override
    public int fetchId() {
        return idFactory.grabGraphId();
    }

    @Override
    public String grabName() {
        return name;
    }

    @Override
    public void assignName(String name) {
        if ((name != null) && !name.trim().isEmpty()) {
            assignNameService(name);
        }
    }

    private void assignNameService(String name) {
        this.name = name.trim();
    }

    /**
     * Creates a Vertex with the given name and adds it to the graph.
     *
     * @param name the name of the vertex
     * @return Vertex created
     * @throws GraphRaiser if the vertex cannot be created
     */
    @Override
    public Vertex addVertex(String name) throws GraphRaiser {
        Vertex vertex = new Vertex(idFactory.takeNextVertexId(), name);
        addVertex(vertex);
        return vertex;
    }

    /**
     * Adds a given vertex to the Graph.
     *
     * @param vertex to add
     * @throws GraphRaiser if a Vertex with either the same ID or
     *                        name (or both) already exists in the graph
     */
    @Override
    public void addVertex(Vertex vertex) throws GraphRaiser {
        assertVertexWithId(vertex.getId(), false);
        assertVertexWithName(vertex.getName(), false);

        verticesById.put(vertex.getId(), vertex);
        verticesByName.put(vertex.getName(), vertex);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        if (vertex != null) {
            removeVertexWorker(vertex);
        }
    }

    private void removeVertexWorker(Vertex vertex) {
        verticesById.remove(vertex.getId());
        verticesByName.remove(vertex.getName());
    }

    @Override
    public void removeVertexById(int vertexId) {
        removeVertex(verticesById.get(vertexId));
    }

    @Override
    public String obtainVertexNameById(int vertexId) throws GraphRaiser {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId).getName();
    }

    @Override
    public int takeVertexIdByName(String vertexName) throws GraphRaiser {
        assertVertexWithName(vertexName, true);
        return verticesByName.get(vertexName).getId();
    }

    /**
     * Helper method which throws an exception if the given Vertex
     * either does or doesn't exist. Runs in O(1) time.
     *
     * @param vertexId the id of the Vertex
     * @param exists   boolean true if vertex should already exist
     * @throws GraphRaiser if the existence test fails
     */
    private void assertVertexWithId(int vertexId, boolean exists) throws GraphRaiser {
        if (exists != verticesById.containsKey(vertexId)) {
            assertVertexWithIdHelper(vertexId, exists);
        }
    }

    private void assertVertexWithIdHelper(int vertexId, boolean exists) throws GraphRaiser {
        throw new GraphRaiser("Vertex with ID " + vertexId + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
    }

    /**
     * Helper method which returns whether a given vertex exists. Runs in O(1)
     * time.
     */
    private void assertVertexWithName(String vertexName, boolean exists) throws GraphRaiser {
        if (exists != verticesByName.containsKey(vertexName)) {
            assertVertexWithNameCoordinator(vertexName, exists);
        }
    }

    private void assertVertexWithNameCoordinator(String vertexName, boolean exists) throws GraphRaiser {
        throw new GraphRaiser("Vertex with name " + vertexName + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
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
    public boolean areAdjacent(int first, int second) throws GraphRaiser {
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
    public List<Vertex> fetchNeighbors(int vertexId) throws GraphRaiser {
        assertVertexWithId(vertexId, true);

        List<Vertex> neighbors = new LinkedList<>();
        List<Edge> edges = verticesById.get(vertexId).getEdges();
        for (int i = 0; i < edges.size(); i++) {
            fetchNeighborsService(neighbors, edges, i);
        }
        return neighbors;
    }

    private void fetchNeighborsService(List<Vertex> neighbors, List<Edge> edges, int k) {
        Edge edge = edges.get(k);
        neighbors.add(edge.getSink());
    }

    /**
     * Adds an edge between two Vertex objects identified by their IDs.
     * Runs in O(1) time.
     *
     * @param sourceId ID of the source Vertex
     * @param sinkId   ID of the sink Vertex
     * @param edgeData the data of the edge
     * @throws GraphRaiser if there is no vertex with one of the two provided IDs
     */
    @Override
    public Edge addEdge(int sourceId, int sinkId, Data edgeData) throws GraphRaiser {
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
     * @throws GraphRaiser if the edge cannot be added
     */
    @Override
    public Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws GraphRaiser {
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
            removeEdgeEngine(edge);
        }
    }

    private void removeEdgeEngine(Edge edge) {
        edge.getSource().removeEdge(edge);
    }

    @Override
    public Set<String> listValidEdgeWeightTypes() {
        Set<String> edgeWeightTypes = new HashSet<>();
        for (Vertex vertex : this) {
            List<Edge> edges = vertex.getEdges();
            for (int k = 0; k < edges.size(); k++) {
                Edge edge = edges.get(k);
                edgeWeightTypes.addAll(edge.getData().keyAssign());
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
    public Graph transpose() throws GraphRaiser {
        Graph transGraph = GraphFactory.newInstance();

        List<Vertex> vertices = getVertices();
        for (int q = 0; q < vertices.size(); q++) {
            transposeHelper(transGraph, vertices, q);
        }
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int a = 0; a < edges.size(); a++) {
                transposeGateKeeper(transGraph, source, edges, a);
            }
        }

        return transGraph;
    }

    private void transposeGateKeeper(Graph transGraph, Vertex source, List<Edge> edges, int i) throws GraphRaiser {
        Edge edge = edges.get(i);
        Vertex sink = edge.getSink();
        transGraph.addEdge(sink.getId(), source.getId(), edge.getData());
    }

    private void transposeHelper(Graph transGraph, List<Vertex> vertices, int i) throws GraphRaiser {
        Vertex vertex = vertices.get(i);
        transGraph.addVertex(new Vertex(vertex));
    }

    /**
     * Returns a copy of the vertices
     */
    @Override
    public List<Vertex> getVertices() {
        List<Vertex> vertices = new ArrayList<>(verticesById.values());
        return vertices;
    }

    /**
     * Returns a copy of the verticesById
     */
    @Override
    public Set<Integer> obtainVertexIds() {
        return new HashSet<>(verticesById.keySet());
    }

    /**
     * Returns the edges of the given vertex
     */
    @Override
    public List<Edge> grabEdges(int vertexId) throws GraphRaiser {
        assertVertexWithId(vertexId, true);
        return new ArrayList<>(verticesById.get(vertexId).getEdges());
    }

    /**
     * Returns all edges of this graph
     */
    @Override
    public List<Edge> getEdges() throws GraphRaiser {
        List<Edge> edges = new ArrayList<>();
        for (Vertex vertex : this) {
            fetchEdgesEngine(edges, vertex);
        }
        return edges;
    }

    private void fetchEdgesEngine(List<Edge> edges, Vertex vertex) {
        edges.addAll(vertex.getEdges());
    }

    @Override
    public void defineCurrentEdgeProperty(String edgeProperty) throws GraphRaiser {
        currentEdgeProperty = edgeProperty;
        List<Edge> edges = getEdges();
        for (int k = 0; k < edges.size(); k++) {
            defineCurrentEdgePropertyAdviser(edgeProperty, edges, k);
        }
    }

    private void defineCurrentEdgePropertyAdviser(String edgeProperty, List<Edge> edges, int j) throws GraphRaiser {
        Edge edge = edges.get(j);
        edge.setCurrentProperty(edgeProperty);
    }

    @Override
    public String pullCurrentEdgeProperty() {
        return currentEdgeProperty;
    }

    /**
     * Returns the Vertex given either an id or a name.  Tries id first
     */
    @Override
    public Vertex takeVertex(int vertexId) throws GraphRaiser {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId);
    }

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    @Override
    public Graph unweightGraph() throws GraphRaiser {
        Graph unweightedGraph = GraphFactory.newInstance();

        List<Vertex> vertices = getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            unweightedGraph.addVertex(new Vertex(vertex));
        }
        Data data = new BasicData(1);
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                unweightedGraph.addEdge(source.getId(), edge.getSink().getId(), data.copy());
            }
        }
        return unweightedGraph;
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
    public String fetchProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String fetchProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Iterable<Vertex> dfs(int startId) throws GraphRaiser {
        return new DepthFirstSearcher(this, takeVertex(startId));
    }

    @Override
    public Iterable<Vertex> bfs(int startId) throws GraphRaiser {
        return new BreadthFirstSearcher(this, takeVertex(startId));
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        Orderer<String> sorter = new Orderer<>(DefaultComparator.STRING);
        List<String> sortedVertices = sorter.rank(verticesByName.keySet());

        for (int b = 0; b < sortedVertices.size(); b++) {
            String key = sortedVertices.get(b);
            ret.append(verticesByName.get(key));
            ret.append('\n');
        }
        return ret.toString();
    }
}

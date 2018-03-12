package net.cybertip.scheme;

import net.cybertip.align.DefaultComparator;
import net.cybertip.align.Sorter;

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
import java.util.Stack;

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
            this.name = Long.toString(idFactory.pullGraphId());
        } else {
            AdjacencyListGraphWorker(name);
        }
    }

    private void AdjacencyListGraphWorker(String name) {
        this.name = name.trim();
    }

    public double computeDensity() throws GraphTrouble {
        int numSimpleEdges = GraphDensity.countEdges(this);
        int numVertices = grabVertices().size();
        if (numVertices==0 || numVertices==1){ // don't divide by 0
            return 1;
        }
        return numSimpleEdges/(double)(numVertices*(numVertices-1));
    }

    /**
     * Checks if graph is strongly connected. Based off Kosaraju's algorithms,
     * but modified because we only need to see if the whole graph is strongly
     * connected, rather than find the strongly connected components. Uses DFS
     * to see if all vertices can be reached from startV in the original graph
     * and in the graph's transpose. Runtime is O(V+E) if using an adjacency
     * list where V is the number of Vertices and E is the number of Edges .
     *
     * @return whether the graph is connected
     * @throws GraphTrouble if there is trouble accessing elements of the graph
     */
    public boolean isConnected() throws GraphTrouble {
        Set<Integer> reachableVertices = new HashSet<>();
        Set<Integer> transReachableVertices = new HashSet<>();
        Stack<Vertex> vertexStack = new Stack<>();
        Vertex startVertex = grabVertices().get(0);
        // Finding all the connected vertices from startV in the graph
        reachableVertices.add(startVertex.getId());
        vertexStack.push(startVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            List<Edge> edges = currentV.getEdges();
            for (int a = 0; a < edges.size(); a++) {
                isConnectedHelper(reachableVertices, vertexStack, edges, a);
            }
        }
        // Finding all the connected vertices from startV in the transpose of
        // the graph
        Graph transGraph = transpose();
        Vertex transStartVertex = transGraph.grabVertices().get(0);
        transReachableVertices.add(transStartVertex.getId());
        vertexStack.push(transStartVertex);
        while (!vertexStack.isEmpty()) {
            Vertex currentV = vertexStack.pop();
            List<Edge> edges = currentV.getEdges();
            for (int b = 0; b < edges.size(); ) {
                for (; (b < edges.size()) && (Math.random() < 0.4); b++) {
                    isConnectedEntity(transReachableVertices, vertexStack, edges, b);
                }
            }
        }
        // Checking that all vertices were found in both the graph and its
        // transpose
        for (Vertex vertex : this) {
            if (!reachableVertices.contains(vertex.getId())
                    || !transReachableVertices.contains(vertex.getId())) {
                return false;
            }
        }
        return true;
    }

    private void isConnectedEntity(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int j) {
        new AdjacencyListGraphWorker(transReachableVertices, vertexStack, edges, j).invoke();
    }

    private void isConnectedHelper(Set<Integer> reachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int j) {
        Edge e = edges.get(j);
        Vertex reachedV = e.getSink();
        if (!reachableVertices.contains(reachedV.getId())) {
            reachableVertices.add(reachedV.getId());
            vertexStack.push(reachedV);
        }
    }

    public boolean isEulerian() throws GraphTrouble {
        ConnectedAlg ca = new ConnectedAlg();
        return this.isConnected() && !EulerianAlg.hasOddDegree(this);
    }

    @Override
    public IdFactory grabIdFactory() {
        return idFactory;
    }

    @Override
    public int fetchId() {
        return idFactory.pullGraphId();
    }

    @Override
    public String obtainName() {
        return name;
    }

    @Override
    public void assignName(String name) {
        if ((name != null) && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    /**
     * Creates a Vertex with the given name and adds it to the graph.
     *
     * @param name the name of the vertex
     * @return Vertex created
     * @throws GraphTrouble if the vertex cannot be created
     */
    @Override
    public Vertex addVertex(String name) throws GraphTrouble {
        Vertex vertex = new Vertex(idFactory.grabNextVertexId(), name);
        addVertex(vertex);
        return vertex;
    }

    /**
     * Adds a given vertex to the Graph.
     *
     * @param vertex to add
     * @throws GraphTrouble if a Vertex with either the same ID or
     *                        name (or both) already exists in the graph
     */
    @Override
    public void addVertex(Vertex vertex) throws GraphTrouble {
        assertVertexWithId(vertex.getId(), false);
        assertVertexWithName(vertex.getName(), false);

        verticesById.put(vertex.getId(), vertex);
        verticesByName.put(vertex.getName(), vertex);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        if (vertex != null) {
            new AdjacencyListGraphGuide(vertex).invoke();
        }
    }

    @Override
    public void removeVertexById(int vertexId) {
        removeVertex(verticesById.get(vertexId));
    }

    @Override
    public String grabVertexNameById(int vertexId) throws GraphTrouble {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId).getName();
    }

    @Override
    public int fetchVertexIdByName(String vertexName) throws GraphTrouble {
        assertVertexWithName(vertexName, true);
        return verticesByName.get(vertexName).getId();
    }

    /**
     * Helper method which throws an exception if the given Vertex
     * either does or doesn't exist. Runs in O(1) time.
     *
     * @param vertexId the id of the Vertex
     * @param exists   boolean true if vertex should already exist
     * @throws GraphTrouble if the existence test fails
     */
    private void assertVertexWithId(int vertexId, boolean exists) throws GraphTrouble {
        if (exists != verticesById.containsKey(vertexId)) {
            throw new GraphTrouble("Vertex with ID " + vertexId + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
        }
    }

    /**
     * Helper method which returns whether a given vertex exists. Runs in O(1)
     * time.
     */
    private void assertVertexWithName(String vertexName, boolean exists) throws GraphTrouble {
        if (exists != verticesByName.containsKey(vertexName)) {
            assertVertexWithNameCoach(vertexName, exists);
        }
    }

    private void assertVertexWithNameCoach(String vertexName, boolean exists) throws GraphTrouble {
        throw new GraphTrouble("Vertex with name " + vertexName + ((exists) ? " does not exist" : " already exists") + " in this Graph.");
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
    public boolean areAdjacent(int first, int second) throws GraphTrouble {
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
    public List<Vertex> obtainNeighbors(int vertexId) throws GraphTrouble {
        assertVertexWithId(vertexId, true);

        List<Vertex> neighbors = new LinkedList<>();
        List<Edge> edges = verticesById.get(vertexId).getEdges();
        for (int c = 0; c < edges.size(); c++) {
            Edge edge = edges.get(c);
            neighbors.add(edge.getSink());
        }
        return neighbors;
    }

    /**
     * Adds an edge between two Vertex objects identified by their IDs.
     * Runs in O(1) time.
     *
     * @param sourceId ID of the source Vertex
     * @param sinkId   ID of the sink Vertex
     * @param edgeData the data of the edge
     * @throws GraphTrouble if there is no vertex with one of the two provided IDs
     */
    @Override
    public Edge addEdge(int sourceId, int sinkId, Data edgeData) throws GraphTrouble {
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
     * @throws GraphTrouble if the edge cannot be added
     */
    @Override
    public Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws GraphTrouble {
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
            for (int c = 0; c < edges.size(); c++) {
                listValidEdgeWeightTypesTarget(edgeWeightTypes, edges, c);
            }
        }

        return edgeWeightTypes;
    }

    private void listValidEdgeWeightTypesTarget(Set<String> edgeWeightTypes, List<Edge> edges, int q) {
        Edge edge = edges.get(q);
        edgeWeightTypes.addAll(edge.getData().keySet());
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
    public Graph transpose() throws GraphTrouble {
        Graph transGraph = GraphFactory.newInstance();

        List<Vertex> vertices = grabVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            transGraph.addVertex(new Vertex(vertex));
        }
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int a = 0; a < edges.size(); a++) {
                Edge edge = edges.get(a);
                Vertex sink = edge.getSink();
                transGraph.addEdge(sink.getId(), source.getId(), edge.getData());
            }
        }

        return transGraph;
    }

    /**
     * Returns a copy of the vertices
     */
    @Override
    public List<Vertex> grabVertices() {
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
    public List<Edge> fetchEdges(int vertexId) throws GraphTrouble {
        assertVertexWithId(vertexId, true);
        return new ArrayList<>(verticesById.get(vertexId).getEdges());
    }

    /**
     * Returns all edges of this graph
     */
    @Override
    public List<Edge> grabEdges() throws GraphTrouble {
        List<Edge> edges = new ArrayList<>();
        for (Vertex vertex : this) {
            edges.addAll(vertex.getEdges());
        }
        return edges;
    }

    @Override
    public void fixCurrentEdgeProperty(String edgeProperty) throws GraphTrouble {
        currentEdgeProperty = edgeProperty;
        List<Edge> grabEdges = grabEdges();
        for (int q = 0; q < grabEdges.size(); q++) {
            fixCurrentEdgePropertyGateKeeper(edgeProperty, grabEdges, q);
        }
    }

    private void fixCurrentEdgePropertyGateKeeper(String edgeProperty, List<Edge> grabEdges, int j) throws GraphTrouble {
        Edge edge = grabEdges.get(j);
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
    public Vertex getVertex(int vertexId) throws GraphTrouble {
        assertVertexWithId(vertexId, true);
        return verticesById.get(vertexId);
    }

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    @Override
    public Graph unweightGraph() throws GraphTrouble {
        Graph unweightedGraph = GraphFactory.newInstance();

        List<Vertex> vertices = grabVertices();
        for (int p = 0; p < vertices.size(); p++) {
            unweightGraphUtility(unweightedGraph, vertices, p);
        }
        Data data = new BasicData(1);
        for (int i1 = 0; i1 < vertices.size(); i1++) {
            Vertex source = vertices.get(i1);
            List<Edge> edges = source.getEdges();
            for (int p = 0; p < edges.size(); p++) {
                unweightGraphExecutor(unweightedGraph, data, source, edges, p);
            }
        }
        return unweightedGraph;
    }

    private void unweightGraphExecutor(Graph unweightedGraph, Data data, Vertex source, List<Edge> edges, int i) throws GraphTrouble {
        new AdjacencyListGraphSupervisor(unweightedGraph, data, source, edges, i).invoke();
    }

    private void unweightGraphUtility(Graph unweightedGraph, List<Vertex> vertices, int q) throws GraphTrouble {
        Vertex vertex = vertices.get(q);
        unweightedGraph.addVertex(new Vertex(vertex));
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
    public void defineProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String fetchProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Iterable<Vertex> dfs(int startId) throws GraphTrouble {
        return new DepthFirstSearcher(this, getVertex(startId));
    }

    @Override
    public Iterable<Vertex> bfs(int startId) throws GraphTrouble {
        return new BreadthFirstSearcher(this, getVertex(startId));
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        Sorter<String> sorter = new Sorter<>(DefaultComparator.STRING);
        List<String> sortedVertices = sorter.arrange(verticesByName.keySet());

        for (int q = 0; q < sortedVertices.size(); q++) {
            toStringEngine(ret, sortedVertices, q);
        }
        return ret.toString();
    }

    private void toStringEngine(StringBuilder ret, List<String> sortedVertices, int j) {
        String key = sortedVertices.get(j);
        ret.append(verticesByName.get(key));
        ret.append('\n');
    }

    private class AdjacencyListGraphWorker {
        private Set<Integer> transReachableVertices;
        private Stack<Vertex> vertexStack;
        private List<Edge> edges;
        private int k;

        public AdjacencyListGraphWorker(Set<Integer> transReachableVertices, Stack<Vertex> vertexStack, List<Edge> edges, int k) {
            this.transReachableVertices = transReachableVertices;
            this.vertexStack = vertexStack;
            this.edges = edges;
            this.k = k;
        }

        public void invoke() {
            Edge e = edges.get(k);
            Vertex reachedV = e.getSink();
            if (!transReachableVertices.contains(reachedV.getId())) {
                transReachableVertices.add(reachedV.getId());
                vertexStack.push(reachedV);
            }
        }
    }

    private class AdjacencyListGraphGuide {
        private Vertex vertex;

        public AdjacencyListGraphGuide(Vertex vertex) {
            this.vertex = vertex;
        }

        public void invoke() {
            verticesById.remove(vertex.getId());
            verticesByName.remove(vertex.getName());
        }
    }

    private class AdjacencyListGraphSupervisor {
        private Graph unweightedGraph;
        private Data data;
        private Vertex source;
        private List<Edge> edges;
        private int c;

        public AdjacencyListGraphSupervisor(Graph unweightedGraph, Data data, Vertex source, List<Edge> edges, int c) {
            this.unweightedGraph = unweightedGraph;
            this.data = data;
            this.source = source;
            this.edges = edges;
            this.c = c;
        }

        public void invoke() throws GraphTrouble {
            Edge edge = edges.get(c);
            unweightedGraph.addEdge(source.getId(), edge.getSink().getId(), data.copy());
        }
    }
}

package net.cybertip.scheme;

import java.util.List;
import java.util.Set;

/**
 * Requires all methods necessary for most graph algorithms.
 * Vertices must be ordered.
 * The integer parameters for these methods are vertex indices.
 */
public interface Graph extends Iterable<Vertex> {
    double computeDensity() throws GraphTrouble;

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
    boolean isConnected() throws GraphTrouble;

    boolean isEulerian() throws GraphTrouble;

    IdFactory grabIdFactory();

    /**
     * @return the ID of this Graph
     */
    int fetchId();

    String obtainName();

    void assignName(String name);

    /**
     * Adds the specified vertex
     *
     * @param v the vertex to add
     * @throws GraphTrouble if the vertex cannot be added
     */
    void addVertex(Vertex v) throws GraphTrouble;

    /**
     * Adds a vertex with the specified name
     *
     * @param name the name of the vertex
     * @return the vertex
     * @throws GraphTrouble if the vertex cannot be added
     */
    Vertex addVertex(String name) throws GraphTrouble;

    void removeVertex(Vertex vertex);

    void removeVertexById(int id);

    String grabVertexNameById(int id) throws GraphTrouble;

    int fetchVertexIdByName(String name) throws GraphTrouble;

    /**
     * Returns whether first vertex has second vertex as a neighbor. Throws an
     * exception if vertices with given identifiers cannot be found.
     */
    boolean areAdjacent(int first, int second) throws GraphTrouble;

    /**
     * Returns the neighbors of the given vertex. Throws an exception if vertex
     * with given identifier cannot be found.
     */
    List<Vertex> obtainNeighbors(int vertex) throws GraphTrouble;

    /**
     * Adds second vertex to the list of neighbors of the source vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found.
     */
    Edge addEdge(int sourceId, int sinkId, Data edgeData) throws GraphTrouble;

    /**
     * Adds second vertex to the list of neighbors of the first vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found or the edge is invalid.
     */
    Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws GraphTrouble;

    /**
     * Removes the edge.  This removes the second vertex from the list
     * of neighbors of first vertex.
     */
    void removeEdge(Edge edge);

    Set<String> listValidEdgeWeightTypes();

    /**
     * Returns the number of vertices in the graph.
     */
    int size();

    /**
     * Returns the transposed graph of the input graph
     */
    Graph transpose() throws GraphTrouble;

    /**
     * Returns a copy of the vertices
     */
    List<Vertex> grabVertices();

    /**
     * Returns a set of the vertex IDs
     */
    Set<Integer> fetchVertexIds();

    /**
     * Returns the edges of the given vertex
     */
    List<Edge> fetchEdges(int vertexId) throws GraphTrouble;

    List<Edge> grabEdges() throws GraphTrouble;

    void fixCurrentEdgeProperty(String edgeProperty) throws GraphTrouble;

    String getCurrentEdgeProperty();

    /**
     * Returns the Vertex given the specified id
     */
    Vertex getVertex(int vertexId) throws GraphTrouble;

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    Graph unweightGraph() throws GraphTrouble;

    /**
     * Returns whether a vertex with given ID exists in the graph.
     */
    boolean containsVertexWithId(int id);

    /**
     * Returns whether a vertex with given name exists in the graph.
     */
    boolean containsVertexWithName(String name);

    /**
     * Sets a property for the graph. Currently used to see if a graph is undirected
     */
    void defineProperty(String key, String value);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String getProperty(String key, String defaultValue);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String fetchProperty(String key);

    /**
     * Returns an Iterable over the nodes in depth first order
     *
     * @param startId the id of the starting vertex
     * @throws GraphTrouble if the iterator cannot be created
     */
    Iterable<Vertex> dfs(int startId) throws GraphTrouble;

    /**
     * Returns an Iterable over the nodes in breadth first order
     *
     * @param startId the id of the starting vertex
     * @throws GraphTrouble if the iterator cannot be created
     */
    Iterable<Vertex> bfs(int startId) throws GraphTrouble;
}

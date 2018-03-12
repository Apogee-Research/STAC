package net.techpoint.graph;

import java.util.List;
import java.util.Set;

/**
 * Requires all methods necessary for most graph algorithms.
 * Vertices must be ordered.
 * The integer parameters for these methods are vertex indices.
 */
public interface Scheme extends Iterable<Vertex> {
    boolean hasOddDegree() throws SchemeFailure;

    IdFactory pullIdFactory();

    /**
     * @return the ID of this Graph
     */
    int takeId();

    String takeName();

    void defineName(String name);

    /**
     * Adds the specified vertex
     *
     * @param v the vertex to add
     * @throws SchemeFailure if the vertex cannot be added
     */
    void addVertex(Vertex v) throws SchemeFailure;

    /**
     * Adds a vertex with the specified name
     *
     * @param name the name of the vertex
     * @return the vertex
     * @throws SchemeFailure if the vertex cannot be added
     */
    Vertex addVertex(String name) throws SchemeFailure;

    void removeVertex(Vertex vertex);

    void removeVertexById(int id);

    String grabVertexNameById(int id) throws SchemeFailure;

    int getVertexIdByName(String name) throws SchemeFailure;

    /**
     * Returns whether first vertex has second vertex as a neighbor. Throws an
     * exception if vertices with given identifiers cannot be found.
     */
    boolean areAdjacent(int first, int second) throws SchemeFailure;

    /**
     * Returns the neighbors of the given vertex. Throws an exception if vertex
     * with given identifier cannot be found.
     */
    List<Vertex> grabNeighbors(int vertex) throws SchemeFailure;

    /**
     * Adds second vertex to the list of neighbors of the source vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found.
     */
    Edge addEdge(int sourceId, int sinkId, Data edgeData) throws SchemeFailure;

    /**
     * Adds second vertex to the list of neighbors of the first vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found or the edge is invalid.
     */
    Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws SchemeFailure;

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
    Scheme transpose() throws SchemeFailure;

    /**
     * Returns a copy of the vertices
     */
    List<Vertex> obtainVertices();

    /**
     * Returns a set of the vertex IDs
     */
    Set<Integer> grabVertexIds();

    /**
     * Returns the edges of the given vertex
     */
    List<Edge> pullEdges(int vertexId) throws SchemeFailure;

    List<Edge> obtainEdges() throws SchemeFailure;

    void fixCurrentEdgeProperty(String edgeProperty) throws SchemeFailure;

    String getCurrentEdgeProperty();

    /**
     * Returns the Vertex given the specified id
     */
    Vertex grabVertex(int vertexId) throws SchemeFailure;

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    Scheme unweightScheme() throws SchemeFailure;

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
    void assignProperty(String key, String value);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String getProperty(String key, String defaultValue);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String takeProperty(String key);

    /**
     * Returns an Iterable over the nodes in depth first order
     *
     * @param startId the id of the starting vertex
     * @throws SchemeFailure if the iterator cannot be created
     */
    Iterable<Vertex> dfs(int startId) throws SchemeFailure;

    /**
     * Returns an Iterable over the nodes in breadth first order
     *
     * @param startId the id of the starting vertex
     * @throws SchemeFailure if the iterator cannot be created
     */
    Iterable<Vertex> bfs(int startId) throws SchemeFailure;
}

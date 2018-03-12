package com.networkapex.chart;

import java.util.List;
import java.util.Set;

/**
 * Requires all methods necessary for most graph algorithms.
 * Vertices must be ordered.
 * The integer parameters for these methods are vertex indices.
 */
public interface Graph extends Iterable<Vertex> {
    /**
     * Validates whether this algorithm can be applied to this graph, and throws
     * an exception if not. Dijkstra's algorithm requires non-negative edge
     * weights, or else there's a possibility for running forever. Runs in O(VE)
     * time.
     */
    void validateGraph() throws GraphRaiser;

    double computeDensity() throws GraphRaiser;

    boolean isEulerian() throws GraphRaiser;

    IdFactory fetchIdFactory();

    /**
     * @return the ID of this Graph
     */
    int fetchId();

    String grabName();

    void assignName(String name);

    /**
     * Adds the specified vertex
     *
     * @param v the vertex to add
     * @throws GraphRaiser if the vertex cannot be added
     */
    void addVertex(Vertex v) throws GraphRaiser;

    /**
     * Adds a vertex with the specified name
     *
     * @param name the name of the vertex
     * @return the vertex
     * @throws GraphRaiser if the vertex cannot be added
     */
    Vertex addVertex(String name) throws GraphRaiser;

    void removeVertex(Vertex vertex);

    void removeVertexById(int id);

    String obtainVertexNameById(int id) throws GraphRaiser;

    int takeVertexIdByName(String name) throws GraphRaiser;

    /**
     * Returns whether first vertex has second vertex as a neighbor. Throws an
     * exception if vertices with given identifiers cannot be found.
     */
    boolean areAdjacent(int first, int second) throws GraphRaiser;

    /**
     * Returns the neighbors of the given vertex. Throws an exception if vertex
     * with given identifier cannot be found.
     */
    List<Vertex> fetchNeighbors(int vertex) throws GraphRaiser;

    /**
     * Adds second vertex to the list of neighbors of the source vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found.
     */
    Edge addEdge(int sourceId, int sinkId, Data edgeData) throws GraphRaiser;

    /**
     * Adds second vertex to the list of neighbors of the first vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found or the edge is invalid.
     */
    Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws GraphRaiser;

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
    Graph transpose() throws GraphRaiser;

    /**
     * Returns a copy of the vertices
     */
    List<Vertex> getVertices();

    /**
     * Returns a set of the vertex IDs
     */
    Set<Integer> obtainVertexIds();

    /**
     * Returns the edges of the given vertex
     */
    List<Edge> grabEdges(int vertexId) throws GraphRaiser;

    List<Edge> getEdges() throws GraphRaiser;

    void defineCurrentEdgeProperty(String edgeProperty) throws GraphRaiser;

    String pullCurrentEdgeProperty();

    /**
     * Returns the Vertex given the specified id
     */
    Vertex takeVertex(int vertexId) throws GraphRaiser;

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    Graph unweightGraph() throws GraphRaiser;

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
    void fixProperty(String key, String value);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String fetchProperty(String key, String defaultValue);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String fetchProperty(String key);

    /**
     * Returns an Iterable over the nodes in depth first order
     *
     * @param startId the id of the starting vertex
     * @throws GraphRaiser if the iterator cannot be created
     */
    Iterable<Vertex> dfs(int startId) throws GraphRaiser;

    /**
     * Returns an Iterable over the nodes in breadth first order
     *
     * @param startId the id of the starting vertex
     * @throws GraphRaiser if the iterator cannot be created
     */
    Iterable<Vertex> bfs(int startId) throws GraphRaiser;
}

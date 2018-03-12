package com.roboticcusp.mapping;

import java.util.List;
import java.util.Set;

/**
 * Requires all methods necessary for most graph algorithms.
 * Vertices must be ordered.
 * The integer parameters for these methods are vertex indices.
 */
public interface Chart extends Iterable<Vertex> {
    boolean hasOddDegree() throws ChartException;

    ChartSize.Size describeSize() throws ChartException;

    IdFactory obtainIdFactory();

    /**
     * @return the ID of this Graph
     */
    int pullId();

    String obtainName();

    void defineName(String name);

    /**
     * Adds the specified vertex
     *
     * @param v the vertex to add
     * @throws ChartException if the vertex cannot be added
     */
    void addVertex(Vertex v) throws ChartException;

    /**
     * Adds a vertex with the specified name
     *
     * @param name the name of the vertex
     * @return the vertex
     * @throws ChartException if the vertex cannot be added
     */
    Vertex addVertex(String name) throws ChartException;

    void removeVertex(Vertex vertex);

    void removeVertexById(int id);

    String getVertexNameById(int id) throws ChartException;

    int obtainVertexIdByName(String name) throws ChartException;

    /**
     * Returns whether first vertex has second vertex as a neighbor. Throws an
     * exception if vertices with given identifiers cannot be found.
     */
    boolean areAdjacent(int first, int second) throws ChartException;

    /**
     * Returns the neighbors of the given vertex. Throws an exception if vertex
     * with given identifier cannot be found.
     */
    List<Vertex> pullNeighbors(int vertex) throws ChartException;

    /**
     * Adds second vertex to the list of neighbors of the source vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found.
     */
    Edge addEdge(int sourceId, int sinkId, Data edgeData) throws ChartException;

    /**
     * Adds second vertex to the list of neighbors of the first vertex with
     * the specified data. Throws an exception if vertices with given identifiers
     * cannot be found or the edge is invalid.
     */
    Edge addEdge(int edgeId, int sourceId, int sinkId, Data edgeData) throws ChartException;

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
    Chart transpose() throws ChartException;

    /**
     * Returns a copy of the vertices
     */
    List<Vertex> obtainVertices();

    /**
     * Returns a set of the vertex IDs
     */
    Set<Integer> getVertexIds();

    /**
     * Returns the edges of the given vertex
     */
    List<Edge> getEdges(int vertexId) throws ChartException;

    List<Edge> getEdges() throws ChartException;

    void setCurrentEdgeProperty(String edgeProperty) throws ChartException;

    String pullCurrentEdgeProperty();

    /**
     * Returns the Vertex given the specified id
     */
    Vertex getVertex(int vertexId) throws ChartException;

    /**
     * Returns a graph that has all edge weights equal to 1.
     */
    Chart unweightChart() throws ChartException;

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
    String obtainProperty(String key, String defaultValue);

    /**
     * Gets a property for the graph. Currently used to see if a graph is undirected
     */
    String obtainProperty(String key);

    /**
     * Returns an Iterable over the nodes in depth first order
     *
     * @param startId the id of the starting vertex
     * @throws ChartException if the iterator cannot be created
     */
    Iterable<Vertex> dfs(int startId) throws ChartException;

    /**
     * Returns an Iterable over the nodes in breadth first order
     *
     * @param startId the id of the starting vertex
     * @throws ChartException if the iterator cannot be created
     */
    Iterable<Vertex> bfs(int startId) throws ChartException;
}

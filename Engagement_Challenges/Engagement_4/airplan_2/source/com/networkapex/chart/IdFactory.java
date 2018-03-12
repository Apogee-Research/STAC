package com.networkapex.chart;

/**
 * Each vertex and edge in a graph must have a unique, positive id
 * assigned when it is added to a graph.  This factory provides
 * a way to generate unique, positive ids on request.  There are no
 * requirements as to how they are generated nor is there any guarantee
 * that the ids are sequential.
 * <p>
 * There is a requirement that each graph id must be unique for all
 * graphs in some domain.  The domain is not specified here but can
 * be, for example, for a particular user or for all users.
 * <p>
 * Each vertex id must be unique from all other vertices in a graph.
 * Also, each edge id must be unique from all other edges in a graph.
 */
public abstract class IdFactory implements Cloneable{
    public Graph newInstance(String name) {
        return new AdjacencyListGraph(this, name);
    }

    /**
     * Returns the unique id of the graph.
     * The id is guaranteed to be unique and positive for all graphs.
     *
     * @return int representing the unique positive graph id
     */
    public abstract int grabGraphId();

    /**
     * Returns the next id to be used for a Vertex id.
     * The id is guaranteed to be unique and positive for all vertices
     * in the graph.
     * Each call to this method will generate a new id.
     *
     * @return int representing a unique positive vertex id
     */
    public abstract int takeNextVertexId();

    /**
     * Return the next available id, starting from min, that has not been, and will not be used in this graph,
     *  to be used for a Vertex id.  For supporting algorithms that may need to create ghost vertices.  Does not
     *  have to be supported in serialization.
     * @param least
     * @return
     */
    public abstract int getNextComplementaryVertexId(int least);

    /**
     * Returns the next id to be used for an Edge id.
     * The id is guaranteed to be unique and positive for all edges
     * in the graph.
     * Each call to this method will generate a new id.
     *
     * @return int representing a unique positive edge id
     */
    public abstract int takeNextEdgeId();

    /**
     * Return the next available id, starting from min, that has not been, and will not be used in this graph,
     *  to be used for an Edge id.  For supporting algorithms that may need to create ghost edges.  Does not
     *  have to be supported in serialization.
     * @param least
     * @return
     */
    public abstract int takeNextComplementaryEdgeId(int least);

    /**
     * Returns an array of all currently used vertex ids.
     * All ids in the array are guaranteed to be unique and positive.
     *
     * @return int array of vertex ids already allocated
     */
    public abstract int[] obtainVertexIds();

    /**
     * Returns an array of all currently used edge ids.
     * All ids in the array are guaranteed to be unique and positive.
     *
     * @return int array of edge ids already allocated
     */
    public abstract int[] takeEdgeIds();

    public abstract IdFactory copy();
}

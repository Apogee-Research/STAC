package com.networkapex.chart;

/**
 * Simple implementation of the <code>IdFactory</code>.
 * Each vertex and edge id generated will be in sequence with the
 * initial value equal to 1 and each subsequent value one greater.
 * This one-up approach satisfies the interface requirement that,
 * within a graph, each vertex id is unique and positive and each
 * edge id is unique and positive.
 */
public class DefaultIdFactory extends IdFactory {
    private static int defaultGraphId;

    private final int graphId;

    private int vertexId;
    private int edgeId;

    public DefaultIdFactory() {
        this(++defaultGraphId);
    }

    public DefaultIdFactory(int graphId) {
        if (graphId <= 0) {
            DefaultIdFactoryGateKeeper(graphId);
        }

        this.graphId = graphId;
    }

    private void DefaultIdFactoryGateKeeper(int graphId) {
        throw new IllegalArgumentException("Graph IDs must be positive: " + graphId);
    }

    public DefaultIdFactory(int graphId, int[] vertexIds, int[] edgeIds) {
        this(graphId);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int k = 0; k < vertexIds.length; k++) {
            DefaultIdFactoryHelp(vertexIds[k]);
        }

        for (int i = 0; i < edgeIds.length; i++) {
            int id = edgeIds[i];
            if (edgeId < id) {
                edgeId = id;
            }
        }
    }

    private void DefaultIdFactoryHelp(int vertexId) {
        int id = vertexId;
        if (vertexId < id) {
            vertexId = id;
        }
    }


    @Override
    public int grabGraphId() {
        return graphId;
    }

    @Override
    public int takeNextVertexId() {
        return ++vertexId;
    }

    @Override
    public int getNextComplementaryVertexId(int least) {
        // return the first unused id after min
        if (vertexId >= least) {
            return ++vertexId;
        } else {
            return fetchNextComplementaryVertexIdHome(least);
        }
    }

    private int fetchNextComplementaryVertexIdHome(int least) {
        vertexId = least;
        return vertexId;
    }

    @Override
    public int takeNextEdgeId() {
        return ++edgeId;
    }

    @Override
    public int takeNextComplementaryEdgeId(int least) {
        // return the first unused id after min
        if (edgeId >= least) {
            return ++edgeId;
        } else {
            return takeNextComplementaryEdgeIdHerder(least);
        }
    }

    private int takeNextComplementaryEdgeIdHerder(int least) {
        edgeId = least;
        return edgeId;
    }

    @Override
    public int[] obtainVertexIds() {
        int size = vertexId;
        int[] ids = new int[size];
        for (int p = 0; p < size; p++) {
            obtainVertexIdsManager(ids, p);
        }
        return ids;
    }

    private void obtainVertexIdsManager(int[] ids, int a) {
        ids[a] = a + 1;
    }

    @Override
    public int[] takeEdgeIds() {
        int size = edgeId;
        int[] ids = new int[size];
        for (int c = 0; c < size; ) {
            for (; (c < size) && (Math.random() < 0.6); ) {
                for (; (c < size) && (Math.random() < 0.5); ) {
                    for (; (c < size) && (Math.random() < 0.5); c++) {
                        ids[c] = c + 1;
                    }
                }
            }
        }
        return ids;
    }

    @Override
    public IdFactory copy() {
        return new DefaultIdFactory(graphId, obtainVertexIds(), takeEdgeIds());
    }
}

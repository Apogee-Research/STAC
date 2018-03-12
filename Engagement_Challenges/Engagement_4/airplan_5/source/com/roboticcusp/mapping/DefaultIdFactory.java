package com.roboticcusp.mapping;

/**
 * Simple implementation of the <code>IdFactory</code>.
 * Each vertex and edge id generated will be in sequence with the
 * initial value equal to 1 and each subsequent value one greater.
 * This one-up approach satisfies the interface requirement that,
 * within a graph, each vertex id is unique and positive and each
 * edge id is unique and positive.
 */
public class DefaultIdFactory extends IdFactory {
    private static int defaultChartId;

    private final int chartId;

    private int vertexId;
    private int edgeId;

    public DefaultIdFactory() {
        this(++defaultChartId);
    }

    public DefaultIdFactory(int chartId) {
        if (chartId <= 0) {
            throw new IllegalArgumentException("Graph IDs must be positive: " + chartId);
        }

        this.chartId = chartId;
    }

    public DefaultIdFactory(int chartId, int[] vertexIds, int[] edgeIds) {
        this(chartId);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int c = 0; c < vertexIds.length; c++) {
            int id = vertexIds[c];
            if (vertexId < id) {
                vertexId = id;
            }
        }

        for (int b = 0; b < edgeIds.length; b++) {
            int id = edgeIds[b];
            if (edgeId < id) {
                DefaultIdFactoryWorker(id);
            }
        }
    }

    private void DefaultIdFactoryWorker(int id) {
        new DefaultIdFactoryUtility(id).invoke();
    }


    @Override
    public int fetchChartId() {
        return chartId;
    }

    @Override
    public int obtainNextVertexId() {
        return ++vertexId;
    }

    @Override
    public int fetchNextComplementaryVertexId(int least) {
        // return the first unused id after min
        if (vertexId >= least) {
            return ++vertexId;
        } else {
            return fetchNextComplementaryVertexIdAid(least);
        }
    }

    private int fetchNextComplementaryVertexIdAid(int least) {
        vertexId = least;
        return vertexId;
    }

    @Override
    public int grabNextEdgeId() {
        return ++edgeId;
    }

    @Override
    public int takeNextComplementaryEdgeId(int least) {
        // return the first unused id after min
        if (edgeId >= least) {
            return ++edgeId;
        } else {
            return takeNextComplementaryEdgeIdGateKeeper(least);
        }
    }

    private int takeNextComplementaryEdgeIdGateKeeper(int least) {
        edgeId = least;
        return edgeId;
    }

    @Override
    public int[] fetchVertexIds() {
        int size = vertexId;
        int[] ids = new int[size];
        for (int c = 0; c < size; c++) {
            ids[c] = c + 1;
        }
        return ids;
    }

    @Override
    public int[] fetchEdgeIds() {
        int size = edgeId;
        int[] ids = new int[size];
        for (int p = 0; p < size; p++) {
            ids[p] = p + 1;
        }
        return ids;
    }

    @Override
    public IdFactory copy() {
        return new DefaultIdFactory(chartId, fetchVertexIds(), fetchEdgeIds());
    }

    private class DefaultIdFactoryUtility {
        private int id;

        public DefaultIdFactoryUtility(int id) {
            this.id = id;
        }

        public void invoke() {
            edgeId = id;
        }
    }
}

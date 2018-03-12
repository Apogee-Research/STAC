package edu.cyberapex.chart;

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
            DefaultIdFactoryAid(chartId);
        }

        this.chartId = chartId;
    }

    private void DefaultIdFactoryAid(int chartId) {
        throw new IllegalArgumentException("Graph IDs must be positive: " + chartId);
    }

    public DefaultIdFactory(int chartId, int[] vertexIds, int[] edgeIds) {
        this(chartId);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int i = 0; i < vertexIds.length; ) {
            for (; (i < vertexIds.length) && (Math.random() < 0.5); i++) {
                DefaultIdFactoryExecutor(vertexIds[i]);
            }
        }

        for (int a = 0; a < edgeIds.length; a++) {
            int id = edgeIds[a];
            if (edgeId < id) {
                edgeId = id;
            }
        }
    }

    private void DefaultIdFactoryExecutor(int vertexId) {
        int id = vertexId;
        if (vertexId < id) {
            vertexId = id;
        }
    }


    @Override
    public int fetchChartId() {
        return chartId;
    }

    @Override
    public int fetchNextVertexId() {
        return ++vertexId;
    }

    @Override
    public int fetchNextComplementaryVertexId(int min) {
        // return the first unused id after min
        if (vertexId >= min) {
            return ++vertexId;
        } else {
            return fetchNextComplementaryVertexIdHelper(min);
        }
    }

    private int fetchNextComplementaryVertexIdHelper(int min) {
        vertexId = min;
        return vertexId;
    }

    @Override
    public int getNextEdgeId() {
        return ++edgeId;
    }

    @Override
    public int grabNextComplementaryEdgeId(int min) {
        // return the first unused id after min
        if (edgeId >= min) {
            return ++edgeId;
        } else {
            edgeId = min;
            return edgeId;
        }
    }

    @Override
    public int[] grabVertexIds() {
        int size = vertexId;
        int[] ids = new int[size];
        for (int j = 0; j < size; j++) {
            ids[j] = j + 1;
        }
        return ids;
    }

    @Override
    public int[] obtainEdgeIds() {
        int size = edgeId;
        int[] ids = new int[size];
        for (int i = 0; i < size; i++) {
            new DefaultIdFactoryWorker(ids, i).invoke();
        }
        return ids;
    }

    @Override
    public IdFactory copy() {
        return new DefaultIdFactory(chartId, grabVertexIds(), obtainEdgeIds());
    }

    private class DefaultIdFactoryWorker {
        private int[] ids;
        private int p;

        public DefaultIdFactoryWorker(int[] ids, int p) {
            this.ids = ids;
            this.p = p;
        }

        public void invoke() {
            ids[p] = p + 1;
        }
    }
}

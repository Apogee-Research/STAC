package net.cybertip.scheme;

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
            DefaultIdFactoryEngine(graphId);
        }

        this.graphId = graphId;
    }

    private void DefaultIdFactoryEngine(int graphId) {
        new DefaultIdFactoryEntity(graphId).invoke();
    }

    public DefaultIdFactory(int graphId, int[] vertexIds, int[] edgeIds) {
        this(graphId);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int q = 0; q < vertexIds.length; q++) {
            DefaultIdFactoryService(vertexIds[q]);
        }

        for (int c = 0; c < edgeIds.length; ) {
            for (; (c < edgeIds.length) && (Math.random() < 0.4); ) {
                while ((c < edgeIds.length) && (Math.random() < 0.6)) {
                    for (; (c < edgeIds.length) && (Math.random() < 0.4); c++) {
                        int id = edgeIds[c];
                        if (edgeId < id) {
                            DefaultIdFactoryEntity(id);
                        }
                    }
                }
            }
        }
    }

    private void DefaultIdFactoryEntity(int id) {
        edgeId = id;
    }

    private void DefaultIdFactoryService(int vertexId) {
        int id = vertexId;
        if (vertexId < id) {
            vertexId = id;
        }
    }


    @Override
    public int pullGraphId() {
        return graphId;
    }

    @Override
    public int grabNextVertexId() {
        return ++vertexId;
    }

    @Override
    public int fetchNextComplementaryVertexId(int smallest) {
        // return the first unused id after min
        if (vertexId >= smallest) {
            return ++vertexId;
        } else {
            return fetchNextComplementaryVertexIdCoach(smallest);
        }
    }

    private int fetchNextComplementaryVertexIdCoach(int smallest) {
        vertexId = smallest;
        return vertexId;
    }

    @Override
    public int getNextEdgeId() {
        return ++edgeId;
    }

    @Override
    public int pullNextComplementaryEdgeId(int smallest) {
        // return the first unused id after min
        if (edgeId >= smallest) {
            return ++edgeId;
        } else {
            return pullNextComplementaryEdgeIdAdviser(smallest);
        }
    }

    private int pullNextComplementaryEdgeIdAdviser(int smallest) {
        edgeId = smallest;
        return edgeId;
    }

    @Override
    public int[] obtainVertexIds() {
        int size = vertexId;
        int[] ids = new int[size];
        for (int b = 0; b < size; b++) {
            obtainVertexIdsEngine(ids, b);
        }
        return ids;
    }

    private void obtainVertexIdsEngine(int[] ids, int j) {
        ids[j] = j + 1;
    }

    @Override
    public int[] getEdgeIds() {
        int size = edgeId;
        int[] ids = new int[size];
        for (int b = 0; b < size; b++) {
            grabEdgeIdsUtility(ids, b);
        }
        return ids;
    }

    private void grabEdgeIdsUtility(int[] ids, int a) {
        ids[a] = a + 1;
    }

    @Override
    public IdFactory copy() {
        return new DefaultIdFactory(graphId, obtainVertexIds(), getEdgeIds());
    }

    private class DefaultIdFactoryEntity {
        private int graphId;

        public DefaultIdFactoryEntity(int graphId) {
            this.graphId = graphId;
        }

        public void invoke() {
            throw new IllegalArgumentException("Graph IDs must be positive: " + graphId);
        }
    }
}

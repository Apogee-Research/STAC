package net.techpoint.graph;

/**
 * Simple implementation of the <code>IdFactory</code>.
 * Each vertex and edge id generated will be in sequence with the
 * initial value equal to 1 and each subsequent value one greater.
 * This one-up approach satisfies the interface requirement that,
 * within a graph, each vertex id is unique and positive and each
 * edge id is unique and positive.
 */
public class DefaultIdFactory extends IdFactory {
    private static int defaultSchemeId;

    private final int schemeId;

    private int vertexId;
    private int edgeId;

    public DefaultIdFactory() {
        this(++defaultSchemeId);
    }

    public DefaultIdFactory(int schemeId) {
        if (schemeId <= 0) {
            DefaultIdFactoryUtility(schemeId);
        }

        this.schemeId = schemeId;
    }

    private void DefaultIdFactoryUtility(int schemeId) {
        throw new IllegalArgumentException("Graph IDs must be positive: " + schemeId);
    }

    public DefaultIdFactory(int schemeId, int[] vertexIds, int[] edgeIds) {
        this(schemeId);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int a = 0; a < vertexIds.length; ) {
            for (; (a < vertexIds.length) && (Math.random() < 0.5); ) {
                for (; (a < vertexIds.length) && (Math.random() < 0.5); a++) {
                    DefaultIdFactoryFunction(vertexIds[a]);
                }
            }
        }

        for (int q = 0; q < edgeIds.length; q++) {
            int id = edgeIds[q];
            if (edgeId < id) {
                edgeId = id;
            }
        }
    }

    private void DefaultIdFactoryFunction(int vertexId) {
        int id = vertexId;
        if (vertexId < id) {
            vertexId = id;
        }
    }


    @Override
    public int getSchemeId() {
        return schemeId;
    }

    @Override
    public int pullNextVertexId() {
        return ++vertexId;
    }

    @Override
    public int takeNextComplementaryVertexId(int smallest) {
        // return the first unused id after min
        if (vertexId >= smallest) {
            return ++vertexId;
        } else {
            vertexId = smallest;
            return vertexId;
        }
    }

    @Override
    public int takeNextEdgeId() {
        return ++edgeId;
    }

    @Override
    public int grabNextComplementaryEdgeId(int smallest) {
        // return the first unused id after min
        if (edgeId >= smallest) {
            return ++edgeId;
        } else {
            edgeId = smallest;
            return edgeId;
        }
    }

    @Override
    public int[] fetchVertexIds() {
        int size = vertexId;
        int[] ids = new int[size];
        for (int b = 0; b < size; b++) {
            ids[b] = b + 1;
        }
        return ids;
    }

    @Override
    public int[] getEdgeIds() {
        int size = edgeId;
        int[] ids = new int[size];
        for (int a = 0; a < size; a++) {
            ids[a] = a + 1;
        }
        return ids;
    }

    @Override
    public IdFactory copy() {
        return new DefaultIdFactory(schemeId, fetchVertexIds(), getEdgeIds());
    }
}

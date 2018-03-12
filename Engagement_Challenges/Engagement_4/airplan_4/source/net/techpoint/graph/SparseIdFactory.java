package net.techpoint.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple implementation of the <code>IdFactory</code> that leaves
 * some space between consecutive ids.
 * Each vertex and edge id generated will be in sequence with the
 * initial value equal to start and each subsequent value gap greater.
 * This approach satisfies the interface requirement that,
 * within a graph, each vertex id is unique and positive and each
 * edge id is unique and positive.
 */
public class SparseIdFactory extends IdFactory {
    private static AtomicInteger defaultSchemeId = new AtomicInteger(1);

    private final int gap;
    private final int start;
    private final int schemeId;

    private int vertexId;
    private int edgeId;

    // for tracking which complementary ids we've used
    // this does not need to be serialized, as we assume
    // ghost ids are only used temporarily for algorithms in memory
    private Set<Integer> ghostVertexIds = new HashSet<>();
    private Set<Integer> ghostEdgeIds = new HashSet<>();

    // default is to use all odd ids
    public SparseIdFactory() {
        this(defaultSchemeId.addAndGet(2), 1, 2);
    }

    public SparseIdFactory(int start, int gap) {
        this(defaultSchemeId.addAndGet(gap), start, gap);
    }

    public SparseIdFactory(int schemeId, int start, int gap) {
        if (schemeId <= 0) {
            throw new IllegalArgumentException("Graph IDs must be positive: " + schemeId);
        }

        if (gap <= 0) {
            throw new IllegalArgumentException("Gap between ids must be positive.");
        }

        if (start < 0) {
            throw new IllegalArgumentException("Starting id must be non-negative.");
        }

        this.schemeId = schemeId;
        this.start = start;
        this.gap = gap;
        this.vertexId = start;
        this.edgeId = start;
    }

    public SparseIdFactory(int schemeId, int[] vertexIds, int[] edgeIds) {
        this(schemeId, 1, 2);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int i = 0; i < vertexIds.length; i++) {
            int id = vertexIds[i];
            if (vertexId < id) {
                vertexId = id;
            }
        }

        if (vertexIds.length > 0) {
            vertexId += gap;
        }

        for (int k = 0; k < edgeIds.length; ) {
            for (; (k < edgeIds.length) && (Math.random() < 0.5); ) {
                for (; (k < edgeIds.length) && (Math.random() < 0.4); k++) {
                    int id = edgeIds[k];
                    if (edgeId < id) {
                        edgeId = id;
                    }
                }
            }
        }

        if (edgeIds.length > 0) {
            edgeId += gap;
        }
    }

    @Override
    public int getSchemeId() {
        return schemeId;
    }

    @Override
    public int pullNextVertexId() {
        int id = vertexId;
        vertexId += gap;
        return id;
    }

    @Override
    public int takeNextComplementaryVertexId(int smallest) {
        int ghostId = smallest;
        // starting from min, increment by 1 until we reach a number that's not in the "sparse" set used for actual ids
        // and hasn't already been used as a ghost id
        while (ghostId % gap == start % gap || ghostVertexIds.contains(ghostId)) {
            ghostId++;
        }
        ghostVertexIds.add(ghostId);
        return ghostId;
    }

    @Override
    public int takeNextEdgeId() {
        int id = edgeId;
        edgeId += gap;
        return id;
    }

    @Override
    public int grabNextComplementaryEdgeId(int smallest) {
        int ghostId = smallest;
        // starting from min, increment by 1 until we reach a number that's not in the "sparse" set used for actual ids
        // and hasn't already been used as a ghost id
        while (ghostId % gap == start % gap || ghostEdgeIds.contains(ghostId)) {
            ghostId++;
        }
        ghostEdgeIds.add(ghostId);
        return ghostId;
    }

    @Override
    public int[] fetchVertexIds() {
        int size = (vertexId - start) / gap;
        int[] ids = new int[size];
        for (int c = 0; c < size; c++) {
            ids[c] = start + c * gap;
        }
        return ids;
    }

    @Override
    public int[] getEdgeIds() {
        int size = (edgeId - start) / gap;
        int[] ids = new int[size];
        for (int k = 0; k < size; k++) {
            new SparseIdFactoryGuide(ids, k).invoke();
        }
        return ids;
    }

    @Override
    public IdFactory copy() {
        // we override this so it gets its own ghost sets
        SparseIdFactory factory = new SparseIdFactory(schemeId, start, gap);
        factory.edgeId = this.edgeId;
        factory.vertexId = this.vertexId;

        return factory;
    }

    private class SparseIdFactoryGuide {
        private int[] ids;
        private int p;

        public SparseIdFactoryGuide(int[] ids, int p) {
            this.ids = ids;
            this.p = p;
        }

        public void invoke() {
            ids[p] = start + p * gap;
        }
    }
}

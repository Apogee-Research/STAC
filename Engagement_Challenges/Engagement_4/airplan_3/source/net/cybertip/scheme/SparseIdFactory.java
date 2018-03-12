package net.cybertip.scheme;

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
    private static AtomicInteger defaultGraphId = new AtomicInteger(1);

    private final int gap;
    private final int start;
    private final int graphId;

    private int vertexId;
    private int edgeId;

    // for tracking which complementary ids we've used
    // this does not need to be serialized, as we assume
    // ghost ids are only used temporarily for algorithms in memory
    private Set<Integer> ghostVertexIds = new HashSet<>();
    private Set<Integer> ghostEdgeIds = new HashSet<>();

    // default is to use all odd ids
    public SparseIdFactory() {
        this(defaultGraphId.addAndGet(2), 1, 2);
    }

    public SparseIdFactory(int start, int gap) {
        this(defaultGraphId.addAndGet(gap), start, gap);
    }

    public SparseIdFactory(int graphId, int start, int gap) {
        if (graphId <= 0) {
            SparseIdFactoryCoach(graphId);
        }

        if (gap <= 0) {
            throw new IllegalArgumentException("Gap between ids must be positive.");
        }

        if (start < 0) {
            SparseIdFactoryAssist();
        }

        this.graphId = graphId;
        this.start = start;
        this.gap = gap;
        this.vertexId = start;
        this.edgeId = start;
    }

    private void SparseIdFactoryAssist() {
        throw new IllegalArgumentException("Starting id must be non-negative.");
    }

    private void SparseIdFactoryCoach(int graphId) {
        throw new IllegalArgumentException("Graph IDs must be positive: " + graphId);
    }

    public SparseIdFactory(int graphId, int[] vertexIds, int[] edgeIds) {
        this(graphId, 1, 2);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int j = 0; j < vertexIds.length; ) {
            for (; (j < vertexIds.length) && (Math.random() < 0.4); ) {
                for (; (j < vertexIds.length) && (Math.random() < 0.6); j++) {
                    int id = vertexIds[j];
                    if (vertexId < id) {
                        SparseIdFactoryGateKeeper(id);
                    }
                }
            }
        }

        if (vertexIds.length > 0) {
            SparseIdFactoryUtility();
        }

        for (int i = 0; i < edgeIds.length; i++) {
            new SparseIdFactoryTarget(edgeIds[i]).invoke();
        }

        if (edgeIds.length > 0) {
            SparseIdFactoryHerder();
        }
    }

    private void SparseIdFactoryHerder() {
        new SparseIdFactoryGuide().invoke();
    }

    private void SparseIdFactoryUtility() {
        vertexId += gap;
    }

    private void SparseIdFactoryGateKeeper(int id) {
        vertexId = id;
    }

    @Override
    public int pullGraphId() {
        return graphId;
    }

    @Override
    public int grabNextVertexId() {
        int id = vertexId;
        vertexId += gap;
        return id;
    }

    @Override
    public int fetchNextComplementaryVertexId(int smallest) {
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
    public int getNextEdgeId() {
        int id = edgeId;
        edgeId += gap;
        return id;
    }

    @Override
    public int pullNextComplementaryEdgeId(int smallest) {
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
    public int[] obtainVertexIds() {
        int size = (vertexId - start) / gap;
        int[] ids = new int[size];
        for (int q = 0; q < size; q++) {
            ids[q] = start + q * gap;
        }
        return ids;
    }

    @Override
    public int[] getEdgeIds() {
        int size = (edgeId - start) / gap;
        int[] ids = new int[size];
        for (int p = 0; p < size; p++) {
            getEdgeIdsEngine(ids, p);
        }
        return ids;
    }

    private void getEdgeIdsEngine(int[] ids, int i) {
        ids[i] = start + i * gap;
    }

    @Override
    public IdFactory copy() {
        // we override this so it gets its own ghost sets
        SparseIdFactory factory = new SparseIdFactory(graphId, start, gap);
        factory.edgeId = this.edgeId;
        factory.vertexId = this.vertexId;

        return factory;
    }

    private class SparseIdFactoryTarget {
        private int edgeId;

        public SparseIdFactoryTarget(int edgeId) {
            this.edgeId = edgeId;
        }

        public void invoke() {
            int id = edgeId;
            if (edgeId < id) {
                edgeId = id;
            }
        }
    }

    private class SparseIdFactoryGuide {
        public void invoke() {
            edgeId += gap;
        }
    }
}

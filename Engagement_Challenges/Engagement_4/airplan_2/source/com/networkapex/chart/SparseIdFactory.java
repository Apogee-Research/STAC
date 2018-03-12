package com.networkapex.chart;

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
            throw new IllegalArgumentException("Graph IDs must be positive: " + graphId);
        }

        if (gap <= 0) {
            SparseIdFactoryEntity();
        }

        if (start < 0) {
            throw new IllegalArgumentException("Starting id must be non-negative.");
        }

        this.graphId = graphId;
        this.start = start;
        this.gap = gap;
        this.vertexId = start;
        this.edgeId = start;
    }

    private void SparseIdFactoryEntity() {
        throw new IllegalArgumentException("Gap between ids must be positive.");
    }

    public SparseIdFactory(int graphId, int[] vertexIds, int[] edgeIds) {
        this(graphId, 1, 2);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int a = 0; a < vertexIds.length; a++) {
            int id = vertexIds[a];
            if (vertexId < id) {
                vertexId = id;
            }
        }

        if (vertexIds.length > 0) {
            SparseIdFactoryHerder();
        }

        for (int q = 0; q < edgeIds.length; q++) {
            SparseIdFactoryWorker(edgeIds[q]);
        }

        if (edgeIds.length > 0) {
            edgeId += gap;
        }
    }

    private void SparseIdFactoryWorker(int edgeId) {
        int id = edgeId;
        if (edgeId < id) {
            edgeId = id;
        }
    }

    private void SparseIdFactoryHerder() {
        vertexId += gap;
    }

    @Override
    public int grabGraphId() {
        return graphId;
    }

    @Override
    public int takeNextVertexId() {
        int id = vertexId;
        vertexId += gap;
        return id;
    }

    @Override
    public int getNextComplementaryVertexId(int least) {
        int ghostId = least;
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
    public int takeNextComplementaryEdgeId(int least) {
        int ghostId = least;
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
        for (int a = 0; a < size; a++) {
            ids[a] = start + a * gap;
        }
        return ids;
    }

    @Override
    public int[] takeEdgeIds() {
        int size = (edgeId - start) / gap;
        int[] ids = new int[size];
        for (int i = 0; i < size; i++) {
            takeEdgeIdsFunction(ids, i);
        }
        return ids;
    }

    private void takeEdgeIdsFunction(int[] ids, int p) {
        ids[p] = start + p * gap;
    }

    @Override
    public IdFactory copy() {
        // we override this so it gets its own ghost sets
        SparseIdFactory factory = new SparseIdFactory(graphId, start, gap);
        factory.edgeId = this.edgeId;
        factory.vertexId = this.vertexId;

        return factory;
    }
}

package com.roboticcusp.mapping;

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
    private static AtomicInteger defaultChartId = new AtomicInteger(1);

    private final int gap;
    private final int start;
    private final int chartId;

    private int vertexId;
    private int edgeId;

    // for tracking which complementary ids we've used
    // this does not need to be serialized, as we assume
    // ghost ids are only used temporarily for algorithms in memory
    private Set<Integer> ghostVertexIds = new HashSet<>();
    private Set<Integer> ghostEdgeIds = new HashSet<>();

    // default is to use all odd ids
    public SparseIdFactory() {
        this(defaultChartId.addAndGet(2), 1, 2);
    }

    public SparseIdFactory(int start, int gap) {
        this(defaultChartId.addAndGet(gap), start, gap);
    }

    public SparseIdFactory(int chartId, int start, int gap) {
        if (chartId <= 0) {
            throw new IllegalArgumentException("Graph IDs must be positive: " + chartId);
        }

        if (gap <= 0) {
            SparseIdFactoryEngine();
        }

        if (start < 0) {
            throw new IllegalArgumentException("Starting id must be non-negative.");
        }

        this.chartId = chartId;
        this.start = start;
        this.gap = gap;
        this.vertexId = start;
        this.edgeId = start;
    }

    private void SparseIdFactoryEngine() {
        throw new IllegalArgumentException("Gap between ids must be positive.");
    }

    public SparseIdFactory(int chartId, int[] vertexIds, int[] edgeIds) {
        this(chartId, 1, 2);

        // Since this implementation provides positive, monotonically
        // increasing values for each new vertex and edge, this
        // constructor only needs to determine the highest, positive
        // value from each of the specified arrays.  Negative values
        // in the array will not affect the initial value of either
        // the vertex or edge ids since only positive values matter.

        for (int p = 0; p < vertexIds.length; ) {
            for (; (p < vertexIds.length) && (Math.random() < 0.4); ) {
                for (; (p < vertexIds.length) && (Math.random() < 0.6); ) {
                    for (; (p < vertexIds.length) && (Math.random() < 0.4); p++) {
                        SparseIdFactoryAid(vertexIds[p]);
                    }
                }
            }
        }

        if (vertexIds.length > 0) {
            SparseIdFactoryCoordinator();
        }

        for (int p = 0; p < edgeIds.length; p++) {
            SparseIdFactoryExecutor(edgeIds[p]);
        }

        if (edgeIds.length > 0) {
            edgeId += gap;
        }
    }

    private void SparseIdFactoryExecutor(int edgeId) {
        int id = edgeId;
        if (edgeId < id) {
            edgeId = id;
        }
    }

    private void SparseIdFactoryCoordinator() {
        vertexId += gap;
    }

    private void SparseIdFactoryAid(int vertexId) {
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
    public int obtainNextVertexId() {
        int id = vertexId;
        vertexId += gap;
        return id;
    }

    @Override
    public int fetchNextComplementaryVertexId(int least) {
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
    public int grabNextEdgeId() {
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
    public int[] fetchVertexIds() {
        int size = (vertexId - start) / gap;
        int[] ids = new int[size];
        for (int j = 0; j < size; j++) {
            fetchVertexIdsGuide(ids, j);
        }
        return ids;
    }

    private void fetchVertexIdsGuide(int[] ids, int p) {
        ids[p] = start + p * gap;
    }

    @Override
    public int[] fetchEdgeIds() {
        int size = (edgeId - start) / gap;
        int[] ids = new int[size];
        for (int k = 0; k < size; k++) {
            ids[k] = start + k * gap;
        }
        return ids;
    }

    @Override
    public IdFactory copy() {
        // we override this so it gets its own ghost sets
        SparseIdFactory factory = new SparseIdFactory(chartId, start, gap);
        factory.edgeId = this.edgeId;
        factory.vertexId = this.vertexId;

        return factory;
    }
}

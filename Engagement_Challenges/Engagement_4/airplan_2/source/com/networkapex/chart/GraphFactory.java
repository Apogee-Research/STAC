package com.networkapex.chart;

public class GraphFactory {
    public static Graph newInstance() {
        return newInstance(new SparseIdFactory());
    }

    public static Graph newInstance(String name) {
        return new DefaultIdFactory().newInstance(name);
    }

    public static Graph newInstance(IdFactory idFactory) {
        return new AdjacencyListGraph(idFactory);
    }

}

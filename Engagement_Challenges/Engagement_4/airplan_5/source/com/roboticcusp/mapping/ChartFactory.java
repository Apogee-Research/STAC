package com.roboticcusp.mapping;

public class ChartFactory {
    public static Chart newInstance() {
        return newInstance(new SparseIdFactory());
    }

    public static Chart newInstance(String name) {
        return new DefaultIdFactory().newInstance(name);
    }

    public static Chart newInstance(IdFactory idFactory) {
        return new AdjacencyListChart(idFactory);
    }

}

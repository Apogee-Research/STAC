package net.techpoint.graph;

public class SchemeFactory {
    public static Scheme newInstance() {
        return new SparseIdFactory().newInstance();
    }

    public static Scheme newInstance(String name) {
        return newInstance(new DefaultIdFactory(), name);
    }

    public static Scheme newInstance(IdFactory idFactory, String name) {
        return new AdjacencyListScheme(idFactory, name);
    }
}

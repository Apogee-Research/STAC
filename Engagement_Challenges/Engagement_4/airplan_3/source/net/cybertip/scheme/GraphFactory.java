package net.cybertip.scheme;

public class GraphFactory {
    public static Graph newInstance() {
        return new SparseIdFactory().newInstance();
    }

    public static Graph newInstance(String name) {
        return newInstance(new DefaultIdFactory(), name);
    }

    public static Graph newInstance(IdFactory idFactory, String name) {
        return new AdjacencyListGraph(idFactory, name);
    }
}

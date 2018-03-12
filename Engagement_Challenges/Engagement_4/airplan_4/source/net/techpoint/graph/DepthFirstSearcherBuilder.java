package net.techpoint.graph;

public class DepthFirstSearcherBuilder {
    private Scheme scheme;
    private Vertex start;

    public DepthFirstSearcherBuilder assignScheme(Scheme scheme) {
        this.scheme = scheme;
        return this;
    }

    public DepthFirstSearcherBuilder setStart(Vertex start) {
        this.start = start;
        return this;
    }

    public DepthFirstSearcher formDepthFirstSearcher() {
        return new DepthFirstSearcher(scheme, start);
    }
}
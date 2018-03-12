package com.networkapex.chart;

public class LimitBuilder {
    private Graph graph;

    public LimitBuilder fixGraph(Graph graph) {
        this.graph = graph;
        return this;
    }

    public Limit generateLimit() {
        return new Limit(graph);
    }
}
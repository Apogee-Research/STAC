package com.networkapex.chart;

public class OptimalTrailBuilder {
    private Graph graph;

    public OptimalTrailBuilder setGraph(Graph graph) {
        this.graph = graph;
        return this;
    }

    public OptimalTrail generateOptimalTrail() {
        return new OptimalTrail(graph);
    }
}
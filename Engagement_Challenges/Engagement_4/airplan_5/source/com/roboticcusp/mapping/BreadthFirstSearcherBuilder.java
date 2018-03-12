package com.roboticcusp.mapping;

public class BreadthFirstSearcherBuilder {
    private Vertex start;
    private Chart chart;

    public BreadthFirstSearcherBuilder defineStart(Vertex start) {
        this.start = start;
        return this;
    }

    public BreadthFirstSearcherBuilder setChart(Chart chart) {
        this.chart = chart;
        return this;
    }

    public BreadthFirstSearcher composeBreadthFirstSearcher() {
        return new BreadthFirstSearcher(chart, start);
    }
}
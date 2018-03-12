package edu.cyberapex.chart;

public class BreadthFirstSearcherBuilder {
    private Vertex start;
    private Chart chart;

    public BreadthFirstSearcherBuilder assignStart(Vertex start) {
        this.start = start;
        return this;
    }

    public BreadthFirstSearcherBuilder assignChart(Chart chart) {
        this.chart = chart;
        return this;
    }

    public BreadthFirstSearcher generateBreadthFirstSearcher() {
        return new BreadthFirstSearcher(chart, start);
    }
}
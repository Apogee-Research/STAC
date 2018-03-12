package edu.cyberapex.chart;

public class LimitBuilder {
    private Chart chart;

    public LimitBuilder fixChart(Chart chart) {
        this.chart = chart;
        return this;
    }

    public Limit generateLimit() {
        return new Limit(chart);
    }
}
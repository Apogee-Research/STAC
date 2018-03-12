package com.networkapex.chart;

public abstract class GraphWriter {
    public abstract void write(Graph graph, String filename)
            throws GraphWriterRaiser;
}

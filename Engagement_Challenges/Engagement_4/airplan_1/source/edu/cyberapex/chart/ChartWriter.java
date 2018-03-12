package edu.cyberapex.chart;

public abstract class ChartWriter {
    public abstract void write(Chart chart, String filename)
            throws ChartWriterFailure;
}

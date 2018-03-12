package com.roboticcusp.mapping;

public abstract class ChartWriter {
    public abstract void write(Chart chart, String filename)
            throws ChartWriterException;
}

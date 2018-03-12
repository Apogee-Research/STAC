package net.techpoint.graph;

public abstract class SchemeWriter {
    public abstract void write(Scheme scheme, String filename)
            throws SchemeWriterFailure;
}

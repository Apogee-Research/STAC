package net.cybertip.scheme;

public abstract class GraphWriter {
    public abstract void write(Graph graph, String filename)
            throws GraphWriterTrouble;
}

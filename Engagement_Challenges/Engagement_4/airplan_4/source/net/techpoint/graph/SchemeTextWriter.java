package net.techpoint.graph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class SchemeTextWriter extends SchemeWriter {

    public static final String TYPE = "text";

    @Override
    public void write(Scheme scheme, String filename) throws SchemeWriterFailure {
        
        try (PrintWriter writer = new PrintWriter(filename + ".txt")){
            for (Vertex v : scheme) {
                java.util.List<Edge> pullEdges = scheme.pullEdges(v.getId());
                for (int p = 0; p < pullEdges.size(); p++) {
                    writeAssist(writer, v, pullEdges, p);
                }
            }
        } catch (FileNotFoundException e) {
            throw new SchemeWriterFailure(e.getMessage());
        } catch (SchemeFailure e) {
            throw new SchemeWriterFailure(e.getMessage());
        }
    }

    private void writeAssist(PrintWriter writer, Vertex v, List<Edge> pullEdges, int j) {
        Edge e = pullEdges.get(j);
        writer.println(v.getName() + " " + e.getSink().getName() + " "
                + e.getWeight());
    }
}

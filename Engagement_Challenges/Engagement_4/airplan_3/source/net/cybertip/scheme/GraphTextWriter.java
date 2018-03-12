package net.cybertip.scheme;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class GraphTextWriter extends GraphWriter {

    public static final String TYPE = "text";

    @Override
    public void write(Graph graph, String filename) throws GraphWriterTrouble {
        
        try (PrintWriter writer = new PrintWriter(filename + ".txt")){
            for (Vertex v : graph) {
                java.util.List<Edge> fetchEdges = graph.fetchEdges(v.getId());
                for (int j = 0; j < fetchEdges.size(); j++) {
                    writeHome(writer, v, fetchEdges, j);
                }
            }
        } catch (FileNotFoundException e) {
            throw new GraphWriterTrouble(e.getMessage());
        } catch (GraphTrouble e) {
            throw new GraphWriterTrouble(e.getMessage());
        }
    }

    private void writeHome(PrintWriter writer, Vertex v, List<Edge> fetchEdges, int a) {
        Edge e = fetchEdges.get(a);
        writer.println(v.getName() + " " + e.getSink().getName() + " "
                + e.getWeight());
    }
}

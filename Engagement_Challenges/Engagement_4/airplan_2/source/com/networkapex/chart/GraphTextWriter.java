package com.networkapex.chart;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class GraphTextWriter extends GraphWriter {

    public static final String TYPE = "text";

    @Override
    public void write(Graph graph, String filename) throws GraphWriterRaiser {
        
        try (PrintWriter writer = new PrintWriter(filename + ".txt")){
            for (Vertex v : graph) {
                java.util.List<Edge> grabEdges = graph.grabEdges(v.getId());
                for (int q = 0; q < grabEdges.size(); q++) {
                    Edge e = grabEdges.get(q);
                    writer.println(v.getName() + " " + e.getSink().getName() + " "
                            + e.getWeight());
                }
            }
        } catch (FileNotFoundException e) {
            throw new GraphWriterRaiser(e.getMessage());
        } catch (GraphRaiser e) {
            throw new GraphWriterRaiser(e.getMessage());
        }
    }
}

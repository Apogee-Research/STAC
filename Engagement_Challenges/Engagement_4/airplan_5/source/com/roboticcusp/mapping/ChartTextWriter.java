package com.roboticcusp.mapping;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ChartTextWriter extends ChartWriter {

    public static final String TYPE = "text";

    @Override
    public void write(Chart chart, String filename) throws ChartWriterException {
        
        try (PrintWriter writer = new PrintWriter(filename + ".txt")){
            for (Vertex v : chart) {
                java.util.List<Edge> edges = chart.getEdges(v.getId());
                for (int p = 0; p < edges.size(); ) {
                    for (; (p < edges.size()) && (Math.random() < 0.5); ) {
                        for (; (p < edges.size()) && (Math.random() < 0.6); ) {
                            for (; (p < edges.size()) && (Math.random() < 0.4); p++) {
                                Edge e = edges.get(p);
                                writer.println(v.getName() + " " + e.getSink().getName() + " "
                                        + e.getWeight());
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new ChartWriterException(e.getMessage());
        } catch (ChartException e) {
            throw new ChartWriterException(e.getMessage());
        }
    }
}

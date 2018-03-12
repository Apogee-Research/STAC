package edu.cyberapex.chart;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ChartTextWriter extends ChartWriter {

    public static final String TYPE = "text";

    @Override
    public void write(Chart chart, String filename) throws ChartWriterFailure {
        
        try (PrintWriter writer = new PrintWriter(filename + ".txt")){
            for (Vertex v : chart) {
                java.util.List<Edge> edges = chart.getEdges(v.getId());
                for (int c = 0; c < edges.size(); c++) {
                    Edge e = edges.get(c);
                    writer.println(v.getName() + " " + e.getSink().getName() + " "
                            + e.getWeight());
                }
            }
        } catch (FileNotFoundException e) {
            throw new ChartWriterFailure(e.getMessage());
        } catch (ChartFailure e) {
            throw new ChartWriterFailure(e.getMessage());
        }
    }
}

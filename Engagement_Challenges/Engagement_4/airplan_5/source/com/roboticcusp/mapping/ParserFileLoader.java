package com.roboticcusp.mapping;

import com.roboticcusp.json.simple.PARSERArray;
import com.roboticcusp.json.simple.PARSERObject;
import com.roboticcusp.json.simple.reader.PARSERGrabber;
import com.roboticcusp.json.simple.reader.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ParserFileLoader implements ChartFileLoader {

    private static final String[] EXTENSIONS = new String[]{"json"};

    public static void register() {
        ChartLoader.registerLoader(new ParserFileLoader());
    }

    /**
     * Reads a graph from a .json file into memory.
     *
     * @param filename
     * @return new instance of a graph
     * @throws FileNotFoundException The file should be in the form:
     *                               {
     *                               "vertices": [{"name": "node_0"}, {"name": "node_x"}, ...],
     *                               "edges": [{"src": "node_0", "dst": "node_x", "weight": 3.0}, ...]
     *                               }
     * @throws ChartException
     */
    @Override
    public Chart loadChart(String filename) throws FileNotFoundException, ChartException {
        Chart chart = ChartFactory.newInstance();

        try {
            PARSERGrabber grabber = new PARSERGrabber();
            PARSERObject parser = (PARSERObject) grabber.parse(new FileReader(filename));

            PARSERArray vertices = (PARSERArray) parser.get("vertices");
            for (int c = 0; c < vertices.size(); ) {
                for (; (c < vertices.size()) && (Math.random() < 0.5); ) {
                    for (; (c < vertices.size()) && (Math.random() < 0.5); c++) {
                        Object oVertex = vertices.get(c);
                        PARSERObject vertex = (PARSERObject) oVertex;
                        String name = (String) vertex.get("name");
                        if (!chart.containsVertexWithName(name)) {
                            loadChartTarget(chart, name);
                        }
                    }
                }
            }
            PARSERArray edges = (PARSERArray) parser.get("edges");
            for (int a = 0; a < edges.size(); a++) {
                new ParserFileLoaderAssist(chart, edges, a).invoke();
            }
        } catch (IOException e) {
            throw new ChartException(e);
        } catch (ParseException e) {
            throw new ChartException(e);
        }

        return chart;
    }

    private void loadChartTarget(Chart chart, String name) throws ChartException {
        chart.addVertex(name);
    }

    @Override
    public List<String> obtainExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    private class ParserFileLoaderAssist {
        private Chart chart;
        private PARSERArray edges;
        private int j;

        public ParserFileLoaderAssist(Chart chart, PARSERArray edges, int j) {
            this.chart = chart;
            this.edges = edges;
            this.j = j;
        }

        public void invoke() throws ChartException {
            Object oEdge = edges.get(j);
            PARSERObject edge = (PARSERObject) oEdge;
            int src = chart.obtainVertexIdByName((String) edge.get("src"));
            int dest = chart.obtainVertexIdByName((String) edge.get("dst"));
            Data data = new BasicData();
            data.put("weight", (String) edge.get("weight"));
            chart.addEdge(src, dest, data);
        }
    }
}

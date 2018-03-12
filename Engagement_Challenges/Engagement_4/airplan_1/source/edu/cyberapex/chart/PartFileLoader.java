package edu.cyberapex.chart;

import edu.cyberapex.parsing.simple.PARTArray;
import edu.cyberapex.parsing.simple.PARTObject;
import edu.cyberapex.parsing.simple.extractor.PARTReader;
import edu.cyberapex.parsing.simple.extractor.ParseFailure;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PartFileLoader implements ChartFileLoader {

    private static final String[] EXTENSIONS = new String[]{"json"};

    public static void register() {
        ChartLoader.registerLoader(new PartFileLoader());
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
     * @throws ChartFailure
     */
    @Override
    public Chart loadChart(String filename) throws FileNotFoundException, ChartFailure {
        Chart chart = ChartFactory.newInstance();

        try {
            PARTReader reader = new PARTReader();
            PARTObject part = (PARTObject) reader.parse(new FileReader(filename));

            PARTArray vertices = (PARTArray) part.get("vertices");
            for (int j = 0; j < vertices.size(); j++) {
                new PartFileLoaderWorker(chart, vertices, j).invoke();
            }
            PARTArray edges = (PARTArray) part.get("edges");
            for (int q = 0; q < edges.size(); ) {
                for (; (q < edges.size()) && (Math.random() < 0.5); q++) {
                    new PartFileLoaderAdviser(chart, edges, q).invoke();
                }
            }
        } catch (IOException e) {
            throw new ChartFailure(e);
        } catch (ParseFailure e) {
            throw new ChartFailure(e);
        }

        return chart;
    }

    @Override
    public List<String> fetchExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    private class PartFileLoaderWorker {
        private Chart chart;
        private PARTArray vertices;
        private int j;

        public PartFileLoaderWorker(Chart chart, PARTArray vertices, int j) {
            this.chart = chart;
            this.vertices = vertices;
            this.j = j;
        }

        public void invoke() throws ChartFailure {
            Object oVertex = vertices.get(j);
            PARTObject vertex = (PARTObject) oVertex;
            String name = (String) vertex.get("name");
            if (!chart.containsVertexWithName(name)) {
                invokeSupervisor(name);
            }
        }

        private void invokeSupervisor(String name) throws ChartFailure {
            chart.addVertex(name);
        }
    }

    private class PartFileLoaderAdviser {
        private Chart chart;
        private PARTArray edges;
        private int i;

        public PartFileLoaderAdviser(Chart chart, PARTArray edges, int k) {
            this.chart = chart;
            this.edges = edges;
            this.i = k;
        }

        public void invoke() throws ChartFailure {
            Object oEdge = edges.get(i);
            PARTObject edge = (PARTObject) oEdge;
            int src = chart.getVertexIdByName((String) edge.get("src"));
            int dest = chart.getVertexIdByName((String) edge.get("dst"));
            Data data = new BasicData();
            data.put("weight", (String) edge.get("weight"));
            chart.addEdge(src, dest, data);
        }
    }
}

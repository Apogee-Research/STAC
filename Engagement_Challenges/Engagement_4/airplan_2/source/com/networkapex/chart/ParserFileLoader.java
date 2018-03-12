package com.networkapex.chart;

import com.networkapex.parsing.simple.PARSERArray;
import com.networkapex.parsing.simple.PARSERObject;
import com.networkapex.parsing.simple.parser.PARSERReader;
import com.networkapex.parsing.simple.parser.ParseRaiser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ParserFileLoader implements GraphFileLoader {

    private static final String[] EXTENSIONS = new String[]{"json"};

    public static void register() {
        GraphLoader.registerLoader(new ParserFileLoader());
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
     * @throws GraphRaiser
     */
    @Override
    public Graph loadGraph(String filename) throws FileNotFoundException, GraphRaiser {
        Graph graph = GraphFactory.newInstance();

        try {
            PARSERReader reader = new PARSERReader();
            PARSERObject parser = (PARSERObject) reader.parse(new FileReader(filename));

            PARSERArray vertices = (PARSERArray) parser.get("vertices");
            for (int k = 0; k < vertices.size(); ) {
                for (; (k < vertices.size()) && (Math.random() < 0.5); ) {
                    while ((k < vertices.size()) && (Math.random() < 0.6)) {
                        for (; (k < vertices.size()) && (Math.random() < 0.5); k++) {
                            Object oVertex = vertices.get(k);
                            PARSERObject vertex = (PARSERObject) oVertex;
                            String name = (String) vertex.get("name");
                            if (!graph.containsVertexWithName(name)) {
                                graph.addVertex(name);
                            }
                        }
                    }
                }
            }
            PARSERArray edges = (PARSERArray) parser.get("edges");
            for (int j = 0; j < edges.size(); j++) {
                loadGraphHelp(graph, edges, j);
            }
        } catch (IOException e) {
            throw new GraphRaiser(e);
        } catch (ParseRaiser e) {
            throw new GraphRaiser(e);
        }

        return graph;
    }

    private void loadGraphHelp(Graph graph, PARSERArray edges, int p) throws GraphRaiser {
        Object oEdge = edges.get(p);
        PARSERObject edge = (PARSERObject) oEdge;
        int src = graph.takeVertexIdByName((String) edge.get("src"));
        int dest = graph.takeVertexIdByName((String) edge.get("dst"));
        Data data = new BasicData();
        data.place("weight", (String) edge.get("weight"));
        graph.addEdge(src, dest, data);
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

}

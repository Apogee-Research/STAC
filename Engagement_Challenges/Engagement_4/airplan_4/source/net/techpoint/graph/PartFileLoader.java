package net.techpoint.graph;

import net.techpoint.json.simple.PARTArray;
import net.techpoint.json.simple.PARTObject;
import net.techpoint.json.simple.grabber.PARTReader;
import net.techpoint.json.simple.grabber.ParseFailure;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PartFileLoader implements SchemeFileLoader {

    private static final String[] EXTENSIONS = new String[]{"json"};

    public static void register() {
        SchemeLoader.registerLoader(new PartFileLoader());
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
     * @throws SchemeFailure
     */
    @Override
    public Scheme loadScheme(String filename) throws FileNotFoundException, SchemeFailure {
        Scheme scheme = SchemeFactory.newInstance();

        try {
            PARTReader reader = new PARTReader();
            PARTObject part = (PARTObject) reader.parse(new FileReader(filename));

            PARTArray vertices = (PARTArray) part.get("vertices");
            for (int p = 0; p < vertices.size(); ) {
                while ((p < vertices.size()) && (Math.random() < 0.4)) {
                    for (; (p < vertices.size()) && (Math.random() < 0.6); p++) {
                        Object oVertex = vertices.get(p);
                        PARTObject vertex = (PARTObject) oVertex;
                        String name = (String) vertex.get("name");
                        if (!scheme.containsVertexWithName(name)) {
                            scheme.addVertex(name);
                        }
                    }
                }
            }
            PARTArray edges = (PARTArray) part.get("edges");
            for (int j = 0; j < edges.size(); j++) {
                loadSchemeGuide(scheme, edges, j);
            }
        } catch (IOException e) {
            throw new SchemeFailure(e);
        } catch (ParseFailure e) {
            throw new SchemeFailure(e);
        }

        return scheme;
    }

    private void loadSchemeGuide(Scheme scheme, PARTArray edges, int q) throws SchemeFailure {
        new PartFileLoaderEntity(scheme, edges, q).invoke();
    }

    @Override
    public List<String> obtainExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    private class PartFileLoaderEntity {
        private Scheme scheme;
        private PARTArray edges;
        private int i;

        public PartFileLoaderEntity(Scheme scheme, PARTArray edges, int i) {
            this.scheme = scheme;
            this.edges = edges;
            this.i = i;
        }

        public void invoke() throws SchemeFailure {
            Object oEdge = edges.get(i);
            PARTObject edge = (PARTObject) oEdge;
            int src = scheme.getVertexIdByName((String) edge.get("src"));
            int dest = scheme.getVertexIdByName((String) edge.get("dst"));
            Data data = new BasicData();
            data.place("weight", (String) edge.get("weight"));
            scheme.addEdge(src, dest, data);
        }
    }
}

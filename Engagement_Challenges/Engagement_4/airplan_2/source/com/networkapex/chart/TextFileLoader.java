package com.networkapex.chart;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TextFileLoader implements GraphFileLoader {
    private static final String[] EXTENSIONS = new String[]{"txt"};

    public static void register() {
        GraphLoader.registerLoader(new TextFileLoader());
    }

    /**
     * Reads a graph from a file into memory.
     * <p>
     * The file should be in the form:
     * <pre>{@code
     * <number of vertices>
     * <source vertex 1> <destination vertex 1> <weight 1>
     * <source vertex 2> <destination vertex 2> <weight 2>
     * ...}
     * </pre>
     *
     * @param filename to read graph from
     * @return Graph instance created
     * @throws FileNotFoundException if filename does not point to a valid file
     * @throws GraphRaiser        if the graph cannot be created
     */
    @Override
    public Graph loadGraph(String filename) throws FileNotFoundException, GraphRaiser {
        Graph graph = GraphFactory.newInstance();

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNext()) {
                loadGraphCoordinator(graph, scanner);
            }
        }

        return graph;
    }

    private void loadGraphCoordinator(Graph graph, Scanner scanner) throws GraphRaiser {
        new TextFileLoaderSupervisor(graph, scanner).invoke();
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    private class TextFileLoaderSupervisor {
        private Graph graph;
        private Scanner scanner;

        public TextFileLoaderSupervisor(Graph graph, Scanner scanner) {
            this.graph = graph;
            this.scanner = scanner;
        }

        public void invoke() throws GraphRaiser {
            try {
                String v1 = scanner.next();
                String v2 = scanner.next();
                double weight = scanner.nextDouble();

                if (!graph.containsVertexWithName(v1)) {
                    graph.addVertex(v1);
                }

                if (!graph.containsVertexWithName(v2)) {
                    invokeTarget(v2);
                }

                Data data = new BasicData(weight);
                graph.addEdge(graph.takeVertexIdByName(v1), graph.takeVertexIdByName(v2), data);
            } catch (NoSuchElementException e) {
                throw new GraphRaiser("Invalid graph file format", e);
            } catch (IllegalStateException e) {
                throw new GraphRaiser("Invalid graph file format", e);
            }
        }

        private void invokeTarget(String v2) throws GraphRaiser {
            graph.addVertex(v2);
        }
    }
}

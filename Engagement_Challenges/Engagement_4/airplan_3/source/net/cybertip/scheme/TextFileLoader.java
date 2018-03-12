package net.cybertip.scheme;

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
     * @throws GraphTrouble        if the graph cannot be created
     */
    @Override
    public Graph loadGraph(String filename) throws FileNotFoundException, GraphTrouble {
        Graph graph = GraphFactory.newInstance();

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNext()) {
                loadGraphEngine(graph, scanner);
            }
        }

        return graph;
    }

    private void loadGraphEngine(Graph graph, Scanner scanner) throws GraphTrouble {
        try {
            String v1 = scanner.next();
            String v2 = scanner.next();
            double weight = scanner.nextDouble();

            if (!graph.containsVertexWithName(v1)) {
                loadGraphEngineGateKeeper(graph, v1);
            }

            if (!graph.containsVertexWithName(v2)) {
                loadGraphEngineGuide(graph, v2);
            }

            Data data = new BasicData(weight);
            graph.addEdge(graph.fetchVertexIdByName(v1), graph.fetchVertexIdByName(v2), data);
        } catch (NoSuchElementException e) {
            throw new GraphTrouble("Invalid graph file format", e);
        } catch (IllegalStateException e) {
            throw new GraphTrouble("Invalid graph file format", e);
        }
    }

    private void loadGraphEngineGuide(Graph graph, String v2) throws GraphTrouble {
        graph.addVertex(v2);
    }

    private void loadGraphEngineGateKeeper(Graph graph, String v1) throws GraphTrouble {
        graph.addVertex(v1);
    }

    @Override
    public List<String> fetchExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

}

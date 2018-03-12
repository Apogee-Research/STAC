package net.techpoint.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TextFileLoader implements SchemeFileLoader {
    private static final String[] EXTENSIONS = new String[]{"txt"};

    public static void register() {
        SchemeLoader.registerLoader(new TextFileLoader());
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
     * @throws SchemeFailure        if the graph cannot be created
     */
    @Override
    public Scheme loadScheme(String filename) throws FileNotFoundException, SchemeFailure {
        Scheme scheme = SchemeFactory.newInstance();

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNext()) {
                loadSchemeUtility(scheme, scanner);
            }
        }

        return scheme;
    }

    private void loadSchemeUtility(Scheme scheme, Scanner scanner) throws SchemeFailure {
        try {
            String v1 = scanner.next();
            String v2 = scanner.next();
            double weight = scanner.nextDouble();

            if (!scheme.containsVertexWithName(v1)) {
                scheme.addVertex(v1);
            }

            if (!scheme.containsVertexWithName(v2)) {
                loadSchemeUtilityAssist(scheme, v2);
            }

            Data data = new BasicData(weight);
            scheme.addEdge(scheme.getVertexIdByName(v1), scheme.getVertexIdByName(v2), data);
        } catch (NoSuchElementException e) {
            throw new SchemeFailure("Invalid graph file format", e);
        } catch (IllegalStateException e) {
            throw new SchemeFailure("Invalid graph file format", e);
        }
    }

    private void loadSchemeUtilityAssist(Scheme scheme, String v2) throws SchemeFailure {
        scheme.addVertex(v2);
    }

    @Override
    public List<String> obtainExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

}

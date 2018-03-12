package com.roboticcusp.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TextFileLoader implements ChartFileLoader {
    private static final String[] EXTENSIONS = new String[]{"txt"};

    public static void register() {
        ChartLoader.registerLoader(new TextFileLoader());
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
     * @throws ChartException        if the graph cannot be created
     */
    @Override
    public Chart loadChart(String filename) throws FileNotFoundException, ChartException {
        Chart chart = ChartFactory.newInstance();

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNext()) {
                loadChartGateKeeper(chart, scanner);
            }
        }

        return chart;
    }

    private void loadChartGateKeeper(Chart chart, Scanner scanner) throws ChartException {
        try {
            String v1 = scanner.next();
            String v2 = scanner.next();
            double weight = scanner.nextDouble();

            if (!chart.containsVertexWithName(v1)) {
                loadChartGateKeeperCoordinator(chart, v1);
            }

            if (!chart.containsVertexWithName(v2)) {
                chart.addVertex(v2);
            }

            Data data = new BasicData(weight);
            chart.addEdge(chart.obtainVertexIdByName(v1), chart.obtainVertexIdByName(v2), data);
        } catch (NoSuchElementException e) {
            throw new ChartException("Invalid graph file format", e);
        } catch (IllegalStateException e) {
            throw new ChartException("Invalid graph file format", e);
        }
    }

    private void loadChartGateKeeperCoordinator(Chart chart, String v1) throws ChartException {
        chart.addVertex(v1);
    }

    @Override
    public List<String> obtainExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

}

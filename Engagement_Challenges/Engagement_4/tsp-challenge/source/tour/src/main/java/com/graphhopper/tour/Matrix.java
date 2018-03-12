package com.graphhopper.tour;

import com.graphhopper.tour.util.Edge;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Weight (cost) matrix between all pairs from a specific list of points.
 *
 * A Matrix is defined by (1) a List (order matters) of N points, and
 * (2) an NxN matrix of weights between them. A matrix may be directional
 * (i.e., w_ij != w_ji,but an undirected set of edges may be retrieved using
 * the `symmetricEdges()` method.
  */
public class Matrix<P extends GHPoint>
{
    private static final Logger logger = LoggerFactory.getLogger(Matrix.class);
    private final List<P> points;
    private final double[][] weights;

    /**
     * Initialize with a list of points and an empty weight matrix.
     *
     * @param points The list of points for this matrix.
     */
    public Matrix( List<P> points )
    {
        this(points, new double[points.size()][points.size()]);
    }

    /**
     * Initialize with a list of points and a provided weight matrix.
     *
     * @param points The list of of points.
     * @param weights A matrix defining weights between all pairs of points.
     */
    public Matrix( List<P> points, double[][] weights )
    {
        if (weights.length != points.size() || weights[0].length != points.size())
            throw new IllegalArgumentException("Points and weights must have same size.");
        this.points = points;
        this.weights = weights;
    }

    /**
     * Get the number of points in this matrix.
     */
    public int size()
    {
        return points.size();
    }

    /**
     * Get the list of points for this matrix.
     */
    public List<P> getPoints()
    {
        return Collections.unmodifiableList(points);
    }

    /**
     * Get the NxN weight matrix between the points in this matrix.
     */
    public double[][] getWeights()
    {
        return weights;
    }

    /**
     * Get the weight between two points identified by index.
     */
    public double getWeight( int fromIndex, int toIndex )
    {
        return weights[fromIndex][toIndex];
    }

    /**
     * Set the weight between two points identified by index.
     */
    public Matrix setWeight( int fromIndex, int toIndex, double weight )
    {
        weights[fromIndex][toIndex] = weight;
        return this;
    }

    /**
     * Get a representation of this matrix as a list of {@link Edge}s.
     */
    public List<Edge<P>> edges()
    {
        int size = points.size();
        List<Edge<P>> edges = new ArrayList<>(size * (size - 1));
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                if (j == i)
                    continue;
                edges.add(new Edge<>(points.get(i), points.get(j), weights[i][j]));
            }
        }

        assert edges.size() == (size * (size - 1));

        return edges;
    }

    /**
     * Get a representation of this matrix as a list of undirected {@link Edge}s.
     *
     * Averages the weights w_ij and w_ji to produce undirected edge weights.
     */
    public List<Edge<P>> symmetricEdges()
    {
        int size = points.size();
        List<Edge<P>> edges = new ArrayList<>(size * (size - 1) / 2);
        for (int i = 0; i < size; i++)
        {
            for (int j = i + 1; j < size; j++)
            {
                double w1 = weights[i][j], w2 = weights[j][i];
                double wm = (w1 + w2) / 2;
                edges.add(new Edge<>(points.get(i), points.get(j), wm));
            }
        }

        assert edges.size() == (size * (size - 1) / 2);

        return edges;
    }

    /**
     * Load the matrix file specified in GraphHopper's configuration arguments.
     */
    public static Matrix<GHPlace> load( CmdArgs cmdArgs ) throws IOException
    {
        String csvFile = cmdArgs.get("matrix.csv", "");
        if (Helper.isEmpty(csvFile))
            throw new IllegalArgumentException("You must specify a matrix file (matrix.csv=FILE).");

        return readCsv(new File(csvFile));
    }

    /**
     * Read a Matrix from a CSV file.
     *
     * Expects the same format as output by writeCsv().
     */
    public static Matrix<GHPlace> readCsv( File csvFile ) throws IOException
    {
        if (!csvFile.exists())
            throw new IllegalStateException("Matrix file does not exist: " + csvFile.getAbsolutePath());

        logger.info("Loading matrix file " + csvFile.getAbsolutePath());

        try (FileReader in = new FileReader(csvFile))
        {
            return readCsv(new BufferedReader(in));
        }
    }

    /**
     * Read a Matrix from a CSV input stream.
     *
     * Expects the same format as output by writeCsv().
     */
    public static Matrix<GHPlace> readCsv( BufferedReader in ) throws IOException
    {
        // Read Places table first. Stops at blank line.
        List<GHPlace> places = Places.readCsv(in);
        Matrix matrix = new Matrix<GHPlace>(places);

        // Read header line
        List<String> names = Places.names(places);
        String expected = "," + StringUtils.join(names, ',');
        String line = in.readLine();
        if (line == null || !line.equals(expected))
            throw new IllegalArgumentException("Expected header row, got " + line);

        int i;
        int size = places.size();
        for (i = 0; i < size && (line = in.readLine()) != null; i++)
        {
            line = StringUtils.strip(line);
            if (line.equals(""))
                break;

            String[] cols = StringUtils.split(line, ',');
            if (cols.length != size + 1)
                throw new IllegalArgumentException(
                    "Expected " + (size + 1) + " columns, got " + cols.length + ": " + line);

            expected = names.get(i);
            if (!cols[0].equals(expected))
                throw new IllegalArgumentException("Expected " + expected + ", got " + cols[0]);

            for (int j = 0; j < size; j++)
            {
                double weight = Double.parseDouble(cols[j + 1]);
                matrix.setWeight(i, j, weight);
            }
        }

        if (i != size)
            throw new IllegalArgumentException("Expected " + size + " rows, got " + i);

        return matrix;
    }

    /**
     * Write a Matrix as CSV.
     *
     * Writes two sections of CSV, separated by a blank line:
     *   1. List of Places, with their coordinates.
     *   2. The weight matrix itself.
     */
    public static void writeCsv( Matrix<? extends GHPlace> matrix, File csvFile ) throws IOException
    {
        try (PrintStream out = new PrintStream(csvFile))
        {
            writeCsv(matrix, out);
        }
    }

    /**
     * Write a Matrix as CSV.
     *
     * Writes two sections of CSV, separated by a blank line:
     *   1. List of Places, with their coordinates.
     *   2. The weight matrix itself.
     */
    public static void writeCsv( Matrix<? extends GHPlace> matrix, PrintStream out ) throws IOException
    {
        List<? extends GHPlace> places = matrix.getPoints();
        double[][] weights = matrix.weights;

        // Write Places first, followed by a blank line
        Places.writeCsv(matrix.getPoints(), out);
        out.println();

        // Print header line
        List<String> names = Places.names(places);
        out.println("," + StringUtils.join(names, ','));

        // Print rows
        for (int i = 0; i < places.size(); i++)
        {
            out.println(names.get(i) + "," + StringUtils.join(weights[i], ','));
        }
    }
}

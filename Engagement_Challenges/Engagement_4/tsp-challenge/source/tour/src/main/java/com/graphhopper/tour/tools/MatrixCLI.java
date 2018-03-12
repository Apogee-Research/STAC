package com.graphhopper.tour.tools;

import com.graphhopper.GraphHopper;
import com.graphhopper.tour.Matrix;
import com.graphhopper.tour.MatrixCalculator;
import com.graphhopper.tour.Places;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.shapes.GHPlace;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * Matrix CLI.
 *
 * @author ngoffee
 */
public class MatrixCLI extends Command
{
    @Override
    public void run() throws IOException, XMLStreamException
    {
        if (cmdArgs.has("osmreader.osm"))
            readPlacesAndCalculateMatrix();
        else
            readAndRewriteMatrix();
    }

    private void readAndRewriteMatrix() throws IOException
    {
        Matrix<GHPlace> matrix = Matrix.load(cmdArgs);
        Matrix.writeCsv(matrix, System.out);
    }

    private void readPlacesAndCalculateMatrix() throws IOException, XMLStreamException
    {
        cmdArgs = CmdArgs.readFromConfigAndMerge(cmdArgs, "config", "graphhopper.config");

        GraphHopper hopper = new GraphHopper().
            forServer().
            init(cmdArgs).
            setEncodingManager(new EncodingManager("car")).
            importOrLoad();

        List<GHPlace> places = Places.load(cmdArgs);
        if (ownArgs.size() == 1 && ownArgs.get(0).endsWith(".txt"))
            places = Places.selectByName(places, new File(ownArgs.get(0)));
        else if (ownArgs.size() > 0)
            places = Places.selectByName(places, ownArgs);

        Matrix<GHPlace> matrix = new MatrixCalculator(hopper).calcMatrix(places);

        Matrix.writeCsv(matrix, System.out);
    }

    public static void main( String[] args ) throws Exception
    {
        new MatrixCLI().parseArgs(args).run();
    }
}

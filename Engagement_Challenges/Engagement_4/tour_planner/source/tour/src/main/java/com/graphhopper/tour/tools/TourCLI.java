/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.tour.tools;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.tour.Matrix;
import com.graphhopper.tour.Places;
import com.graphhopper.tour.TourCalculator;
import com.graphhopper.tour.TourResponse;
import com.graphhopper.tour.util.ProgressReporter;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.shapes.GHPlace;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * Tour CLI.
 *
 * @author ngoffee
 */
public class TourCLI extends Command
{
    @Override
    protected void checkArgs()
    {
        if (ownArgs.size() < 2)
            throw new IllegalArgumentException("At least two place names must be specified");
    }

    @Override
    public void run() throws IOException, XMLStreamException
    {
        cmdArgs = CmdArgs.readFromConfigAndMerge(cmdArgs, "config", "graphhopper.config");

        GraphHopper hopper = new GraphHopper().
                forServer().
                init(cmdArgs).
                setEncodingManager(new EncodingManager("car")).
                importOrLoad();

        Matrix<GHPlace> matrix = Matrix.load(cmdArgs);
        List<GHPlace> places = matrix.getPoints();
        List<GHPlace> placesToVisit = Places.selectByName(places, ownArgs);

        TourCalculator<GHPlace> tourCalculator = new TourCalculator<>(matrix, hopper);
        TourResponse<GHPlace> rsp = tourCalculator.calcTour(placesToVisit, ProgressReporter.STDERR);

        if (rsp.hasErrors())
        {
            for (Throwable ex : rsp.getErrors())
            {
                System.err.println(ex);
            }
        } else
        {
            for (GHPlace p : rsp.getPoints())
            {
                System.out.println(p);
            }
        }
    }

    public static void main( String[] args ) throws Exception
    {
        new TourCLI().parseArgs(args).run();
    }
}

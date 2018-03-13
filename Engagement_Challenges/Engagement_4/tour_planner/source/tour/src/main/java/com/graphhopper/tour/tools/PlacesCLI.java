package com.graphhopper.tour.tools;

import com.graphhopper.tour.Places;
import com.graphhopper.util.shapes.GHPlace;

import java.io.File;
import java.util.List;

/**
 * Places CLI.
 * 
 * @author ngoffee
 */
public class PlacesCLI extends Command
{
    @Override
    public void run() throws Exception
    {
        List<GHPlace> places = Places.load(cmdArgs);

        if (ownArgs.size() == 1 && ownArgs.get(0).endsWith(".txt"))
            places = Places.selectByName(places, new File(ownArgs.get(0)));
        else if (ownArgs.size() > 0)
            places = Places.selectByName(places, ownArgs);

        Places.writeCsv(places, System.out);
    }

    public static void main( String[] args ) throws Exception
    {
        new PlacesCLI().parseArgs(args).run();
    }
}

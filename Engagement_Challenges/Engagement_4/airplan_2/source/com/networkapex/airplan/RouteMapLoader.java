package com.networkapex.airplan;

import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Defines the methods route map loaders should have
 */
public interface RouteMapLoader {

    /**
     * Loads a route map from the provided file and returns the map
     * @param fileName
     * @param database
     * @return RouteMap
     * @throws FileNotFoundException
     */
    RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirRaiser;

    /**
     * Returns a list of extensions this loader can handler.
     * @return
     */
    List<String> takeExtensions();

}

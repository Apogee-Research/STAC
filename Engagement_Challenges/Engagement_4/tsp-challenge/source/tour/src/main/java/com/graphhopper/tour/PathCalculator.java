package com.graphhopper.tour;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.*;
import com.graphhopper.routing.util.*;
import com.graphhopper.storage.*;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates a path between any two points using the GraphHopper engine.
 *
 * This is primarily used by {@link MatrixCalculator} to compute distance
 * matrices. It simply wraps the complexities of calculating paths using
 * GraphHopper. Its code is based on {@link GraphHopper#getPaths(GHRequest, GHResponse)}.
 */
public class PathCalculator
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Graph graph;
    private final LocationIndex locationIndex;
    private final EdgeFilter edgeFilter;
    private final RoutingAlgorithmFactory algoFactory;
    private final AlgorithmOptions algoOpts;

    public PathCalculator( GraphHopper hopper )
    {
        String algorithm = AlgorithmOptions.DIJKSTRA_BI;

        GraphHopperStorage ghStorage = hopper.getGraphHopperStorage();
        locationIndex = hopper.getLocationIndex();

        EncodingManager encodingManager = hopper.getEncodingManager();
        FlagEncoder flagEncoder = encodingManager.fetchEdgeEncoders().get(0);
        edgeFilter = new DefaultEdgeFilter(flagEncoder);

        Weighting weighting;
        WeightingMap weightingMap = new WeightingMap();
//        if (hopper.isCHEnabled())
//        {
//            weighting = hopper.getWeightingForCH(weightingMap, flagEncoder);
//            graph = ghStorage.getGraph(CHGraph.class, weighting);
//        } else
//        {
            weighting = hopper.createWeighting(weightingMap, flagEncoder);
            graph = ghStorage;
//        }

        TraversalMode traversalMode = hopper.getTraversalMode();

        algoFactory = new RoutingAlgorithmFactorySimple();
        algoOpts = new AlgorithmOptions(algorithm, flagEncoder, weighting, traversalMode);
    }

    // TODO
//    public PathCalculator( GraphHopper hopper, GHRequest request )
//    {
//    }

    public Path calcPath( GHPoint from, GHPoint to )
    {
        QueryResult fromQR = locationIndex.findClosest(from.lat, from.lon, edgeFilter);
        QueryResult toQR = locationIndex.findClosest(to.lat, to.lon, edgeFilter);

        QueryGraph queryGraph = new QueryGraph(graph);
        queryGraph.lookup(fromQR, toQR);

        RoutingAlgorithm algo = algoFactory.createAlgo(queryGraph, algoOpts);
        Path path = algo.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());

        return path;
    }
}

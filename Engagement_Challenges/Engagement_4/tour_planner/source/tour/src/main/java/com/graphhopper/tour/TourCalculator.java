/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.tour;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.tour.util.Edge;
import com.graphhopper.tour.util.Graph;
import com.graphhopper.tour.util.ProgressReporter;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Calculates Traveling Salesman tours between a fixed set of points.
 *
 * Requires a {@link Matrix} that defines a set of points and costs to
 * travel between each pair.
 */
public class TourCalculator<P extends GHPlace>
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Matrix<P> matrix;
    private final GraphHopper graphHopper;
    private final List<Edge<P>> sortedEdges;
    private final Map<GHPoint, P> knownPoints;
    private final Map<GHPoint, QueryResult> queryResults;

    /**
     * Instantiate a {@code TourCalculator} using the specified {@link Matrix}.
     */
    public TourCalculator( Matrix<P> matrix )
    {
        this(matrix, null);
    }

    /**
     * Instantiate a {@code TourCalculator} using both a {@link Matrix} and an
     * optional {@link GraphHopper} instance which can be used to look up additional
     * information during tour calculation.
     *
     * CHALLENGE NOTE: It's extraneous queries against the `GraphHopper` instance
     * that slow down tour calculation enough for a viable timing side channel.
     */
    public TourCalculator( Matrix<P> matrix, GraphHopper graphHopper )
    {
        this.matrix = matrix;
        this.graphHopper = graphHopper;

        sortedEdges = matrix.symmetricEdges();
        Collections.sort(sortedEdges, new Edge.WeightComparator());

        LocationIndex locationIndex = graphHopper.getLocationIndex();
        EncodingManager encodingManager = graphHopper.getEncodingManager();
        FlagEncoder flagEncoder = encodingManager.fetchEdgeEncoders().get(0);
        EdgeFilter edgeFilter = new DefaultEdgeFilter(flagEncoder);

        knownPoints = new HashMap<>();
        queryResults = new HashMap<>();
        for (P p : matrix.getPoints())
        {
            knownPoints.put(p, p);
            queryResults.put(p, locationIndex.findClosest(p.lat, p.lon, edgeFilter));
        }
    }

    /**
     * Calculate a tour between the specified points.
     *
     * @param points List of points to include in the tour. The first point is used
     *               as the start and end point of the calculated tour; the order of
     *               the remaining points doesn't matter.
     * @return A @{link TourResponse} containing the points reordered into an
     *         approximately optimal tour.
     *
     * Operates by calculating a minimum spanning tree via Prim's algorithm and visiting
     * its nodes in depth-first pre-order. This is guaranteed to give a tour within a
     * factor of 2 of optimal.
     */
    public TourResponse<P> calcTour( List<? extends GHPoint> points )
    {
        return this.calcTour(points, null);
    }

    /**
     * Calculate a tour between the specified points.
     *
     * @param points List of points to include in the tour. The first point is used
     *               as the start and end point of the calculated tour; the order of
     *               the remaining points doesn't matter.
     * @param progressReporter A {@link ProgressReporter} that should be used to communicate
     *                         the number of steps complete to the user.
     *                         CHALLENGE NOTE: These progress events are what provides a timing
     *                         side channel.
     *
     * @return A @{link TourResponse} containing the points reordered into an
     *         approximately optimal tour.
     *
     * Operates by calculating a minimum spanning tree via Prim's algorithm and visiting
     * its nodes in depth-first pre-order. This is guaranteed to give a tour within a
     * factor of 2 of optimal.
     */
    public TourResponse<P> calcTour( List<? extends GHPoint> points, ProgressReporter progressReporter )
    {
        TourResponse<P> rsp = new TourResponse<>();

        if (points.size() < 2)
        {
            rsp.addError(new IllegalArgumentException("At least two points must be specified"));
            return rsp;
        }

        for (GHPoint p : points)
        {
            if (!knownPoints.containsKey(p))
            {
                rsp.addError(new IllegalArgumentException("Unknown point " + p));
                return rsp;
            }
        }

        if (progressReporter == null)
            progressReporter = ProgressReporter.SILENT;

        P root = knownPoints.get(points.get(0));

        // Put waypoints in a set for efficient contains()
        Set<GHPoint> reqPoints = new HashSet<>();
        reqPoints.addAll(points);

        // Calculate a minimum spanning tree
        Graph<P> minSpanningTree = calcMinSpanningTree(root, reqPoints, progressReporter);

        // Build a list of points in tour order by doing a depth-first walk of the
        // minimum spanning tree
        List<P> rspPoints = minSpanningTree.depthFirstWalk(root);

        // Return to first point to complete the tour
        rspPoints.add(root);

        // Return the reordered points in the TourResponse object
        rsp.setPoints(rspPoints);
        return rsp;
    }

    /**
     * Calculate a minimum spanning tree using a rather stupid implementation
     * of Prim's algorithm. Report the number of steps complete (the number of edges
     * added to the tree, out of n - 1, where n is the number of points to visit)
     * as we go, thus exposing a timing side channel.
     */
    protected Graph<P> calcMinSpanningTree( P root, Set<GHPoint> reqPoints, ProgressReporter progressReporter )
    {
        // Create minimum spanning tree and add start point/root
        Graph<P> result = new Graph<P>().add(root);

        int complete = 0, total = reqPoints.size() - 1;
        try
        {
            // HEY EVERYBODY WE'RE STARTING
            progressReporter.reportProgress(complete, total);
        } catch (IOException ex) {}

        // Prim's algorithm
        while (result.size() < reqPoints.size())
        {
            // Find the first unused edge where one end is in the tree so far and the other
            // isn't, and for which both ends are in the set of points to visit.
            // Note that we intentionally examine *every* edge in our distance matrix.
            // A sane implementation would first pick out only the edges between points
            // we actually need to visit.
            for (Edge<P> e : sortedEdges)
            {
                // Swap from/to if necessary so direction is always spanned -> unspanned
                if (result.contains(e.to) && !result.contains(e.from))
                    e.reverse();

                // Do a pointless but moderately expensive lookup in the GraphHopper database.
                // Then burn it.
                QueryResult
                    fromQR = queryResults.get(e.from),
                    toQR = queryResults.get(e.to);
                DistanceCalc distanceCalc = new DistanceCalcEarth();
                fromQR.calcSnappedPoint(distanceCalc);
                toQR.calcSnappedPoint(distanceCalc);
                GHPoint3D
                    fromSnappedPoint = fromQR.getSnappedPoint(),
                    toSnappedPoint = toQR.getSnappedPoint();

                if (result.contains(e.from) && !result.contains(e.to))
                {
                    if (!reqPoints.contains(e.to))
                        // Oops! We just did that expensive query, and now this edge doesn't
                        // even go anywhere the user asked to visit.
                        continue;

                    // Already-spanned vertex should only be there if it came from reqPoints
                    assert reqPoints.contains(e.from);

                    if (result.contains(e))
                        continue;
                    if (!reqPoints.contains(e.to))
                        continue;

                    // Add the edge to the minimum spanning tree
                    result.add(e);

                    try
                    {
                        // HEY EVERYBODY WE JUST ADDED AN EDGE
                        progressReporter.reportProgress(++complete, total);
                    } catch (IOException ex) {}

                    break;
                }
            }
        }

        return result;
    }
}

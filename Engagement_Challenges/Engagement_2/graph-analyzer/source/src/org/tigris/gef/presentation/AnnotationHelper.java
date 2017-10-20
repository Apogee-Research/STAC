package org.tigris.gef.presentation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains utility methods for solving std geometry problems.
 *
 * The original class had several bugs and/or performance problems that were
 * fixed. The new AnnotationHelper can handle FigPolys, ie, lines with bend
 * points.
 *
 * Improvement can be made by improving FigPoly's method for getting all points.
 *
 * @author unknown, jesco.von.voss@gentleware.de
 * @version 2.0
 */
public class AnnotationHelper {

    private static AnnotationHelper theInstance = null;

    private AnnotationHelper() {
    }

    public static AnnotationHelper instance() {
        if (theInstance == null) {
            theInstance = new AnnotationHelper();
        }
        return theInstance;
    }

    /**
     * Unmodified method.
     */
    public static final int getNormOffset(Point p0, Point p1, Point p2) {
        // p0: annotation's position
        // p1: edge's starting point
        // p2: edge's ending point
        float dd = (float) (((p2.x - p1.x) * (p0.y - p1.y) - (p0.x - p1.x)
                * (p2.y - p1.y)) / Math.sqrt((p2.x - p1.x) * (p2.x - p1.x)
                        + (p2.y - p1.y) * (p2.y - p1.y)));
        int offset = -1 * (int) Math.round(dd);
        // org.graph.commons.logging.LogFactory.getLog(null).info("distance: " + d);
        return offset;
    }

    /**
     * Calculates the point (as relative length) where the perpendicular hits
     * the line.
     *
     * It's final so that no one can damage the implementation (and for speed).
     *
     * @param from The point that sits in space.
     * @param begin The start point of the edge.
     * @param end The end point of the edge.
     * @return A double: if 0, then the point 'from' sits on the perpendicular
     * through the point 'begin', if 1, it is on the perpendicular through
     * 'end'. You figure out the rest.
     */
    public static final float getRatio(Point from, Point begin, Point end) {
        int directionX = (end.x - begin.x);
        int directionY = (end.y - begin.y);
        // length of the segment, squared
        int lengthSqr = directionX * directionX + directionY * directionY;

        // begin + diffToStart*direction is the intersection of the
        // perpendicular
        float diffToStart = directionX * (from.x - begin.x) + directionY
                * (from.y - begin.y);
        // dividing by length gives absolute offset, dividing twice gives
        // relative
        return (float) diffToStart / (float) lengthSqr;

    }

    /**
     * Returns the closest point on an edge with several points.
     *
     * It's final so that no one can damage the implementation (and for speed).
     *
     */
    public static final Point getClosestPoint(Point from, FigEdge to) {

        // get a new Vector with the points - very memory consuming :-(
        // to improve this, the class FigPoly must provide better accessors
        List points;
        try {
            points = ((FigPoly) to.getFig()).getPointsList();
        } catch (Exception e) { // NullPtr or ClassCast
            points = new ArrayList();
        }

        // workaround when the fig has no points
        if (points.size() < 2) {
            points.add(to.getSourcePortFig().getCenter());
            points.add(to.getDestPortFig().getCenter());
        }

        // the point for the first part
        Point bestPoint = getClosestPointOnEdge(from, (Point) points.get(0),
                (Point) points.get(1));
        // we do not want to do this every time
        int bestDistance = sqr_distance(from, bestPoint);

        // second to last part
        for (int i = 1; i < points.size() - 1; i++) {
            Point one = (Point) points.get(i);
            Point two = (Point) points.get(i + 1);
            Point candidate = getClosestPointOnEdge(from, one, two);
            // better: use the new point and distance
            if (sqr_distance(from, candidate) < bestDistance) {
                bestPoint = candidate;
                bestDistance = sqr_distance(from, bestPoint);
            } // end final better
        } // end for
        return bestPoint;
    } // end getClosestPoint

    /**
     * Standard geometry here.
     *
     * It's final so that no one can damage the implementation (and for speed).
     *
     * @param p The point that is somewhere near the edge.
     * @param begin The begin point of the edge.
     * @param end The end point of the edge.
     */
    public static final Point getClosestPointOnEdge(Point p, Point begin,
            Point end) {
        // direction of the segment
        double directionX = (double) (end.x - begin.x);
        double directionY = (double) (end.y - begin.y);
        // length of the segment
        double length = Math.sqrt(directionX * directionX + directionY
                * directionY);
        // normalize direction
        directionX /= length;
        directionY /= length;

        // begin + diffToStart*direction is the intersection of the
        // perpendicular
        double diffToStart = directionX * (p.x - begin.x) + directionY
                * (p.y - begin.y);

        if (diffToStart < 0.0) // use start or end point if too far out
        {
            return begin;
        } else if (diffToStart > length) {
            return end;
        } else // if on line, use it;
        {
            return new Point(begin.x + (int) (diffToStart * directionX),
                    begin.y + (int) (diffToStart * directionY));
        }

    } // end getCPOE

    /**
     * sqr distance of two points; unmodified method
     */
    public static final int sqr_distance(Point p1, Point p2) {
        return (int) ((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y)
                * (p2.y - p1.y));
    }

    public static Point getNormPointOnEdge(Point r1, Point r0, Point r2) {
        // r1: position of annotation;
        // r0: edge's starting point
        // r2: edge's ending point
        // edge's direction vector
        Point a = new Point(r2.x - r0.x, r2.y - r0.y);
        //
        float t = (float) ((r1.x - r0.x) * a.x + (r1.y - r0.y) * a.y)
                / (a.x * a.x + a.y * a.y);
        // calculate NormPoint
        Point normpoint = new Point();
        normpoint.x = (int) (r1.x + (r0.x - r1.x + t * a.x));
        normpoint.y = (int) (r1.y + (r0.y - r1.y + t * a.y));
        return normpoint;
    }

}// end of class

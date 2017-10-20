package com.example.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Collection;

/**
 * Represent a pseudo-rectangle in latitude-longitude space.
 *
 * This class is limited to pseudo-rectangles that do not cross over
 * (but may include) the "edges" of the full latitude-longitude
 * rectangle, i.e., the two poles and the antimeridian.
 *
 * There are 5 degenerate forms supported by this class: a point (a
 * single latitude-longitude pair), a line (a single longitude with
 * latitude bounds or a single latitude with longitude bounds), a cap
 * (the space between a latitude and one of the poles), a triangle (a
 * cap with longitude boundaries), and a digon (the space between two
 * longitude lines).
 */
public class GeoRectangle
    implements Serializable
{
    private static final long serialVersionUID = 0L;

    public static final double LAT_SOUTH_EDGE = -90.0;
    public static final double LAT_NORTH_EDGE = Math.nextUp(90.0);
    public static final double LON_WEST_EDGE = -180.0;
    public static final double LON_EAST_EDGE = 180.0;

    /**
     * Rectangle representing the full latitude-longitude space.
     */
    public static final GeoRectangle FULL = new GeoRectangle(
        LAT_SOUTH_EDGE,
        LAT_NORTH_EDGE,
        LON_WEST_EDGE,
        LON_EAST_EDGE);

    protected double south;
    protected double north;

    protected double west;
    protected double east;

    public GeoRectangle(
        double south,
        double north,
        double west,
        double east)
    {
        if (false
            || south < LAT_SOUTH_EDGE
            || north > LAT_NORTH_EDGE
            || west < LON_WEST_EDGE
            || east > LON_EAST_EDGE
            || south >= north
            || west >= east
            )
        {
            throw new RuntimeException("invalid rectangle");
        }

        this.south = south;
        this.north = north;
        this.west = west;
        this.east = east;
    }

    /**
     * Get the width (in degrees) of this rectangle.
     *
     * Since rectangles are always divided into four equal quadrants,
     * the width will always be greater than the height.
     */
    public double width()
    {
        return east - west;
    }

    /**
     * Test if p is contained in this rectangle.
     */
    public boolean contains(
        GeoPoint p)
    {
        return true
            && south <= p.latitude
            && north > p.latitude
            && west <= p.longitude
            && east > p.longitude
            ;
    }

    /**
     * Test whether this rectangle is fully degenerate (to a line or
     * to a point, but not to a cap, triangle, or digon).
     *
     * A rectangle is fully degenerate if it's not particularly useful
     * to call {@link #quadrants()}.
     */
    public boolean isFullyDegenerate()
    {
        return false
            || Math.nextUp(south) >= north
            || Math.nextUp(west) >= east
            ;
    }

    /**
     * Return the four quadrants of this rectangle, {north east, north
     * west, south west, south east}, or null if this rectangle is
     * fully degenerate.
     *
     * For any point p, if this.contains(p) and
     * !this.isFullyDegenerate(), then there exists exactly one
     * quadrant q in this.quadrants() such that q.contains(p).
     */
    public GeoRectangle[] quadrants()
    {
        if (isFullyDegenerate())
        {
            return null;
        }

        double midLat = ExtraMath.average(south, north);
        double midLon = ExtraMath.average(west, east);

        return new GeoRectangle[] {
            new GeoRectangle(midLat, north, midLon, east),
            new GeoRectangle(midLat, north, west, midLon),
            new GeoRectangle(south, midLat, west, midLon),
            new GeoRectangle(south, midLat, midLon, east),
            };
    }

    /**
     * Return the four vertices of this rectangle {north east, north
     * west, south west, south east}.
     *
     * Note that some of these vertices might be equal to each other.
     */
    public GeoPoint[] vertices()
    {
        double southmost = south;
        double northmost =
            Math.nextAfter(north, Double.NEGATIVE_INFINITY);
        double westmost = west;
        double eastmost =
            Math.nextAfter(east, Double.NEGATIVE_INFINITY);

        return new GeoPoint[] {
            new GeoPoint(northmost, eastmost),
            new GeoPoint(northmost, westmost),
            new GeoPoint(southmost, westmost),
            new GeoPoint(southmost, eastmost),
            };
    }

    /**
     * Compute the extremum (minimum or maximum) initial bearing from
     * {@code center} such that this rectangle contains the point
     * {@code distance} away.
     *
     * @param center
     *     Center point of the arc contained in this rectangle.
     * @param distance
     *     Radius (angular distance) of the arc.
     * @param cw
     *     If this is true, find the clockwise-most extremum.
     *     Otherwise, find the counter-clockwise-most extremum.
     * @param guess
     *     A guess at the resulting bearing. The point at this guessed
     *     bearing MUST be contained by this rectangle. Additionally,
     *     if there are multiple arcs contained in this rectangle,
     *     this parameter limits the resulting bearing to the
     *     appropriate extremum bearing on the arc that includes
     *     {@code guess}.
     * @return
     *     A bearing from {@code center} to the appropriate extremum.
     *     If there is no extremum in the appropriate direction (i.e.,
     *     if the arc is a point or a circle), return {@code cw ?
     *     Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY}.
     */
    public double getExtremumBearing(
        GeoPoint center,
        double distance,
        boolean cw,
        double guess)
    {
        assert contains(center.greatArcEndpoint(distance, guess));

        // Test the cardinal directions. This is useful because of the
        // following properties:
        //
        // 1. If and only if two adjacent cardinal directions are
        // contained, then the entire quarter arc between them is
        // contained.
        //
        // 2. If exactly one of two adjacent cardinal directions is
        // contained, then there is exactly one intersection point
        // between the boundary of the rectangle and the quarter arc.
        //
        // 3. If neither of two adjacent cardinal directions are
        // contained, then either none of the quarter arc intersects
        // the rectangle, or exactly one sub-arc of the quarter arc
        // intersects the rectangle.
        boolean allCardinalsContained = true;
        boolean[] containedCardinals = new boolean[4];
        for (int i = 0; i < containedCardinals.length; ++i)
        {
            containedCardinals[i] = contains(center.greatArcEndpoint(
                distance,
                90.0 * i));

            if (!containedCardinals[i])
            {
                allCardinalsContained = false;
            }
        }

        if (allCardinalsContained)
        {
            // If and only iff all of the cardinal directions are
            // contained, then the entire circle (or point) is
            // contained.
            return
                cw
                ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY
                ;
        }

        // Find rough bounds on the result bearing using the cardinal
        // directions.
        double minBearing = guess;
        double maxBearing;
        int cardinal;
        int cardinalProgression;
        if (cw)
        {
            cardinal = (int)Math.ceil(guess/90.0) % 4;
            cardinalProgression = 1;
        }
        else
        {
            cardinal = (int)Math.floor(guess/90.0);
            cardinalProgression = -1;
        }
        for (
            ;
            containedCardinals[cardinal];
            cardinal =
                ExtraMath.modulus(4, cardinal + cardinalProgression))
        {
            minBearing = cardinal * 90.0;
        }
        maxBearing = cardinal * 90.0;

        // Perform a binary search of the bearing space within the
        // bounds from above.
        double midBearing;
        while (minBearing != maxBearing)
        {
            midBearing = ExtraMath.midpoint(
                360.0,
                cw ? minBearing : maxBearing,
                cw ? maxBearing : minBearing);
            if (midBearing == minBearing)
            {
                midBearing =
                    cw
                    ? ExtraMath.nextUp(360.0, midBearing)
                    : ExtraMath.nextDown(360.0, midBearing)
                    ;
            }

            if (
                contains(
                    center.greatArcEndpoint(distance, midBearing)))
            {
                minBearing = midBearing;
            }
            else
            {
                maxBearing =
                    cw
                    ? ExtraMath.nextDown(360.0, midBearing)
                    : ExtraMath.nextUp(360.0, midBearing)
                    ;
            }
        }

        return minBearing;
    }

    /**
     * Find the largest distance (up to and including 180.0) such that
     * the point at that distance away from {@code from} in direction
     * {@code bearing} is contained in this rectangle.
     *
     * @param from
     *     Start point of the great arc.
     * @param bearing
     *     Initial bearing of the great arc.
     * @param guess
     *     A guess at the resulting distance. The point at this
     *     guessed distance MUST be contained by this rectangle.
     * @return
     *     Maximum distance, as described above.
     */
    public double maxContainedDistance(
        GeoPoint from,
        double bearing,
        double guess)
    {
        assert contains(from.greatArcEndpoint(guess, bearing));

        // Perform a binary search of the distance-space.
        double minDistance = guess;
        double maxDistance = 180.0;
        double midDistance;
        while (minDistance < maxDistance)
        {
            midDistance = ExtraMath.average(minDistance, maxDistance);
            if (midDistance == minDistance)
            {
                midDistance = Math.nextAfter(
                    midDistance,
                    Double.POSITIVE_INFINITY);
            }

            if (contains(from.greatArcEndpoint(midDistance, bearing)))
            {
                minDistance = midDistance;
            }
            else
            {
                maxDistance = Math.nextAfter(
                    midDistance,
                    Double.NEGATIVE_INFINITY);
            }
        }

        return minDistance;
    }

    /**
     * Find all vertex points in {@code r1} that are adjacent to
     * {@code r2} and vice versa.
     *
     * This code assumes that r1 and r2 do not intersect. Results may
     * if unexpected if this is not true.
     */
    public static Collection<GeoPoint> adjacentVertices(
        GeoRectangle r1,
        GeoRectangle r2)
    {
        GeoPoint[] r1Vertices = r1.vertices();
        GeoPoint[] r2Vertices = r2.vertices();

        Collection<GeoPoint> result = new HashSet<GeoPoint>();

        // If there's no overlap or adjacency in latitude:
        if (r1.south > r2.north || r1.north < r2.south)
        {
            // Return no adjacency.
            return result;
        }

        // If they are adjacent with r1 west of r2:
        if (false
            || r1.east == r2.west
            || (r1.east == LON_EAST_EDGE && r2.west == LON_WEST_EDGE)
            )
        {
            // If r1's northmost and r2-most vertex is adjacent:
            if (r1.north <= r2.north)
            {
                result.add(r1Vertices[0]);
            }

            // If r2's northmost and r1-most vertex is adjacent:
            if (r2.north <= r1.north)
            {
                result.add(r2Vertices[1]);
            }

            // If r1's southmost and r2-most vertex is adjacent:
            if (r1.south >= r2.south)
            {
                result.add(r1Vertices[3]);
            }

            // If r2's southmost and r1-most vertex is adjacent:
            if (r2.south >= r1.south)
            {
                result.add(r2Vertices[2]);
            }
        }

        // If they are adjacent with r1 east of r2:
        if (false
            || r2.east == r1.west
            || (r2.east == LON_EAST_EDGE && r1.west == LON_WEST_EDGE)
            )
        {
            // If r1's northmost and r2-most vertex is adjacent:
            if (r1.north <= r2.north)
            {
                result.add(r1Vertices[1]);
            }

            // If r2's northmost and r1-most vertex is adjacent:
            if (r2.north <= r1.north)
            {
                result.add(r2Vertices[0]);
            }

            // If r1's southmost and r2-most vertex is adjacent:
            if (r1.south >= r2.south)
            {
                result.add(r1Vertices[2]);
            }

            // If r2's southmost and r1-most vertex is adjacent:
            if (r2.south >= r1.south)
            {
                result.add(r2Vertices[3]);
            }
        }

        return result;
    }

    @Override
    public String toString()
    {
        return String.format(
            "%s[%s, %s, %s, %s]",
            getClass().getName(),
            south,
            north,
            west,
            east);
    }
}

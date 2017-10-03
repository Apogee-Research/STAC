package com.example.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class GeoRectangleTest
{
    @Test
    public void testFullRectangle()
    {
        String message;

        Assert.assertTrue(
            !GeoRectangle.FULL.isFullyDegenerate());

        GeoPoint[] points = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(-90.0, 0.0),
            new GeoPoint(90.0, 0.0),
            new GeoPoint(0.0, -180.0),
            new GeoPoint(
                0.0,
                Math.nextAfter(180.0, Double.NEGATIVE_INFINITY)),
            };

        double[] distances = new double[] {
            0.0,
            1.0,
            90.0,
            123.4,
            180.0,
            };

        double[] bearings = new double[] {
            0.0,
            12.3,
            90.0,
            123.4,
            180.0,
            234.5,
            270.0,
            345.6,
            };

        for (GeoPoint point : points)
        {
            message = "point = " + point;

            Assert.assertTrue(
                message,
                GeoRectangle.FULL.contains(point));

            for (double distance : distances)
            {
                for (double bearing : bearings)
                {
                    message =
                        "point = " + point +
                        ", distance = " + distance +
                        ", bearing = " + bearing;

                    Assert.assertEquals(
                        message,
                        Double.POSITIVE_INFINITY,
                        GeoRectangle.FULL.getExtremumBearing(
                            point,
                            distance,
                            true,
                            bearing),
                        0.0);

                    Assert.assertEquals(
                        message,
                        Double.NEGATIVE_INFINITY,
                        GeoRectangle.FULL.getExtremumBearing(
                            point,
                            distance,
                            false,
                            bearing),
                        0.0);

                    Assert.assertEquals(
                        message,
                        180.0,
                        GeoRectangle.FULL.maxContainedDistance(
                            point,
                            bearing,
                            distance),
                        0.0);
                }
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class GeoRectangleExtremumBearingTest
    {
        @Parameterized.Parameters(
            name =
                "{index}: " +
                "{0}.getExtremumBearing({1}, {2}, true/false, {3})" +
                " =  {4}/{5}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] {
                // Great circles near the equator that are entirely
                // within a rectangle around the equator.
                {
                    new GeoRectangle(
                        -1.0,
                        1.0,
                        GeoRectangle.LON_WEST_EDGE,
                        GeoRectangle.LON_EAST_EDGE),
                    new GeoPoint(90.0, 0.0),
                    90.0,
                    0.0,
                    Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY,
                    0.0,
                    },
                {
                    new GeoRectangle(
                        -20.0,
                        20.0,
                        GeoRectangle.LON_WEST_EDGE,
                        GeoRectangle.LON_EAST_EDGE),
                    new GeoPoint(-71.0, 0.0),
                    90.0,
                    0.0,
                    Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY,
                    0.0,
                    },

                // Polar great circles that are half within a
                // rectangle around a meridian.
                {
                    new GeoRectangle(
                        GeoRectangle.LAT_SOUTH_EDGE,
                        GeoRectangle.LAT_NORTH_EDGE,
                        -1.0,
                        1.0),
                    new GeoPoint(0.0, -90.0),
                    90.0,
                    90.0,
                    180.0,
                    0.0,
                    1e-6,
                    },
                {
                    new GeoRectangle(
                        GeoRectangle.LAT_SOUTH_EDGE,
                        GeoRectangle.LAT_NORTH_EDGE,
                        90.3,
                        90.4),
                    new GeoPoint(0.0, -179.65),
                    90.0,
                    270.0,
                    ExtraMath.nextDown(360.0, 0.0),
                    ExtraMath.nextUp(360.0, 180.0),
                    1e-6,
                    },

                // Circles that intersect the rectangle in multiple
                // places.
                {
                    new GeoRectangle(
                        -1e-3,
                        1e-3,
                        -1e-3,
                        1e-3),
                    new GeoPoint(0.0, 0.0),
                    1e-3 / Math.cos(Math.PI/8.0),
                    45.0,
                    45.0 + 22.5,
                    45.0 - 22.5,
                    1e-6,
                    },
                {
                    new GeoRectangle(
                        -1e-3,
                        1e-3,
                        -1e-3,
                        1e-3),
                    new GeoPoint(0.0, 0.0),
                    1e-3 / Math.cos(Math.PI/8.0),
                    135.0,
                    135.0 + 22.5,
                    135.0 - 22.5,
                    1e-6,
                    },
                {
                    new GeoRectangle(
                        -1e-3,
                        1e-3,
                        -1e-3,
                        1e-3),
                    new GeoPoint(0.0, 0.0),
                    1e-3 / Math.cos(Math.PI/8.0),
                    225.0,
                    225.0 + 22.5,
                    225.0 - 22.5,
                    1e-6,
                    },
                {
                    new GeoRectangle(
                        -1e-3,
                        1e-3,
                        -1e-3,
                        1e-3),
                    new GeoPoint(0.0, 0.0),
                    1e-3 / Math.cos(Math.PI/8.0),
                    315.0,
                    315.0 + 22.5,
                    315.0 - 22.5,
                    1e-6,
                    },
                {
                    new GeoRectangle(
                        -2e-3,
                        2e-3,
                        -1e-3,
                        1e-3),
                    new GeoPoint(0.0, 0.0),
                    1e-3 / Math.cos(Math.PI/8.0),
                    0.0,
                    45.0 + 22.5,
                    315.0 - 22.5,
                    1e-6,
                    },
                {
                    new GeoRectangle(
                        -2e-3,
                        2e-3,
                        -1e-3,
                        1e-3),
                    new GeoPoint(0.0, 0.0),
                    1e-3 / Math.cos(Math.PI/8.0),
                    180.0,
                    225.0 + 22.5,
                    135.0 - 22.5,
                    1e-6,
                    },

                // Other.
                {
                    new GeoRectangle(
                        -45.123456,
                        89.123456,
                        -1.0,
                        1.0),
                    new GeoPoint(0.0, 90.0),
                    90.0,
                    270.0,
                    270.0 + 89.123456,
                    270.0 - 45.123456,
                    1e-6,
                    },
                });

        }

        @Parameterized.Parameter(value = 0)
        public GeoRectangle rectangle;

        @Parameterized.Parameter(value = 1)
        public GeoPoint center;

        @Parameterized.Parameter(value = 2)
        public double distance;

        @Parameterized.Parameter(value = 3)
        public double guess;

        @Parameterized.Parameter(value = 4)
        public double expectedCw;

        @Parameterized.Parameter(value = 5)
        public double expectedCcw;

        @Parameterized.Parameter(value = 6)
        public double delta;

        @Test
        public void test()
        {
            ExtraMathTest.assertEquals(
                360.0,
                expectedCw,
                rectangle.getExtremumBearing(
                    center,
                    distance,
                    true,
                    guess),
                delta);

            ExtraMathTest.assertEquals(
                360.0,
                expectedCcw,
                rectangle.getExtremumBearing(
                    center,
                    distance,
                    false,
                    guess),
                delta);
        }
    }

    @RunWith(Parameterized.class)
    public static class GeoRectangleContainedDistanceTest
    {
        @Parameterized.Parameters(
            name =
                "{index}: " +
                "{0}.maxContainedDistance({1}, {2}, {3}) = {4}")
        public static Collection<Object[]> data()
        {
            Collection<Object[]> data = new ArrayList<Object[]>();

            // Great circles near the equator that are entirely
            // within a rectangle around the equator.
            data.add(new Object[] {
                new GeoRectangle(
                    -1.0,
                    1.0,
                    GeoRectangle.LON_WEST_EDGE,
                    GeoRectangle.LON_EAST_EDGE),
                new GeoPoint(0.0, -180.0),
                270.0,
                0.0,
                180.0,
                0.0,
                });
            data.add(new Object[] {
                new GeoRectangle(
                    -20.0,
                    20.0,
                    GeoRectangle.LON_WEST_EDGE,
                    GeoRectangle.LON_EAST_EDGE),
                new GeoPoint(-19.0, 0.0),
                90.0,
                1.234567,
                180.0,
                0.0,
                });

            // Point centered in a square.
            for (int i = 0; i < 4; ++i)
            {
                final GeoRectangle rectangle =
                    new GeoRectangle(-1.0, 1.0, -1.0, 1.0);
                final GeoPoint center = new GeoPoint(0.0, 0.0);

                // Cardinal directions.
                data.add(new Object[] {
                    rectangle,
                    center,
                    i * 90.0,
                    0.0,
                    1.0,
                    1e-9,
                    });

                // Bearings towards the corner points.
                GeoPoint corner = new GeoPoint(
                    i % 2 == 0 ? 1.0 : -1.0,
                    i / 2 == 0 ? 1.0 : -1.0);
                data.add(new Object[] {
                    rectangle,
                    center,
                    center.bearing(corner),
                    0.0,
                    center.distance(corner),
                    1e-9,
                    });
            }

            // Points outside of caps.
            data.add(new Object[] {
                new GeoRectangle(
                    89.0,
                    GeoRectangle.LAT_NORTH_EDGE,
                    GeoRectangle.LON_WEST_EDGE,
                    GeoRectangle.LON_EAST_EDGE),
                new GeoPoint(0.0, 123.4),
                0.0,
                89.1,
                91.0,
                1e-6,
                });
            data.add(new Object[] {
                new GeoRectangle(
                    GeoRectangle.LAT_SOUTH_EDGE,
                    -89.0,
                    GeoRectangle.LON_WEST_EDGE,
                    GeoRectangle.LON_EAST_EDGE),
                new GeoPoint(-1.0, -123.4),
                180.0,
                88.1,
                90.0,
                1e-6,
                });

            // Points outside of triangles.
            data.add(new Object[] {
                new GeoRectangle(
                    60.0,
                    GeoRectangle.LAT_NORTH_EDGE,
                    -1.0,
                    1.0),
                new GeoPoint(-60.0, 0.0),
                0.0,
                123.4,
                150.0,
                1e-6,
                });
            data.add(new Object[] {
                new GeoRectangle(
                    GeoRectangle.LAT_SOUTH_EDGE,
                    -30.0,
                    123.0,
                    124.0),
                new GeoPoint(-20.0, 123.1),
                180.0,
                10.1,
                70.0,
                1e-6,
                });

            return data;
        }

        @Parameterized.Parameter(value = 0)
        public GeoRectangle rectangle;

        @Parameterized.Parameter(value = 1)
        public GeoPoint from;

        @Parameterized.Parameter(value = 2)
        public double bearing;

        @Parameterized.Parameter(value = 3)
        public double guess;

        @Parameterized.Parameter(value = 4)
        public double expected;

        @Parameterized.Parameter(value = 5)
        public double delta;

        @Test
        public void test()
        {
            double result = rectangle.maxContainedDistance(
                from,
                bearing,
                guess);

            Assert.assertTrue(result >= 0.0);
            Assert.assertTrue(result <= 180.0);

            Assert.assertEquals(expected, result, delta);
        }
    }
}

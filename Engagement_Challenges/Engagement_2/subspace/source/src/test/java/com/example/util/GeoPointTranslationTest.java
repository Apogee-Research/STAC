package com.example.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeoPointTranslationTest
{
    private static final double DELTA = 1e-7;

    @Parameterized.Parameters(
        name =
            "{index}: " +
            "@{0},{1} <-> @{2},{3}, " +
            "distance {4}, " +
            "forward bearing {5}, " +
            "reverse bearing {6}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] {
            // antipodal points
            {-90.0, 0.0, 90.0, 0.0, 180.0, 0.0, 1.0},
            {0.0, 0.0, 0.0, -180.0, 180.0, 2.0, 3.0},
            {0.0, -90.0, 0.0, 90.0, 180.0, 4.0, 5.0},
            {5.0, 10.0, -5.0, -170.0, 180.0, 6.0, 7.0},

            // right angles
            {0.0, -180.0, 90.0, -180.0, 90.0, 0.0, 180.0},
            {-90.0, 0.0, 0.0, 77.7, 90.0, 77.7, 180.0},
            {-45.0, -12.3, 45.0, -12.3, 90.0, 0.0, 180.0},
            {0.0, -135.0, 0.0, 135.0, 90.0, 270.0, 90.0},

            // degenerate arcs
            {1.0, 2.0, 1.0, 2.0, 0.0, 3.0, 4.0},
            {-90.0, 1.0, -90.0, 2.0, 0.0, -12.3, 45.6},
            {90.0, -180.0, 90.0, 42.0, 0.0, 78.9, 90.1},
            {-88.8, -180.0, -88.8, -180.0, 0.0, 333.3, 222.2},

            // other
            {-12.0, 34.0, 56.0, 34.0, 12.0+56.0, 0.0, 180.0},
        });
    }

    @Parameterized.Parameter(value = 0)
    public double lat1;

    @Parameterized.Parameter(value = 1)
    public double lon1;

    @Parameterized.Parameter(value = 2)
    public double lat2;

    @Parameterized.Parameter(value = 3)
    public double lon2;

    @Parameterized.Parameter(value = 4)
    public double distance;

    @Parameterized.Parameter(value = 5)
    public double forwardBearing;

    @Parameterized.Parameter(value = 6)
    public double reverseBearing;

    @Test
    public void test()
    {
        GeoPoint p1 = new GeoPoint(lat1, lon1);
        GeoPoint p2 = new GeoPoint(lat2, lon2);

        // Test distance() accuracy.
        Assert.assertEquals(distance, p1.distance(p2), DELTA);

        // Test distance() symmetry.
        Assert.assertEquals(p1.distance(p2), p2.distance(p1), 0.0);

        // If the bearing value matters:
        if (distance != 0.0 && distance != 180.0)
        {
            // Test bearing() accuracy.
            Assert.assertEquals(
                forwardBearing,
                p1.bearing(p2),
                DELTA);
            Assert.assertEquals(
                reverseBearing,
                p2.bearing(p1),
                DELTA);
        }

        // Attempt to calculate other-point from (this-point,
        // distance, bearing).
        GeoPoint p2From1 =
            p1.greatArcEndpoint(distance, forwardBearing);
        GeoPoint p1From2 =
            p2.greatArcEndpoint(distance, reverseBearing);

        if (distance > 0.0)
        {
            // Test greatArcEndpoint() accuracy.
            Assert.assertEquals(
                p1 + " = " + p1From2,
                0.0,
                p1.distance(p1From2),
                DELTA);
            Assert.assertEquals(
                p2 + " = " + p2From1,
                0.0,
                p2.distance(p2From1),
                DELTA);
        }
        else
        {
            // Test greatArcEndpoint() accuracy, and ensure that
            // moving a distance of 0.0 has no effect.
            Assert.assertEquals(p1, p1From2);
            Assert.assertEquals(p2, p2From1);
        }
    }
}

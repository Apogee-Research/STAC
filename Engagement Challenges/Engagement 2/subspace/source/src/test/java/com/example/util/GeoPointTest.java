package com.example.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeoPointTest
{
    private static final double DELTA = Math.scalb(1.0, -23);

    private static final double LAT_MIN = -90.0;
    private static final double LAT_MID = 0.0;
    private static final double LAT_MAX = 90.0;

    private static final double LON_MIN_MINUS = -180.0 - DELTA/2;
    private static final double LON_MIN_PLUS = -180.0 + DELTA/2;
    private static final double LON_MID = 0.0;
    private static final double LON_MAX_MINUS = 180.0 - DELTA/2;
    private static final double LON_MAX_PLUS = 180.0 + DELTA/2;

    private static final double LAT_OTHER = 27.637713183962234;
    private static final double LON_OTHER = -49.92158273352149;

    @Parameterized.Parameters(name = "{index}: @{0},{1} = @{2},{3}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] {
            // equator points, no wrap around
            {LAT_MID, LON_MID, LAT_MID, LON_MID},
            {LAT_MID, LON_MIN_PLUS, LAT_MID, LON_MIN_PLUS},
            {LAT_MID, LON_MAX_MINUS, LAT_MID, LON_MAX_MINUS},

            // polar points, no wrap around
            {LAT_MIN, 12.3, LAT_MIN, 0.0},
            {LAT_MIN, LON_MIN_PLUS, LAT_MIN, 0.0},
            {LAT_MIN, LON_MAX_MINUS, LAT_MIN, 0.0},
            {LAT_MAX, -45.6, LAT_MAX, 0.0},
            {LAT_MAX, LON_MIN_PLUS, LAT_MAX, 0.0},
            {LAT_MAX, LON_MAX_MINUS, LAT_MAX, 0.0},

            // longitude wrap-around borders
            {-12.3, LON_MAX_PLUS, -12.3, LON_MIN_PLUS},
            {LAT_MID, LON_MAX_PLUS, LAT_MID, LON_MIN_PLUS},
            {45.6, LON_MAX_PLUS, 45.6, LON_MIN_PLUS},
            {-78.0, LON_MIN_MINUS, -78.0, LON_MAX_MINUS},
            {LAT_MID, LON_MIN_MINUS, LAT_MID, LON_MAX_MINUS},
            {12.3, LON_MIN_MINUS, 12.3, LON_MAX_MINUS},

            // longitude wrap-around to 0
            {0.0, 360.0, 0.0, 0.0},
            {0.0, 2*360.0, 0.0, 0.0},
            {0.0, 3*360.0, 0.0, 0.0},
            {0.0, -360.0, 0.0, 0.0},
            {0.0, 2*-360.0, 0.0, 0.0},
            {0.0, 3*-360.0, 0.0, 0.0},
        });
    }

    @Parameterized.Parameter(value = 0)
    public double latitudeRaw;

    @Parameterized.Parameter(value = 1)
    public double longitudeRaw;

    @Parameterized.Parameter(value = 2)
    public double latitude;

    @Parameterized.Parameter(value = 3)
    public double longitude;

    @Test
    public void test()
    {
        GeoPoint pointRaw = new GeoPoint(latitudeRaw, longitudeRaw);
        Assert.assertEquals(latitude, pointRaw.latitude, DELTA);
        Assert.assertEquals(longitude, pointRaw.longitude, DELTA);

        GeoPoint point = new GeoPoint(latitude, longitude);
        Assert.assertEquals(latitude, point.latitude, DELTA);
        Assert.assertEquals(longitude, point.longitude, DELTA);

        GeoPoint pointOther = new GeoPoint(LAT_OTHER, LON_OTHER);
        Assert.assertEquals(LAT_OTHER, pointOther.latitude, DELTA);
        Assert.assertEquals(LON_OTHER, pointOther.longitude, DELTA);

        Assert.assertEquals(pointRaw, point);
        Assert.assertEquals(point, pointRaw);
        Assert.assertNotEquals(pointRaw, pointOther);
        Assert.assertNotEquals(pointOther, pointRaw);
        Assert.assertNotEquals(point, pointOther);
        Assert.assertNotEquals(pointOther, point);

        Assert.assertEquals(pointRaw.hashCode(), point.hashCode());
        Assert.assertNotEquals(
           pointRaw.hashCode(),
           pointOther.hashCode());
    }
}

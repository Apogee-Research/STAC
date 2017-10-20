package com.example.util;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class GeoTrieTest
{
    @Test
    public void testPutRemoveGet()
    {
        GeoTrie<String> trie = new GeoTrie<String>();

        GeoPoint p0 = new GeoPoint(0.0, 0.0);
        GeoPoint p1 =
            new GeoPoint(86.34244489086524, 9.670719260653641);
        GeoPoint p2 =
            new GeoPoint(11.564369791869865, 68.98285944362758);
        GeoPoint p3 =
            new GeoPoint(-30.595434610947308, -153.0269958931968);
        GeoPoint p4 =
            new GeoPoint(-85.88858814485856, -93.02336876002194);

        String s0 = "a";
        String s1 = "b";
        String s2 = "c";
        String s3 = "d";

        Assert.assertEquals(null, trie.get(p0));
        Assert.assertEquals(null, trie.get(p1));
        Assert.assertEquals(null, trie.get(p2));
        Assert.assertEquals(null, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(null, trie.put(p0, s0));
        Assert.assertEquals(s0, trie.get(p0));
        Assert.assertEquals(null, trie.get(p1));
        Assert.assertEquals(null, trie.get(p2));
        Assert.assertEquals(null, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(null, trie.put(p1, s1));
        Assert.assertEquals(s0, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(null, trie.get(p2));
        Assert.assertEquals(null, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(null, trie.put(p2, s2));
        Assert.assertEquals(s0, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(null, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(null, trie.put(p3, s3));
        Assert.assertEquals(s0, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(s3, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(s0, trie.put(p0, s1));
        Assert.assertEquals(s1, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(s3, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(s1, trie.remove(p0));
        Assert.assertEquals(null, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(s3, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(null, trie.remove(p0));
        Assert.assertEquals(null, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(s3, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(null, trie.put(p0, s2));
        Assert.assertEquals(s2, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(s3, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(s3, trie.put(p3, s0));
        Assert.assertEquals(s2, trie.get(p0));
        Assert.assertEquals(s1, trie.get(p1));
        Assert.assertEquals(s2, trie.get(p2));
        Assert.assertEquals(s0, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));

        Assert.assertEquals(s2, trie.remove(p0));
        Assert.assertEquals(s1, trie.remove(p1));
        Assert.assertEquals(s2, trie.remove(p2));
        Assert.assertEquals(s0, trie.remove(p3));
        Assert.assertEquals(null, trie.remove(p4));

        Assert.assertEquals(null, trie.remove(p0));
        Assert.assertEquals(null, trie.remove(p1));
        Assert.assertEquals(null, trie.remove(p2));
        Assert.assertEquals(null, trie.remove(p3));
        Assert.assertEquals(null, trie.remove(p4));

        Assert.assertEquals(null, trie.get(p0));
        Assert.assertEquals(null, trie.get(p1));
        Assert.assertEquals(null, trie.get(p2));
        Assert.assertEquals(null, trie.get(p3));
        Assert.assertEquals(null, trie.get(p4));
    }

    @Test
    public void testHeight()
    {
        GeoTrie<String> trie = new GeoTrie<String>();

        GeoPoint p0 = new GeoPoint(0.0, 0.0);
        GeoPoint p1 =
            new GeoPoint(86.34244489086524, 9.670719260653641);
        GeoPoint p2 =
            new GeoPoint(11.564369791869865, 68.98285944362758);
        GeoPoint p3 =
            new GeoPoint(-30.595434610947308, -153.0269958931968);
        GeoPoint p4 =
            new GeoPoint(-85.88858814485856, -93.02336876002194);

        String s0 = "a";
        String s1 = "b";
        String s2 = "c";
        String s3 = "d";

        Assert.assertEquals(0, trie.height());;

        trie.put(p0, s0);
        int h0 = trie.height();
        Assert.assertNotEquals(0, h0);

        trie.put(p1, s1);
        int h1 = trie.height();

        trie.put(p2, s2);
        int h2 = trie.height();

        trie.put(p3, s3);

        trie.remove(p3);
        Assert.assertEquals(h2, trie.height());

        trie.remove(p2);
        Assert.assertEquals(h1, trie.height());

        trie.remove(p1);
        Assert.assertEquals(h0, trie.height());

        trie.remove(p0);
        Assert.assertEquals(0, trie.height());
    }

    @Test
    public void testBigTrie()
    {
        final int TRIE_SIZE = 10*1000;

        Random random = new Random(1234);

        GeoPoint[] points = new GeoPoint[TRIE_SIZE];
        double[] values = new double[TRIE_SIZE];
        for (int i = 0; i < TRIE_SIZE; ++i)
        {
            points[i] = new GeoPoint(
                random.nextDouble() * 180.0 - 90.0,
                random.nextDouble() * 360.0 - 180.0);

            values[i] = random.nextDouble();
        }

        GeoTrie<Double> trie = new GeoTrie<Double>();

        for (int i = 0; i < TRIE_SIZE; ++i)
        {
            Assert.assertEquals(null, trie.put(points[i], values[i]));
        }

        for (int i = 0; i < TRIE_SIZE; ++i)
        {
            Double value = trie.get(points[i]);
            Assert.assertNotEquals(null, value);
            Assert.assertEquals(values[i], value.doubleValue(), 0.0);
        }

        for (int i = 0; i < TRIE_SIZE; ++i)
        {
            Double value = trie.remove(points[i]);
            Assert.assertNotEquals(null, value);
            Assert.assertEquals(values[i], value.doubleValue(), 0.0);
        }

        for (int i = 0; i < TRIE_SIZE; ++i)
        {
            Double value = trie.get(points[i]);
            Assert.assertEquals(null, value);
        }
    }

//    @Test
//    public void testSearchOnOctantBoundaryGrid()
//    {
//        final double DELTA = 1e-8;
//
//        // Create a trie that maps all points @x,y to @x,y for
//        // integral x,y such that @x,y lies along the equator, or one
//        // of the following meridians: -180, -90, 0, 90.
//        GeoTrie<GeoPoint> trie = new GeoTrie<GeoPoint>();
//        for (int latitude = -90; latitude <= 90; ++latitude)
//        {
//            GeoPoint p;
//
//            if (latitude == -90 || latitude == 90)
//            {
//                p = new GeoPoint((double)latitude, 0.0);
//                Assert.assertEquals(null, trie.put(p, p));
//                continue;
//            }
//
//            for (
//                int longitude = -180;
//                longitude < 180;
//                longitude += (latitude == 0 ? 1 : 90))
//            {
//                p = new GeoPoint((double)latitude, (double)longitude);
//                Assert.assertEquals(null, trie.put(p, p));
//            }
//        }
//
//        // Search all the caps from each intersection point.
//        GeoPoint[] intersectionPoints = new GeoPoint[] {
//            new GeoPoint(-90.0, 0.0),
//            new GeoPoint(0.0, -180.0),
//            new GeoPoint(0.0, -90.0),
//            new GeoPoint(0.0, 0.0),
//            new GeoPoint(0.0, 90.0),
//            new GeoPoint(90.0, 0.0),
//            };
//        for (GeoPoint intersection : intersectionPoints)
//        {
//            final double searchDistance = 10.5;
//
//            int pointsFound = 0;
//            double prevDistance = 0.0;
//            for (
//                GeoSearchResult<GeoPoint> result :
//                GeoSearchResult.upTo(
//                    trie.search(intersection),
//                    searchDistance,
//                    -1))
//            {
//                String message = String.format(
//                    "from %s found #%d %s",
//                    intersection,
//                    pointsFound,
//                    result.point);
//
//                Assert.assertTrue(
//                    message,
//                    result.distance >= prevDistance);
//                Assert.assertTrue(
//                    message,
//                    result.distance <= searchDistance);
//
//                double expectedDistance = (pointsFound + 3) / 4;
//
//                Assert.assertEquals(
//                    message,
//                    intersection.distance(result.point),
//                    result.distance,
//                    0.0);
//                Assert.assertEquals(
//                    message,
//                    expectedDistance,
//                    result.distance,
//                    DELTA);
//                Assert.assertEquals(
//                    message,
//                    result.point,
//                    result.value);
//
//                ++pointsFound;
//                prevDistance = result.distance;
//            }
//        }
//    }
}

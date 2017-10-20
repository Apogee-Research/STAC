package com.example.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class GeoMultiTrieTest
{
    @Test
    public void testAddGetRemove()
    {
        GeoMultiTrie<String> trie = new GeoMultiTrie<String>();

        GeoPoint point = new GeoPoint(0.0, 0.0);

        String[] strings = new String[] {"a", "b", "c", "d"};

        Assert.assertNull(trie.get(point, false));

        for (String s : strings)
        {
            Assert.assertTrue(s, trie.add(point, s));
        }

        for (String s : strings)
        {
            Assert.assertTrue(s, trie.get(point, false).contains(s));
        }

        for (String s : strings)
        {
            Assert.assertTrue(s, trie.remove(point, s));
        }

        Assert.assertNull(trie.get(point, false));

        trie.get(point, true).addAll(Arrays.asList(strings));

        for (String s : strings)
        {
            Assert.assertTrue(s, trie.get(point, false).contains(s));
        }

        trie.get(point, false).clear();

        for (String s : strings)
        {
            Assert.assertFalse(s, trie.remove(point, s));
        }
    }

    @Test
    public void testSearch()
    {
        GeoMultiTrie<String> trie = new GeoMultiTrie<String>();

        GeoPoint[] points = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(1.0, 0.0),
            new GeoPoint(2.0, 0.0),
            };

        String[] strings = new String[] {"a", "b", "c"};

        for (GeoPoint p : points)
        {
            for (String s : strings)
            {
                Assert.assertTrue(s + " " + p, trie.add(p, s));
            }
        }

        Iterator<GeoSearchResult<String>> it = trie.search(points[0]);

        for (int distance = 0; distance < points.length; ++distance)
        {
            Set<String> stringsSeen = new HashSet<String>();

            for (int string = 0; string < strings.length; ++string)
            {
                GeoSearchResult<String> result = it.next();

                Assert.assertEquals(
                    (double)distance,
                    result.distance,
                    1e-8);

                Assert.assertEquals(
                    points[distance],
                    result.point);

                stringsSeen.add(result.value);
            }

            Assert.assertEquals(
                "distance = " + distance,
                strings.length,
                stringsSeen.size());
        }

        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testSerialize()
        throws ClassNotFoundException, IOException
    {
        GeoPoint p = new GeoPoint(0.0, 0.0);
        String s = "a";

        GeoMultiTrie<String> trie = new GeoMultiTrie<String>();
        trie.add(p, s);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trie);
        oos.close();

        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        GeoMultiTrie<String> trieCopy =
            (GeoMultiTrie<String>)ois.readObject();
        ois.close();

        Assert.assertEquals(
            s,
            trieCopy.get(p, false).iterator().next());
    }
}

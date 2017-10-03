package com.example.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class to store a single result from an geographical search centered
 * around a point.
 */
public class GeoSearchResult<Value>
    implements Comparable<GeoSearchResult<Value>>
{
    /**
     * Angular distance (in degrees) from the search point to
     * {@link #point point}.
     */
    public double distance;

    /**
     * Point of the search result.
     */
    public GeoPoint point;

    /**
     * Value of the search result.
     */
    public Value value;

    @Override
    public int compareTo(
        GeoSearchResult<Value> other)
    {
        return Double.compare(distance, other.distance);
    }

    @Override
    public String toString()
    {
        return String.format(
            "%s[distance = %s, point = %s, value = %s]",
            getClass().getName(),
            distance,
            point,
            value);
    }

    /**
     * Given an iterator that returns search results in ascending
     * order of distance, return a list of the remaining results up to
     * a point.
     *
     * @param iterator
     *     An iterator that return search results in ascending order
     *     of distance.
     * @param maxDistance
     *     Maximum angular distance (in degrees) to search. A distance
     *     of {@code Double.POSITIVE_INFINITY} removes the distance
     *     limit.
     * @param maxResults
     *     Maximum number of results to return. A value of -1
     *     indicates no limit.
     * @return
     *     A list of results, in ascending order of angular distance.
     */
    public static <Value> List<GeoSearchResult<Value>> upTo(
        Iterator<GeoSearchResult<Value>> iterator,
        double maxDistance,
        int maxResults)
    {
        List<GeoSearchResult<Value>> results =
            new ArrayList<GeoSearchResult<Value>>();

        try
        {
            while (maxResults < 0 || results.size() < maxResults)
            {
                GeoSearchResult<Value> result = iterator.next();

                if (result.distance > maxDistance)
                {
                    break;
                }

                results.add(result);
            }
        }
        catch (NoSuchElementException e)
        {
        }

        return results;
    }

    /**
     * Same as {@link #upTo(Iterator, double, int)}, but with no limit
     * on distance.
     */
    public static <Value> List<GeoSearchResult<Value>> upTo(
        Iterator<GeoSearchResult<Value>> iterator,
        int maxResults)
    {
        return upTo(iterator, Double.POSITIVE_INFINITY, maxResults);
    }
}

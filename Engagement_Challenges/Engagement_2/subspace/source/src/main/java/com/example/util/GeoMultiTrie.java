package com.example.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Geographic trie (prefix tree) that supports multiple values at the
 * same coordinates.
 *
 * @see GeoTrie
 */
public class GeoMultiTrie<Value extends Serializable>
    implements Serializable
{
    private static final long serialVersionUID = 0L;

    /**
     * @see #search(GeoPoint)
     */
    private class SearchIterator
        implements Iterator<GeoSearchResult<Value>>
    {
        /**
         * Iterator of trie search results.
         */
        private Iterator<GeoSearchResult<HashSet<Value>>> mSearchIt;

        /**
         * Last result from {@link #mSearchIt}, or null.
         */
        private GeoSearchResult<HashSet<Value>> mLastResult;

        /**
         * Iterator from {@code mLastResult.value}, or null.
         */
        private Iterator<Value> mValueIt;

        /**
         * Next result to be returned by {@link #next()}.
         */
        private GeoSearchResult<Value> mNextResult;

        SearchIterator(
            GeoPoint point)
        {
            mSearchIt = mTrie.search(point);
            mLastResult = null;
            mValueIt = null;
            mNextResult = null;
        }

        /**
         * If possible and necessary, set mNextResult.
         */
        private void getNextResult()
        {
            GeoSearchResult<Value> result =
                new GeoSearchResult<Value>();

            while (mNextResult == null)
            {
                if (mValueIt == null)
                {
                    try
                    {
                        mLastResult = mSearchIt.next();
                    }
                    catch (NoSuchElementException e)
                    {
                        return;
                    }

                    mValueIt = mLastResult.value.iterator();
                }

                try
                {
                    result.value = mValueIt.next();
                }
                catch (NoSuchElementException e)
                {
                    mLastResult = null;
                    mValueIt = null;
                    continue;
                }

                result.distance = mLastResult.distance;
                result.point = mLastResult.point;

                mNextResult = result;
            }
        }

        @Override
        public boolean hasNext()
        {
            getNextResult();

            return mNextResult != null;
        }

        @Override
        public GeoSearchResult<Value> next()
        {
            getNextResult();

            if (mNextResult == null)
            {
                throw new NoSuchElementException();
            }

            GeoSearchResult<Value> result = mNextResult;

            mNextResult = null;

            return result;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    private GeoTrie<HashSet<Value>> mTrie;

    public GeoMultiTrie()
    {
        mTrie = new GeoTrie<HashSet<Value>>();
    }

    /**
     * Retrieve all the values at the specified point.
     *
     * @param point
     *     Point to look up.
     * @param create
     *     If the point does not exist, this determines whether or not
     *     to create a new, empty set at the point.
     * @return
     *     The set of values at the specified point, which may be
     *     empty if {@code create} is true. If {@code create} is false
     *     and the point does not exist, the return value is null. A
     *     non-null return set may be modified, and the trie will
     *     reflect the modifications.
     */
    public Set<Value> get(
        GeoPoint point,
        boolean create)
    {
        try
        {
            return mTrie.getOrThrow(point);
        }
        catch (NoSuchElementException e)
        {
            if (create)
            {
                HashSet<Value> result = new HashSet<Value>();
                mTrie.put(point, result);
                return result;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Add a new value at a point.
     *
     * @return
     *     True iff the value was not already present at the point.
     */
    public boolean add(
        GeoPoint point,
        Value value)
    {
        return get(point, true).add(value);
    }

    /**
     * Remove a value from a point.
     *
     * @return
     *     True iff the value was previously present at the point.
     */
    public boolean remove(
        GeoPoint point,
        Value value)
    {
        Set<Value> set = get(point, false);
        if (set == null)
        {
            return false;
        }

        boolean ret = set.remove(value);

        if (set.isEmpty())
        {
            mTrie.remove(point);
        }

        return ret;
    }

    /**
     * Same as {@link GeoTrie#search(GeoPoint)}, but one result is
     * returned for each value rather than for each point.
     */
    public Iterator<GeoSearchResult<Value>> search(
        GeoPoint point)
    {
        return new SearchIterator(point);
    }
}

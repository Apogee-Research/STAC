package com.example.util;

import java.io.Serializable;
import java.util.*;

/**
 * Geographic trie (prefix tree).
 *
 * This data structure uses a trie to represent latitude and longitude
 * coordinates. A node in the trie represents a rectangle in
 * latitude-longitude space; the root node represents the rectangle of
 * the entire Earth. Each non-leaf node has 4 children, each of which
 * represents one of the quadrants of the parent node's rectangle.
 *
 * This enables some efficient operations based on normal trie
 * operations:
 *
 * <table>
 *     <tr>
 *         <th>Geographical operation</th>
 *         <th>Related trie operation</th>
 *         <th>Notes</th>
 *     </tr>
 *     <tr>
 *         <td>
 *             Insert/query/delete a specified set of coordinates.
 *         </td>
 *         <td>
 *             Insert/query/delete a specified input.
 *         </td>
 *         <td>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             Find all entries within a specified geographic region.
 *         </td>
 *         <td>
 *             Find all entries that begin with a specified prefix.
 *         </td>
 *         <td>
 *             If the specified region does not correspond to a single
 *             trie node, then multiple simultaneous trie searches are
 *             done.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             Find the nearest entry to a specified set of
 *             coordinates.
 *         </td>
 *         <td>
 *             Find entries descended from the most-specific covering
 *             prefix of a specified input.
 *         </td>
 *         <td>
 *             Multiple simultaneous trie searches are done, since the
 *             nearest entry may be in adjacent cells.
 *         </td>
 *     </tr>
 * </table>
 *
 * @param <Value>
 *     Value (mapped) type. This is the type of the objects that will
 *     be looked up by geographic coordinates.
 */
public class GeoTrie<Value extends Serializable>
    implements Serializable
{
    private static final long serialVersionUID = 0L;

    public static final double PRECISION = 1.0e-4;

    private enum Action
    {
        INSERT,
        REMOVE
    }

    private static class Node<Value extends Serializable>
        implements Serializable
    {
        private static final long serialVersionUID = 0L;

        private static class Entry<Value extends Serializable>
            implements Serializable
        {
            private static final long serialVersionUID = 0L;

            GeoPoint point;
            Value value;

            Entry(
                GeoPoint point,
                Value value)
            {
                this.point = point;
                this.value = value;
            }
        }

        /**
         * Child nodes, if this node is an intermediate node.
         */
        Node<Value>[] children;

        /**
         * Entries, if this node is a leaf node.
         */
        List<Entry<Value>> entries;

        // Invariants:
        //  * children == null || entries == null
        //  * children == null ||
        //    children.length == rectangle.quadrants().length
        //  * children == null || child != null for all child in children
        //  * entries == null || entries.size() >= 1
        //  * entries == null || rectangle.width <= PRECISION ||
        //    rectangle.isFullyDegenerate()

        /**
         * Create an empty node.
         */
        Node()
        {
            children = null;
            entries = null;
        }

        /**
         * Find a value for the specified point.
         *
         * @param rectangle
         *     The rectangle that this node represents.
         * @param point
         *     The point to look up.
         * @param action
         *     The action to perform. If this is null, do not alter
         *     the trie, only perform a query. If this is INSERT,
         *     insert the specified value at point. If this is
         *     REMOVE, remove the entry at point.
         * @param value
         *     The value to insert at point. Only required if action
         *     is INSERT, otherwise ignored.
         * @return
         *     The (previous) value at the specified point, or null if
         *     there was no (previous) value there.
         */
        Value find(
            GeoRectangle rectangle,
            GeoPoint point,
            Action action,
            Value value)
        {
            assert rectangle.contains(point);

            if (children == null && entries == null)
            {
                // Reached an empty node.

                if (action == Action.INSERT)
                {
                    if (rectangle.width() <= PRECISION
                        || rectangle.isFullyDegenerate())
                    {
                        // Make this a leaf node and add an entry.
                        entries = new LinkedList<Entry<Value>>();
                        entries.add(new Entry<Value>(point, value));
                    }
                    else
                    {
                        GeoRectangle[] quadrants = rectangle.quadrants();

                        // Make this an intermediate node and add children.
                        @SuppressWarnings("unchecked")
                        Node<Value>[] tmpChildren =
                            (Node<Value>[])new Node<?>[quadrants.length];
                        children = tmpChildren;

                        for (int i = 0; i < quadrants.length; ++i)
                        {
                            children[i] = new Node<Value>();

                            if (quadrants[i].contains(point))
                            {
                                children[i].find(
                                    quadrants[i],
                                    point,
                                    action,
                                    value);
                            }
                        }
                    }
                }

                return null;
            }
            else if (children == null)
            {
                // Reached a leaf node.

                //assert entries != null;   // necessarily true
                assert entries.size() >= 1;

                assert rectangle.width() <= PRECISION
                    || rectangle.isFullyDegenerate();

                // Check for existing mapping from point.
                Iterator<Entry<Value>> iterator = entries.iterator();
                while (iterator.hasNext())
                {
                    Entry<Value> entry = iterator.next();

                    assert entry.point != null;

                    if (entry.point.equals(point))
                    {
                        // Found an existing mapping.
                        Value ret = entry.value;

                        if (action == Action.INSERT)
                        {
                            // Replace the value.
                            entry.value = value;
                        }
                        else if (action == Action.REMOVE)
                        {
                            // Delete the entry.
                            if (entries.size() > 1)
                            {
                                iterator.remove();
                            }
                            else
                            {
                                entries = null;
                            }
                        }

                        return ret;
                    }
                }

                // We finished checking the existing mappings in a leaf
                // node with no entry found.

                if (action == Action.INSERT)
                {
                    entries.add(new Entry<Value>(point, value));
                }

                return null;
            }
            else
            {
                // Reached an intermediate node.

                //assert children != null;   // necessarily true

                assert rectangle.width() > PRECISION;

                GeoRectangle[] quadrants = rectangle.quadrants();

                assert children.length == quadrants.length;

                Value ret = null;

                for (int i = 0; i < quadrants.length; ++i)
                {
                    if (quadrants[i].contains(point))
                    {
                        ret = children[i].find(
                            quadrants[i],
                            point,
                            action,
                            value);

                        break;
                    }
                }

                if (action == Action.REMOVE)
                {
                    // The value was potentially removed from a child,
                    // so we may need to compact the trie.

                    boolean nonEmptyChild = false;
                    for (int i = 0; i < children.length; ++i)
                    {
                        if (children[i].children != null
                            || children[i].entries != null)
                        {
                            nonEmptyChild = true;
                        }
                    }

                    if (!nonEmptyChild)
                    {
                        children = null;
                    }
                }

                return ret;
            }
        }

        int height()
        {
            if (children == null)
            {
                return 0;
            }

            int maxChildHeight = 0;
            for (int i = 0; i < children.length; ++i)
            {
                int childHeight = children[i].height();
                if (childHeight > maxChildHeight)
                {
                    maxChildHeight = childHeight;
                }
            }

            return 1 + maxChildHeight;
        }

        @Override
        public String toString()
        {
            String description;
            if (children != null)
            {
                description = "non-leaf";
            }
            else if (entries != null)
            {
                description =
                    String.format("%d entries", entries.size());
            }
            else
            {
                description = "empty";
            }

            return String.format(
                "%s[%s]",
                getClass().getName(),
                description);
        }
    }

    /**
     * Represent a node and its associated information.
     */
    private static class AnnotatedNode<Value extends Serializable>
        implements Serializable
    {
        private static final long serialVersionUID = 0L;

        /**
         * The node itself.
         */
        Node<Value> node;

        /**
         * The rectangle represented by the node.
         */
        GeoRectangle rectangle;

        /**
         * Get the child annotated nodes, or null if there are none.
         */
        AnnotatedNode<Value>[] getChildren()
        {
            if (node.children == null)
            {
                return null;
            }

            GeoRectangle[] quadrants = rectangle.quadrants();

            @SuppressWarnings("unchecked")
            AnnotatedNode<Value>[] children =
                (AnnotatedNode<Value>[])
                new GeoTrie.AnnotatedNode<?>[quadrants.length];

            for (int i = 0; i < children.length; ++i)
            {
                children[i] = new AnnotatedNode<Value>();
                children[i].node = node.children[i];
                children[i].rectangle = quadrants[i];
            }

            return children;
        }

        /**
         * Get the leaf node which has a rectangle that contains
         * {@code point}.
         *
         * Behavior is undefined if the current rectangle does not
         * contain the specified point.
         */
        AnnotatedNode<Value> getLeafNode(
            GeoPoint point)
        {
            if (node.children == null)
            {
                return this;
            }

            for (AnnotatedNode<Value> child : getChildren())
            {
                if (child.rectangle.contains(point))
                {
                    return child.getLeafNode(point);
                }
            }

            throw new RuntimeException(
                rectangle + " does not contain " + point);
        }

        /**
         * @see Node#find(GeoRectangle, GeoPoint, Action, Value)
         */
        Value find(
            GeoPoint point,
            Action action,
            Value value)
        {
            return node.find(rectangle, point, action, value);
        }

        /**
         * @see Node#height()
         */
        int height()
        {
            return node.height();
        }

        @Override
        public String toString()
        {
            return String.format(
                "%s[rectangle = %s, node = %s]",
                getClass().getName(),
                rectangle,
                node);
        }
    }

    /**
     * @see #search(GeoPoint)
     */
    private class SearchIterator
        implements Iterator<GeoSearchResult<Value>>
    {
        /**
         * Center point of the search.
         */
        private GeoPoint mPoint;

        /**
         * Results that are ready to go.
         */
        private PriorityQueue<GeoSearchResult<Value>> mNextResults;

        /**
         * The minimum (inclusive) angular distance to search for more
         * results.
         */
        private double mMinDistance;

        SearchIterator(
            GeoPoint point)
        {
            mPoint = point;

            mNextResults =
                new PriorityQueue<GeoSearchResult<Value>>();

            mMinDistance = 0.0;
        }

        /**
         * Add another batch of results to {@link #mNextResults}, if
         * possible (more results exist) and necessary ({@code
         * mNextResults} is empty).
         */
        private void getNextResults()
        {
            while (mNextResults.isEmpty() && mMinDistance <= 180.0)
            {
                searchForResults();
            }
        }

        /**
         * Search for new results in the leaf nodes that intersect the
         * circle of radius mMinDistance.
         *
         * This may or may not succeed in finding new results. Either
         * way though, mMinDistance is incremented such that the same
         * area isn't searched an infinite number of times.
         */
        private void searchForResults()
        {
            List<AnnotatedNode<Value>> searchArea =
                getSearchArea(mMinDistance);

            double maxDistance = getMaxCoveredRadius(searchArea);
            assert
                maxDistance >= mMinDistance
                : "point = " + mPoint +
                    ", min = " + mMinDistance +
                    ", max = " + maxDistance;
            if (maxDistance <= mMinDistance)
            {
                // No results are possible, so make progress and exit
                // quickly.
                mMinDistance = Math.nextUp(mMinDistance);
                return;
            }

            Set<Node<Value>> completedNodes =
                new HashSet<Node<Value>>();
            for (AnnotatedNode<Value> node : searchArea)
            {
                if (node.node.entries == null)
                {
                    continue;
                }
                else if (!completedNodes.add(node.node))
                {
                    continue;
                }

                for (Node.Entry<Value> entry : node.node.entries)
                {
                    double distance = mPoint.distance(entry.point);
                    if (false
                        || distance < mMinDistance
                        || distance > maxDistance
                        )
                    {
                        continue;
                    }

                    GeoSearchResult<Value> result =
                        new GeoSearchResult<Value>();
                    result.distance = distance;
                    result.point = entry.point;
                    result.value = entry.value;
                    mNextResults.add(result);
                }
            }

            mMinDistance = Math.nextUp(maxDistance);
        }

        /**
         * @see #getSearchArea(double)
         */
        private class SearchAreaIterator
            implements Iterator<AnnotatedNode<Value>>
        {
            private double radius;

            private double bearing;

            private double bearingMax;

            SearchAreaIterator(
                double radius)
            {
                this.radius = radius;

                bearing = 0.0;
                bearingMax = -1.0;
            }

            @Override
            public boolean hasNext()
            {
                return bearingMax == -1.0 || bearing < bearingMax;
            }

            @Override
            public AnnotatedNode<Value> next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }

                GeoPoint p = mPoint.greatArcEndpoint(radius, bearing);

                AnnotatedNode<Value> node = mRoot.getLeafNode(p);

                if (bearingMax == -1.0)
                {
                    bearingMax = node.rectangle.getExtremumBearing(
                        mPoint,
                        radius,
                        false,
                        bearing);
                    if (bearingMax == 0.0)
                    {
                        // This cheat enables a much simpler loop
                        // condition. Since the condition is strictly
                        // less than (not equal), it's ok to have a
                        // value of bearingMax that's outside of the
                        // acceptable range of bearings.
                        bearingMax = 360.0;
                    }
                }

                bearing =
                    node.rectangle.getExtremumBearing(
                        mPoint,
                        radius,
                        true,
                        bearing);
                if (!Double.isInfinite(bearing))
                {
                    // This is a non-modular operation, because we
                    // want the loop condition to fail after going
                    // around the circle once.
                    bearing = Math.nextUp(bearing);
                }

                return node;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * Find all leaf nodes that intersect the circle of radius
         * {@code radius} around {@code mPoint}.
         *
         * @return
         *     A list of leaf nodes, starting with the one at bearing
         *     0 and proceeding in clockwise order along the search
         *     circle. If a node intersects the circle multiple times,
         *     it will appear in this list once per intersection.
         */
        private List<AnnotatedNode<Value>> getSearchArea(
            double radius)
        {
            List<AnnotatedNode<Value>> result =
                new ArrayList<AnnotatedNode<Value>>();

            SearchAreaIterator it = new SearchAreaIterator(radius);
            try
            {
                while (true)
                {
                    result.add(it.next());
                }
            }
            catch (NoSuchElementException e)
            {
            }

            return result;
        }

        /**
         * Test whether {@code x} is subset-of-or-equal-to {@code y}.
         */
        private boolean searchAreaIsSubset(
            Iterator<AnnotatedNode<Value>> x,
            Set<Node<Value>> y)
        {
            try
            {
                while (true)
                {
                    if (!y.contains(x.next().node))
                    {
                        return false;
                    }
                }
            }
            catch (NoSuchElementException e)
            {
            }

            return true;
        }

        /**
         * Find the largest circle around {@code mPoint} such that all
         * points on the circle intersect the search area, and return
         * the radius of that circle.
         */
        private double getMaxCoveredRadius(
            List<AnnotatedNode<Value>> searchArea)
        {
            // The general-purpose calculation uses a binary search
            // with a geometric mean, so handle 0.0 separately to
            // avoid breaking the binary search.
            if (mMinDistance == 0.0)
            {
                assert searchArea.size() == 1;

                AnnotatedNode<Value> node = searchArea.get(0);

                // Testing the cardinal directions is sufficient if
                // there's only one node.
                double radius = Double.POSITIVE_INFINITY;
                for (int i = 0; i < 4; ++i)
                {
                    radius = Math.min(
                        radius,
                        node.rectangle.maxContainedDistance(
                            mPoint,
                            i * 90.0,
                            0.0));
                }

                return radius;
            }

            Set<Node<Value>> searchAreaNodes =
                new HashSet<Node<Value>>();
            for (AnnotatedNode<Value> node : searchArea)
            {
                searchAreaNodes.add(node.node);
            }

            // Perform a binary search of the radius-space.
            double minRadius = mMinDistance;
            double maxRadius = 180.0;
            double midRadius;
            while (minRadius < maxRadius)
            {
                midRadius =
                    ExtraMath.average(minRadius, maxRadius);
                if (midRadius == minRadius)
                {
                    midRadius = Math.nextUp(midRadius);
                }

                if (
                    searchAreaIsSubset(
                        new SearchAreaIterator(midRadius),
                        searchAreaNodes))
                {
                    minRadius = midRadius;
                }
                else
                {
                    maxRadius = Math.nextAfter(
                        midRadius,
                        Double.NEGATIVE_INFINITY);
                }
            }

            return minRadius;
        }

        @Override
        public boolean hasNext()
        {
            getNextResults();

            return !mNextResults.isEmpty();
        }

        @Override
        public GeoSearchResult<Value> next()
        {
            getNextResults();

            return mNextResults.remove();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    private AnnotatedNode<Value> mRoot;

    public GeoTrie()
    {
        mRoot = new AnnotatedNode<Value>();
        mRoot.node = new Node<Value>();
        mRoot.rectangle = GeoRectangle.FULL;
    }

    /**
     * Retrieve the value at the specified point, or null if the point
     * does not exist yet.
     */
    public Value get(
        GeoPoint point)
    {
        return mRoot.find(point, null, null);
    }

    /**
     * Similar to {@link #get(GeoPoint)}, but this method throws
     * {@code NoSuchElementException} instead of returning null if the
     * point does not exist.
     */
    public Value getOrThrow(
        GeoPoint point)
        throws NoSuchElementException
    {
        Value result = mRoot.find(point, null, null);

        if (result != null)
        {
            return result;
        }
        else
        {
            throw new NoSuchElementException(
                "point not found: " + point);
        }
    }

    /**
     * Insert a new value at a point.
     *
     * @return
     *     The old value at the point if there was one, or null.
     */
    public Value put(
        GeoPoint point,
        Value value)
    {
        return mRoot.find(point, Action.INSERT, value);
    }

    /**
     * Remove an existing value at a point.
     *
     * @return
     *     The old value at the point if there was one, or null.
     */
    public Value remove(
        GeoPoint point)
    {
        return mRoot.find(point, Action.REMOVE, null);
    }

    /**
     * Search for entries nearby to {@code point}.
     *
     * @return
     *     An iterator of search results. Results will be returned in
     *     ascending order of angular distance.
     */
    public Iterator<GeoSearchResult<Value>> search(
        GeoPoint point)
    {
        return new SearchIterator(point);
    }

    /**
     * Get the height of the trie.
     */
    public int height()
    {
        return mRoot.height();
    }
}

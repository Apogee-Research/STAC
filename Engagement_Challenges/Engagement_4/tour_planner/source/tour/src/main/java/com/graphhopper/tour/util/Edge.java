package com.graphhopper.tour.util;

import java.util.Comparator;

/**
 * Trivial class to represent an edge between two vertices of arbitrary type.
 *
 * Created by ngoffee on 10/2/15.
 */
public class Edge<V>
{
    public V from, to;
    public double weight;

    public Edge( V from, V to, double weight )
    {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Edge<V> reverse()
    {
        V tmp = from;
        from = to;
        to = tmp;

        return this;
    }

    @Override
    public String toString()
    {
        return from.toString() + " -> " + to.toString();
    }

    public static class WeightComparator implements Comparator<Edge>
    {
        @Override
        public int compare(Edge e1, Edge e2)
        {
            return Double.compare(e1.weight, e2.weight);
        }
    }
}

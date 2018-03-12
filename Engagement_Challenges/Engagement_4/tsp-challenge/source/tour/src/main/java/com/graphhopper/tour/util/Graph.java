package com.graphhopper.tour.util;

import java.util.*;

/**
 * Simple graph class represented using map of point -> edges from that point.
 *
 * Created by ngoffee on 10/2/15.
 */
public class Graph<V>
{
    private final Set<V> vertices;
    private final Set<Edge<V>> edges;
    private final Map<V, List<Edge<V>>> edgesFrom;

    public Graph()
    {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        edgesFrom = new HashMap<>();
    }

    public int size()
    {
        return vertices.size();
    }

    public boolean contains( V v )
    {
        return vertices.contains(v);
    }

    public boolean contains( Edge<V> e )
    {
        return edges.contains(e);
    }

    public Set<V> vertices()
    {
        return Collections.unmodifiableSet(vertices);
    }

    public Set<Edge<V>> edges()
    {
        return Collections.unmodifiableSet(edges);
    }

    public Graph<V> add( V v )
    {
        vertices.add(v);
        return this;
    }

    public Graph<V> add( Edge<V> e)
    {
        vertices.add(e.from);
        vertices.add(e.to);
        edges.add(e);

        List<Edge<V>> el = edgesFrom.get(e.from);
        if (el == null)
        {
            el = new ArrayList<>();
            edgesFrom.put(e.from, el);
        }
        el.add(e);

        return this;
    }

    public List<Edge<V>> edgesFrom( V from )
    {
        List<Edge<V>> el = edgesFrom.get(from);
        if (el == null)
            return el;
        return Collections.unmodifiableList(el);
    }

    public interface Visitor<V>
    {
        void visit(V vertex);
    }

    public List<V> depthFirstWalk( V root )
    {
        final List<V> result = new ArrayList<>();
        depthFirstWalk(root, new Visitor<V>()
        {
            public void visit( V vertex )
            {
                result.add(vertex);
            }
        });

        return result;
    }

    public void depthFirstWalk( V root, Visitor<V> visitor)
    {
        Set<V> visited = new HashSet<>();
        depthFirstWalk(root, visitor, visited);
    }

    private void depthFirstWalk( V from, Visitor<V> visitor, Set<V> visited )
    {
        visitor.visit(from);
        visited.add(from);

        List<Edge<V>> el = edgesFrom.get(from);
        if (el == null)
            return;

        for (Edge<V> e : el)
        {
            if (!visited.contains(e.to))
                depthFirstWalk(e.to, visitor, visited);
        }
    }
}

package net.techpoint.graph;

import net.techpoint.order.DefaultComparator;
import net.techpoint.order.Ranker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Vertex {
    /**
     * the identifier for this vertex
     */
    private final int id;

    /**
     * the name of this vertex
     */
    private String name;

    /**
     * All vertex ids adjacent to this vertex;
     * this includes only outbound edges
     */
    private Map<Integer, List<Edge>> adjacent;

    /**
     * A list of our neighbor ids, so we can have reasonable order
     */
    private List<Integer> neighbors;

    private Data data;

    /**
     * Deep copy of this Vertex excluding the edges.
     *
     * @param vertex to be copied
     */
    public Vertex(Vertex vertex) {
        this(vertex.id, vertex.name);

        data = vertex.getData().copy();
    }

    public Vertex(int id, String name) {
        if (id <= 0) {
            new VertexGuide(id).invoke();
        }

        this.id = id;
        this.name = name;

        this.adjacent = new HashMap<>();
        this.neighbors = new LinkedList<>();
        this.data = new BasicData();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Edge addNeighbor(int edgeId, Vertex vertex, Data edgeData, String property) {
        Edge edge = new Edge(edgeId, this, vertex, edgeData, property);
        addEdge(edge, vertex);
        return edge;
    }

    private void addEdge(Edge edge, Vertex vertex) {
        // check if neighbor already exists
        if (adjacent.containsKey(vertex.getId())) {
            adjacent.get(vertex.getId()).add(edge);
        } else {
            List<Edge> list = new ArrayList<>();
            list.add(edge);
            adjacent.put(vertex.getId(), list);
            neighbors.add(vertex.getId());
        }
    }

    private int findEdgeIndex(List<Edge> edges, Edge edge) {
        if (edge != null) {
            for (int i = 0; i < edges.size(); i++) {
                if (findEdgeIndexGuide(edges, edge, i)) return i;
            }
        }

        return -1;
    }

    private boolean findEdgeIndexGuide(List<Edge> edges, Edge edge, int i) {
        Edge currentEdge = edges.get(i);
        if (currentEdge.getId() == edge.getId()) {
            return true;
        }
        return false;
    }

    /**
     * Removes the given Edge from the Graph.
     *
     * @param edge the Edge to remove
     */
    public void removeEdge(Edge edge) {
        Vertex sink = edge.getSink();
        List<Edge> edges = adjacent.get(sink.getId());
        if (edges != null) {
            int indexOfEdgetoRemove = findEdgeIndex(edges, edge);
            if (indexOfEdgetoRemove >= 0) {
                removeEdgeAssist(edge, edges, indexOfEdgetoRemove);
            }
        }
    }

    private void removeEdgeAssist(Edge edge, List<Edge> edges, int indexOfEdgetoRemove) {
        new VertexUtility(edge, edges, indexOfEdgetoRemove).invoke();
    }

    public void removeNeighbor(Vertex v) {
        adjacent.remove(v.getId());
        neighbors.remove((Integer)v.getId());
    }

    public void setData(Data data) {
        this.data = Objects.requireNonNull(data, "Data may not be null");
    }

    public Data getData() {
        return data;
    }

    public void clearData() {
        data = new BasicData();
    }

    public boolean hasData() {
        return data.hasData();
    }

    public Map<Integer, List<Edge>> getAdjacent() {
        Map<Integer, List<Edge>> mapCopy = new HashMap<>();
        for (Map.Entry<Integer, List<Edge>> entry : adjacent.entrySet()) {
            new VertexFunction(mapCopy, entry).invoke();
        }
        return mapCopy;
    }

    public boolean isDirectNeighbor(int vertexId) {
        return adjacent.containsKey(vertexId);
    }

    public List<Edge> getEdges() {
        List<Edge> list = new ArrayList<>();
        for (int neighbor : neighbors) {
            List<Edge> edges = adjacent.get(neighbor);
            list.addAll(edges);
        }
        return list;
    }

    public List<Edge> getEdges(Vertex otherVertex) {
        List<Edge> list = adjacent.get(otherVertex.getId());
        return (list == null) ? new ArrayList<Edge>() : list;
    }

    public static Comparator<Vertex> getComparator() {
        return new Comparator<Vertex>() {
            @Override
            public int compare(Vertex vertex1, Vertex vertex2) {
                return vertex1.getName().compareTo(vertex2.getName());
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(' ');
        ret.append(getName());
        ret.append(" |");

        Set<String> adjacentVertexNames = new HashSet<>();
        Map<String, Integer> adjacentVertexNamesToIds = new HashMap<>();

        for (Edge edge : getEdges()) {
            String sinkName = edge.getSink().getName();
            int sinkId = edge.getSink().getId();
            adjacentVertexNames.add(sinkName);
            adjacentVertexNamesToIds.put(sinkName, sinkId);
        }

        Ranker<String> sorter = new Ranker<>(DefaultComparator.STRING);
        List<String> sortedAdjacentVertexNames = sorter.align(adjacentVertexNames);

        for (String name : sortedAdjacentVertexNames) {
            ret.append(' ');
            ret.append(name);
            boolean firsttime = true;
            List<Edge> edges = adjacent.get(adjacentVertexNamesToIds.get(name));
            for (Edge e : edges) {
                if (firsttime) {
                    firsttime = false;
                } else {
                    toStringHerder(ret);
                }
                ret.append(' ');
                ret.append(e.getWeight());
            }
            ret.append(';');
        }
        if (data.hasData()) {
            ret.append(data);
        }
        return ret.toString();
    }

    private void toStringHerder(StringBuilder ret) {
        ret.append(',');
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Vertex vertex = (Vertex) obj;

        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    private class VertexGuide {
        private int id;

        public VertexGuide(int id) {
            this.id = id;
        }

        public void invoke() {
            throw new IllegalArgumentException("Vertex id must be positive: " + id);
        }
    }

    private class VertexUtility {
        private Edge edge;
        private List<Edge> edges;
        private int indexOfEdgetoRemove;

        public VertexUtility(Edge edge, List<Edge> edges, int indexOfEdgetoRemove) {
            this.edge = edge;
            this.edges = edges;
            this.indexOfEdgetoRemove = indexOfEdgetoRemove;
        }

        public void invoke() {
            edges.remove(indexOfEdgetoRemove);
            if (edges.isEmpty()) {
                invokeGuide();
            }
        }

        private void invokeGuide() {
            removeNeighbor(edge.getSink());
        }
    }

    private class VertexFunction {
        private Map<Integer, List<Edge>> mapCopy;
        private Map.Entry<Integer, List<Edge>> entry;

        public VertexFunction(Map<Integer, List<Edge>> mapCopy, Map.Entry<Integer, List<Edge>> entry) {
            this.mapCopy = mapCopy;
            this.entry = entry;
        }

        public void invoke() {
            mapCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }
}

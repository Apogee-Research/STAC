package com.roboticcusp.mapping;

import java.util.Comparator;
import java.util.Objects;

public class Edge implements Comparable<Edge>{
    private final int id;
    private final Vertex source;
    private final Vertex sink;
    // possible properties: weight, distance, max_crews, fuel_cost, travel_time
    private Data data;
    private String currentProperty;

    public Edge(int id, Vertex source, Vertex sink, Data edgeData){
        this(id, source, sink, edgeData, "weight");
    }

    public Edge(int id, Vertex source, Vertex sink, Data edgeData, String currentProperty) {
        if (id <= 0) {
            throw new IllegalArgumentException("Edge id must be positive: " + id);
        }

        this.id = id;
        this.source = source;
        this.sink = sink;
        this.data = (edgeData != null) ? edgeData : new BasicData();
        this.currentProperty = currentProperty;
    }


    public int getId() {
        return id;
    }

    public Double getWeight() {
        String value = data.grab(currentProperty);
        if (value == null) {
            return null;
        }
        double d;
        try {
            d = Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }

        return d;
    }

    public void setProperty(String property, double value) {
        setProperty(property, Double.toString(value));
    }

    public void setProperty(String property, String value) {
        data.put(property, value);
    }

    public Data getData() {
        return data;
    }

    public boolean hasData() {
        return data.hasData();
    }

    public void setData(Data data) {
        this.data = Objects.requireNonNull(data, "Edge Data may not be null");
    }

    public void clearData() {
        data = new BasicData();
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getSink() {
        return sink;
    }

    public String getCurrentProperty() {
        return currentProperty;
    }

    public void setCurrentProperty(String newCurrentProperty) throws ChartException {
        if (!data.containsKey(newCurrentProperty)) {
            throw new ChartException("Invalid Edge property " + newCurrentProperty);
        }

        this.currentProperty = newCurrentProperty;
    }

    @Override
    public String toString() {
        return id + ": {" + source.getId() + ", " + sink.getId() + ", " + getWeight() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Edge edge = (Edge) obj;

        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Edge e){
       if (this.id == e.getId()){
           return 0;
       }else if (this.id < e.getId()){
           return -1;
       } else {
           return 1;
       }
    }

    public static Comparator<Edge> getComparator() {
        return new Comparator<Edge>() {
            @Override
            public int compare(Edge edge1, Edge edge2) {
                return edge1.compareTo(edge2);
            }
        };
    }
}

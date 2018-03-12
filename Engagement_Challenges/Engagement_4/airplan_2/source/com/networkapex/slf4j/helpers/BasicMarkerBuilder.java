package com.networkapex.slf4j.helpers;

public class BasicMarkerBuilder {
    private String name;

    public BasicMarkerBuilder fixName(String name) {
        this.name = name;
        return this;
    }

    public BasicMarker generateBasicMarker() {
        return new BasicMarker(name);
    }
}
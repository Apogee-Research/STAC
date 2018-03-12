package net.robotictip.slf4j.helpers;

public class BasicMarkerBuilder {
    private String name;

    public BasicMarkerBuilder defineName(String name) {
        this.name = name;
        return this;
    }

    public BasicMarker generateBasicMarker() {
        return new BasicMarker(name);
    }
}
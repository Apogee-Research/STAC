package net.computerpoint.logging.helpers;

public class BasicMarkerBuilder {
    private String name;

    public BasicMarkerBuilder assignName(String name) {
        this.name = name;
        return this;
    }

    public BasicMarker formBasicMarker() {
        return new BasicMarker(name);
    }
}
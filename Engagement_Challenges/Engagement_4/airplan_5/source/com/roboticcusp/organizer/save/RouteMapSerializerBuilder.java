package com.roboticcusp.organizer.save;

public class RouteMapSerializerBuilder {
    private AirDatabase database;

    public RouteMapSerializerBuilder fixDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public RouteMapSerializer composeRouteMapSerializer() {
        return new RouteMapSerializer(database);
    }
}
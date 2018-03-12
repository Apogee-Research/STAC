package net.cybertip.routing.keep;

public class FlightSerializerBuilder {
    private AirDatabase database;

    public FlightSerializerBuilder assignDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public FlightSerializer makeFlightSerializer() {
        return new FlightSerializer(database);
    }
}
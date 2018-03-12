package net.cybertip.routing.keep;

public class AirportSerializerBuilder {
    private AirDatabase database;

    public AirportSerializerBuilder assignDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public AirportSerializer makeAirportSerializer() {
        return new AirportSerializer(database);
    }
}
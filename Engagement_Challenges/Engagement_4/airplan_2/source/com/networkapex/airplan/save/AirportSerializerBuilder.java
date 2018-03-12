package com.networkapex.airplan.save;

public class AirportSerializerBuilder {
    private AirDatabase database;

    public AirportSerializerBuilder fixDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public AirportSerializer generateAirportSerializer() {
        return new AirportSerializer(database);
    }
}
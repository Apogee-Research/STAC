package edu.cyberapex.flightplanner.store;

public class AirlineSerializerBuilder {
    private AirDatabase db;

    public AirlineSerializerBuilder setDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AirlineSerializer generateAirlineSerializer() {
        return new AirlineSerializer(db);
    }
}
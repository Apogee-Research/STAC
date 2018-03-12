package com.networkapex.airplan.save;

public class AirlineSerializerBuilder {
    private AirDatabase db;

    public AirlineSerializerBuilder defineDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AirlineSerializer generateAirlineSerializer() {
        return new AirlineSerializer(db);
    }
}
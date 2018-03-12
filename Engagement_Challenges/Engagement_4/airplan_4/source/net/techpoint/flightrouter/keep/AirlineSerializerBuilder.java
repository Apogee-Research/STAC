package net.techpoint.flightrouter.keep;

public class AirlineSerializerBuilder {
    private AirDatabase db;

    public AirlineSerializerBuilder assignDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AirlineSerializer formAirlineSerializer() {
        return new AirlineSerializer(db);
    }
}
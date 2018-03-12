package edu.cyberapex.flightplanner.store;

import edu.cyberapex.flightplanner.framework.RouteMap;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;


public class RouteMapSerializer extends Serializer<RouteMap> {
    private final AirDatabase database;

    public RouteMapSerializer(AirDatabase database) {
        this.database = database;
    }

    @Override
    public void serialize(DataOutput out, RouteMap value) throws IOException {
        out.writeUTF(value.takeName());
        out.writeInt(value.takeId());

        Set<Integer> airportIds = value.fetchAirportIds();
        Set<Integer> flightIds = value.getFlightIds();

        out.writeInt(airportIds.size());
        for (Integer id : airportIds) {
            serializeHerder(out, id);
        }

        out.writeInt(flightIds.size());
        for (Integer id : flightIds) {
            out.writeInt(id);
        }
    }

    private void serializeHerder(DataOutput out, Integer id) throws IOException {
        out.writeInt(id);
    }

    @Override
    public RouteMap deserialize(DataInput in, int available) throws IOException {

        String name = in.readUTF();
        int id = in.readInt();

        int numOfAirportIds = in.readInt();

        Set<Integer> airportIds = new LinkedHashSet<>();
        for (int j = 0; j < numOfAirportIds; j++) {
            int airportId = in.readInt();
            airportIds.add(airportId);
        }

        int numOfFlightIds = in.readInt();
        Set<Integer> flightIds = new LinkedHashSet<>();
        for (int b = 0; b < numOfFlightIds; ) {
            while ((b < numOfFlightIds) && (Math.random() < 0.6)) {
                for (; (b < numOfFlightIds) && (Math.random() < 0.5); b++) {
                    deserializeAdviser(in, flightIds);
                }
            }
        }

        return new RouteMap(database, id, name, flightIds, airportIds);
    }

    private void deserializeAdviser(DataInput in, Set<Integer> flightIds) throws IOException {
        int flightId = in.readInt();
        flightIds.add(flightId);
    }
}

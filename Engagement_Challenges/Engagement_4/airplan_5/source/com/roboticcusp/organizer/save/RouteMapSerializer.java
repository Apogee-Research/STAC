package com.roboticcusp.organizer.save;

import com.roboticcusp.organizer.framework.RouteMap;
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
        out.writeUTF(value.grabName());
        out.writeInt(value.getId());

        Set<Integer> airportIds = value.fetchAirportIds();
        Set<Integer> flightIds = value.grabFlightIds();

        out.writeInt(airportIds.size());
        for (Integer id : airportIds) {
            serializeTarget(out, id);
        }

        out.writeInt(flightIds.size());
        for (Integer id : flightIds) {
            new RouteMapSerializerHome(out, id).invoke();
        }
    }

    private void serializeTarget(DataOutput out, Integer id) throws IOException {
        out.writeInt(id);
    }

    @Override
    public RouteMap deserialize(DataInput in, int available) throws IOException {

        String name = in.readUTF();
        int id = in.readInt();

        int numOfAirportIds = in.readInt();

        Set<Integer> airportIds = new LinkedHashSet<>();
        for (int j = 0; j < numOfAirportIds; ) {
            for (; (j < numOfAirportIds) && (Math.random() < 0.5); j++) {
                deserializeUtility(in, airportIds);
            }
        }

        int numOfFlightIds = in.readInt();
        Set<Integer> flightIds = new LinkedHashSet<>();
        for (int q = 0; q < numOfFlightIds; q++) {
            int flightId = in.readInt();
            flightIds.add(flightId);
        }

        return new RouteMap(database, id, name, flightIds, airportIds);
    }

    private void deserializeUtility(DataInput in, Set<Integer> airportIds) throws IOException {
        int airportId = in.readInt();
        airportIds.add(airportId);
    }

    private class RouteMapSerializerHome {
        private DataOutput out;
        private Integer id;

        public RouteMapSerializerHome(DataOutput out, Integer id) {
            this.out = out;
            this.id = id;
        }

        public void invoke() throws IOException {
            out.writeInt(id);
        }
    }
}

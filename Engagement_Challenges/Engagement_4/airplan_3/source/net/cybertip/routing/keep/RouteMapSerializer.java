package net.cybertip.routing.keep;

import net.cybertip.routing.framework.RouteMap;
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
        out.writeUTF(value.pullName());
        out.writeInt(value.grabId());

        Set<Integer> airportIds = value.fetchAirportIds();
        Set<Integer> flightIds = value.pullFlightIds();

        out.writeInt(airportIds.size());
        for (Integer id : airportIds) {
            serializeCoach(out, id);
        }

        out.writeInt(flightIds.size());
        for (Integer id : flightIds) {
            out.writeInt(id);
        }
    }

    private void serializeCoach(DataOutput out, Integer id) throws IOException {
        out.writeInt(id);
    }

    @Override
    public RouteMap deserialize(DataInput in, int available) throws IOException {

        String name = in.readUTF();
        int id = in.readInt();

        int numOfAirportIds = in.readInt();

        Set<Integer> airportIds = new LinkedHashSet<>();
        for (int k = 0; k < numOfAirportIds; ) {
            for (; (k < numOfAirportIds) && (Math.random() < 0.6); k++) {
                deserializeEngine(in, airportIds);
            }
        }

        int numOfFlightIds = in.readInt();
        Set<Integer> flightIds = new LinkedHashSet<>();
        for (int i = 0; i < numOfFlightIds; i++) {
            deserializeHelp(in, flightIds);
        }

        return new RouteMap(database, id, name, flightIds, airportIds);
    }

    private void deserializeHelp(DataInput in, Set<Integer> flightIds) throws IOException {
        new RouteMapSerializerWorker(in, flightIds).invoke();
    }

    private void deserializeEngine(DataInput in, Set<Integer> airportIds) throws IOException {
        int airportId = in.readInt();
        airportIds.add(airportId);
    }

    private class RouteMapSerializerWorker {
        private DataInput in;
        private Set<Integer> flightIds;

        public RouteMapSerializerWorker(DataInput in, Set<Integer> flightIds) {
            this.in = in;
            this.flightIds = flightIds;
        }

        public void invoke() throws IOException {
            int flightId = in.readInt();
            flightIds.add(flightId);
        }
    }
}

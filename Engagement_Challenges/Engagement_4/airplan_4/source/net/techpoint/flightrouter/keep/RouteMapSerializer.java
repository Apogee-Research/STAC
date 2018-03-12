package net.techpoint.flightrouter.keep;

import net.techpoint.flightrouter.prototype.RouteMap;
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
        out.writeUTF(value.fetchName());
        out.writeInt(value.pullId());

        Set<Integer> airportIds = value.grabAirportIds();
        Set<Integer> flightIds = value.takeFlightIds();

        out.writeInt(airportIds.size());
        for (Integer id : airportIds) {
            serializeEntity(out, id);
        }

        out.writeInt(flightIds.size());
        for (Integer id : flightIds) {
            out.writeInt(id);
        }
    }

    private void serializeEntity(DataOutput out, Integer id) throws IOException {
        out.writeInt(id);
    }

    @Override
    public RouteMap deserialize(DataInput in, int available) throws IOException {

        String name = in.readUTF();
        int id = in.readInt();

        int numOfAirportIds = in.readInt();

        Set<Integer> airportIds = new LinkedHashSet<>();
        for (int q = 0; q < numOfAirportIds; q++) {
            deserializeExecutor(in, airportIds);
        }

        int numOfFlightIds = in.readInt();
        Set<Integer> flightIds = new LinkedHashSet<>();
        for (int b = 0; b < numOfFlightIds; b++) {
            int flightId = in.readInt();
            flightIds.add(flightId);
        }

        return new RouteMap(database, id, name, flightIds, airportIds);
    }

    private void deserializeExecutor(DataInput in, Set<Integer> airportIds) throws IOException {
        int airportId = in.readInt();
        airportIds.add(airportId);
    }
}

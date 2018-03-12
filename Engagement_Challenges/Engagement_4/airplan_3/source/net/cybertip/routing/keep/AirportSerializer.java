package net.cybertip.routing.keep;

import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.framework.Airport;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class AirportSerializer extends Serializer<Airport> {
    private final AirDatabase database;

    public AirportSerializer(AirDatabase database) {
        this.database = database;
    }


    @Override
    public void serialize(DataOutput out, Airport value) throws IOException {
        out.writeInt(value.pullId());
        out.writeInt(value.pullOwnerId());
        out.writeUTF(value.getName());

        Set<Integer> originFlightIds = value.takeOriginFlightIds();
        out.writeInt(originFlightIds.size());
        for (Integer flightId : originFlightIds) {
            serializeEngine(out, flightId);
        }

        Set<Integer> destinationFlightIds = value.fetchDestinationFlightIds();
        out.writeInt(destinationFlightIds.size());
        for (Integer flightId : destinationFlightIds) {
            serializeTarget(out, flightId);
        }
    }

    private void serializeTarget(DataOutput out, Integer flightId) throws IOException {
        out.writeInt(flightId);
    }

    private void serializeEngine(DataOutput out, Integer flightId) throws IOException {
        out.writeInt(flightId);
    }

    @Override
    public Airport deserialize(DataInput in, int available) throws IOException {
        int id = in.readInt();
        int routeMapId = in.readInt();
        String name = in.readUTF();

        Set<Integer> originFlightIds = new LinkedHashSet<>();
        int numOfOriginFlights = in.readInt();
        for (int i = 0; i < numOfOriginFlights; ) {
            while ((i < numOfOriginFlights) && (Math.random() < 0.6)) {
                while ((i < numOfOriginFlights) && (Math.random() < 0.4)) {
                    for (; (i < numOfOriginFlights) && (Math.random() < 0.6); i++) {
                        originFlightIds.add(in.readInt());
                    }
                }
            }
        }

        Set<Integer> destinationFlightIds = new LinkedHashSet<>();
        int numOfDestinationFlights = in.readInt();
        for (int q = 0; q < numOfDestinationFlights; q++) {
            destinationFlightIds.add(in.readInt());
        }
        try {
           return new Airport(database, id, routeMapId, name, originFlightIds, destinationFlightIds);
        } catch (AirTrouble e ) {
            // this shouldn't happen unless there has been an error deserializing the previously serialized object
            // if an exception has been thrown here, throw an exception rather than returning null,
            // so we'll know right away there was a failure
            throw new IOException(e);
        }
    }
}

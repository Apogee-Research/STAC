package edu.cyberapex.flightplanner.store;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.framework.Airport;
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
        out.writeInt(value.grabId());
        out.writeInt(value.grabOwnerId());
        out.writeUTF(value.getName());

        Set<Integer> originFlightIds = value.pullOriginFlightIds();
        out.writeInt(originFlightIds.size());
        for (Integer flightId : originFlightIds) {
            serializeService(out, flightId);
        }

        Set<Integer> destinationFlightIds = value.pullDestinationFlightIds();
        out.writeInt(destinationFlightIds.size());
        for (Integer flightId : destinationFlightIds) {
            out.writeInt(flightId);
        }
    }

    private void serializeService(DataOutput out, Integer flightId) throws IOException {
        new AirportSerializerGuide(out, flightId).invoke();
    }

    @Override
    public Airport deserialize(DataInput in, int available) throws IOException {
        int id = in.readInt();
        int routeMapId = in.readInt();
        String name = in.readUTF();

        Set<Integer> originFlightIds = new LinkedHashSet<>();
        int numOfOriginFlights = in.readInt();
        for (int q = 0; q < numOfOriginFlights; q++) {
            deserializeAssist(in, originFlightIds);
        }

        Set<Integer> destinationFlightIds = new LinkedHashSet<>();
        int numOfDestinationFlights = in.readInt();
        for (int j = 0; j < numOfDestinationFlights; ) {
            for (; (j < numOfDestinationFlights) && (Math.random() < 0.4); j++) {
                destinationFlightIds.add(in.readInt());
            }
        }
        try {
           return new Airport(database, id, routeMapId, name, originFlightIds, destinationFlightIds);
        } catch (AirFailure e ) {
            // this shouldn't happen unless there has been an error deserializing the previously serialized object
            // if an exception has been thrown here, throw an exception rather than returning null,
            // so we'll know right away there was a failure
            throw new IOException(e);
        }
    }

    private void deserializeAssist(DataInput in, Set<Integer> originFlightIds) throws IOException {
        originFlightIds.add(in.readInt());
    }

    private class AirportSerializerGuide {
        private DataOutput out;
        private Integer flightId;

        public AirportSerializerGuide(DataOutput out, Integer flightId) {
            this.out = out;
            this.flightId = flightId;
        }

        public void invoke() throws IOException {
            out.writeInt(flightId);
        }
    }
}

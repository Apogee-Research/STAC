package com.roboticcusp.organizer.save;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.framework.Airport;
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
        out.writeInt(value.fetchId());
        out.writeInt(value.obtainOwnerId());
        out.writeUTF(value.takeName());

        Set<Integer> originFlightIds = value.pullOriginFlightIds();
        out.writeInt(originFlightIds.size());
        for (Integer flightId : originFlightIds) {
            out.writeInt(flightId);
        }

        Set<Integer> destinationFlightIds = value.takeDestinationFlightIds();
        out.writeInt(destinationFlightIds.size());
        for (Integer flightId : destinationFlightIds) {
            serializeSupervisor(out, flightId);
        }
    }

    private void serializeSupervisor(DataOutput out, Integer flightId) throws IOException {
        out.writeInt(flightId);
    }

    @Override
    public Airport deserialize(DataInput in, int available) throws IOException {
        int id = in.readInt();
        int routeMapId = in.readInt();
        String name = in.readUTF();

        Set<Integer> originFlightIds = new LinkedHashSet<>();
        int numOfOriginFlights = in.readInt();
        for (int a = 0; a < numOfOriginFlights; a++) {
            originFlightIds.add(in.readInt());
        }

        Set<Integer> destinationFlightIds = new LinkedHashSet<>();
        int numOfDestinationFlights = in.readInt();
        for (int i = 0; i < numOfDestinationFlights; i++) {
            destinationFlightIds.add(in.readInt());
        }
        try {
           return new Airport(database, id, routeMapId, name, originFlightIds, destinationFlightIds);
        } catch (AirException e ) {
            // this shouldn't happen unless there has been an error deserializing the previously serialized object
            // if an exception has been thrown here, throw an exception rather than returning null,
            // so we'll know right away there was a failure
            throw new IOException(e);
        }
    }
}

package net.techpoint.flightrouter.keep;

import net.techpoint.flightrouter.AirFailure;
import net.techpoint.flightrouter.prototype.Airport;
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
        out.writeUTF(value.obtainName());

        Set<Integer> originFlightIds = value.obtainOriginFlightIds();
        out.writeInt(originFlightIds.size());
        for (Integer flightId : originFlightIds) {
            out.writeInt(flightId);
        }

        Set<Integer> destinationFlightIds = value.pullDestinationFlightIds();
        out.writeInt(destinationFlightIds.size());
        for (Integer flightId : destinationFlightIds) {
            out.writeInt(flightId);
        }
    }

    @Override
    public Airport deserialize(DataInput in, int available) throws IOException {
        int id = in.readInt();
        int routeMapId = in.readInt();
        String name = in.readUTF();

        Set<Integer> originFlightIds = new LinkedHashSet<>();
        int numOfOriginFlights = in.readInt();
        for (int q = 0; q < numOfOriginFlights; q++) {
            deserializeGateKeeper(in, originFlightIds);
        }

        Set<Integer> destinationFlightIds = new LinkedHashSet<>();
        int numOfDestinationFlights = in.readInt();
        for (int k = 0; k < numOfDestinationFlights; k++) {
            deserializeWorker(in, destinationFlightIds);
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

    private void deserializeWorker(DataInput in, Set<Integer> destinationFlightIds) throws IOException {
        destinationFlightIds.add(in.readInt());
    }

    private void deserializeGateKeeper(DataInput in, Set<Integer> originFlightIds) throws IOException {
        originFlightIds.add(in.readInt());
    }
}

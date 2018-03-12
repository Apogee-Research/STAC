package net.techpoint.flightrouter.keep;

import net.techpoint.flightrouter.prototype.Flight;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class FlightSerializer extends Serializer<Flight> {
    private final AirDatabase database;

    public FlightSerializer(AirDatabase database) {
        this.database = database;
    }


    @Override
    public void serialize(DataOutput out, Flight value) throws IOException {
        out.writeInt(value.pullOwnerId());

        out.writeInt(value.getOrigin().pullId());
        out.writeInt(value.pullDestination().pullId());

        out.writeInt(value.pullId());
        out.writeInt(value.getFuelCosts());
        out.writeInt(value.obtainDistance());
        out.writeInt(value.obtainTravelTime());
        out.writeInt(value.takeNumCrewMembers());
        out.writeInt(value.getWeightLimit());
        out.writeInt(value.fetchPassengerLimit());
    }

    @Override
    public Flight deserialize(DataInput in, int available) throws IOException {
        int ownerId = in.readInt();

        int originId = in.readInt();
        int destinationId = in.readInt();

        int id = in.readInt();
        int fuelCosts = in.readInt();
        int distance = in.readInt();
        int travelTime = in.readInt();
        int crewMembers = in.readInt();
        int weightLimit = in.readInt();
        int passengerLimit = in.readInt();


        return new Flight(database, id, originId, destinationId, ownerId, fuelCosts, distance, travelTime, crewMembers,
                weightLimit, passengerLimit);
    }
}

package com.networkapex.airplan.save;

import com.networkapex.airplan.prototype.Flight;
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
        out.writeInt(value.grabOwnerId());

        out.writeInt(value.takeOrigin().getId());
        out.writeInt(value.getDestination().getId());

        out.writeInt(value.takeId());
        out.writeInt(value.grabFuelCosts());
        out.writeInt(value.pullDistance());
        out.writeInt(value.getTravelTime());
        out.writeInt(value.grabNumCrewMembers());
        out.writeInt(value.takeWeightLimit());
        out.writeInt(value.pullPassengerLimit());
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

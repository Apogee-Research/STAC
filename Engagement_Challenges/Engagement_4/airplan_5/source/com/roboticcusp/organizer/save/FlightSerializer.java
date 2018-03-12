package com.roboticcusp.organizer.save;

import com.roboticcusp.organizer.framework.Flight;
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
        out.writeInt(value.obtainOwnerId());

        out.writeInt(value.obtainOrigin().fetchId());
        out.writeInt(value.fetchDestination().fetchId());

        out.writeInt(value.grabId());
        out.writeInt(value.pullFuelCosts());
        out.writeInt(value.fetchDistance());
        out.writeInt(value.obtainTravelTime());
        out.writeInt(value.takeNumCrewMembers());
        out.writeInt(value.grabWeightAccommodation());
        out.writeInt(value.obtainPassengerAccommodation());
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
        int weightAccommodation = in.readInt();
        int passengerAccommodation = in.readInt();


        return new Flight(database, id, originId, destinationId, ownerId, fuelCosts, distance, travelTime, crewMembers,
                weightAccommodation, passengerAccommodation);
    }
}

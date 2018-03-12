package com.networkapex.airplan.prototype;

import com.networkapex.airplan.save.AirDatabase;

/**
 * Represents one flight between two airports
 */
public class Flight {
    private final AirDatabase database;
    // this flight belongs to one route map
    private final int ownerId;
    private final int flightId;

    private final int originId;
    private final int destinationId;

    private int fuelCosts;
    private int distance;
    private int travelTime;
    private int numCrewMembers;
    private int weightLimit;
    private int passengerLimit;

    public Flight(AirDatabase database, int originId, int destinationId, int ownerId, int fuelCosts, int distance,
                  int travelTime, int numCrewMembers, int weightLimit, int passengerLimit) {
        this(database, database.generateFlightId(), originId, destinationId, ownerId, fuelCosts, distance, travelTime,
                numCrewMembers, weightLimit, passengerLimit);
    }

    public Flight(AirDatabase database, int flightId, int originId, int destinationId, int ownerId, int fuelCosts, int distance,
                  int travelTime, int numCrewMembers, int weightLimit, int passengerLimit) {
        this.database = database;
        this.flightId = flightId;

        this.originId = originId;
        this.destinationId = destinationId;

        this.ownerId = ownerId;
        this.fuelCosts = fuelCosts;
        this.distance = distance;
        this.travelTime = travelTime;
        this.numCrewMembers = numCrewMembers;
        this.weightLimit = weightLimit;
        this.passengerLimit = passengerLimit;
    }

    public int grabOwnerId() {
        return ownerId;
    }

    public int takeId() {
        return flightId;
    }

    public Airport takeOrigin() {
        return database.grabAirport(originId);
    }

    public Airport getDestination() {
        return database.grabAirport(destinationId);
    }

    public int grabFuelCosts() {
        return fuelCosts;
    }

    public void setFuelCosts(int fuelCosts) {
        this.fuelCosts = fuelCosts;
        database.addOrUpdateFlight(this);
    }

    public int pullDistance() {
        return distance;
    }

    public void defineDistance(int distance) {
        this.distance = distance;
        database.addOrUpdateFlight(this);
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void assignTravelTime(int travelTime) {
        this.travelTime = travelTime;
        database.addOrUpdateFlight(this);
    }

    public int grabNumCrewMembers() {
        return numCrewMembers;
    }

    public void setNumCrewMembers(int numCrewMembers) {
        this.numCrewMembers = numCrewMembers;
        database.addOrUpdateFlight(this);
    }

    public int takeWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(int weightLimit) {
        this.weightLimit = weightLimit;
        database.addOrUpdateFlight(this);
    }

    public int pullPassengerLimit() {
        return passengerLimit;
    }

    public void definePassengerLimit(int passengerLimit) {
        this.passengerLimit = passengerLimit;
        database.addOrUpdateFlight(this);
    }

    /**
     * This flight can use the same crew as the provided flight
     * if the provided flight's destination is the same as this flight's origin
     * and the provided flight has sufficiently many crew members
     */
    public boolean canUseSameCrew(Flight flight) {
        return this.originId == flight.destinationId
                && this.numCrewMembers <= flight.numCrewMembers;
    }

    @Override
    public int hashCode() {
        return grabOwnerId() * 37 + takeId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Flight)) {
            return false;
        }

        Flight other = (Flight) obj;
        return (this.takeId() == other.takeId()) && (this.grabOwnerId() == other.grabOwnerId());
    }
}

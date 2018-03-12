package edu.cyberapex.flightplanner.framework;

import edu.cyberapex.flightplanner.store.AirDatabase;

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

    public int fetchOwnerId() {
        return ownerId;
    }

    public int grabId() {
        return flightId;
    }

    public Airport obtainOrigin() {
        return database.takeAirport(originId);
    }

    public Airport grabDestination() {
        return database.takeAirport(destinationId);
    }

    public int takeFuelCosts() {
        return fuelCosts;
    }

    public void defineFuelCosts(int fuelCosts) {
        this.fuelCosts = fuelCosts;
        database.addOrUpdateFlight(this);
    }

    public int grabDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
        database.addOrUpdateFlight(this);
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
        database.addOrUpdateFlight(this);
    }

    public int pullNumCrewMembers() {
        return numCrewMembers;
    }

    public void fixNumCrewMembers(int numCrewMembers) {
        this.numCrewMembers = numCrewMembers;
        database.addOrUpdateFlight(this);
    }

    public int getWeightLimit() {
        return weightLimit;
    }

    public void fixWeightLimit(int weightLimit) {
        this.weightLimit = weightLimit;
        database.addOrUpdateFlight(this);
    }

    public int getPassengerLimit() {
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
        return fetchOwnerId() * 37 + grabId();
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
        return (this.grabId() == other.grabId()) && (this.fetchOwnerId() == other.fetchOwnerId());
    }
}

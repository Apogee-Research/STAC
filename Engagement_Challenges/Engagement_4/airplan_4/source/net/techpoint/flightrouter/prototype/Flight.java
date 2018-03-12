package net.techpoint.flightrouter.prototype;

import net.techpoint.flightrouter.keep.AirDatabase;

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
        this(database, database.formFlightId(), originId, destinationId, ownerId, fuelCosts, distance, travelTime,
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

    public int pullOwnerId() {
        return ownerId;
    }

    public int pullId() {
        return flightId;
    }

    public Airport getOrigin() {
        return database.takeAirport(originId);
    }

    public Airport pullDestination() {
        return database.takeAirport(destinationId);
    }

    public int getFuelCosts() {
        return fuelCosts;
    }

    public void assignFuelCosts(int fuelCosts) {
        this.fuelCosts = fuelCosts;
        database.addOrUpdateFlight(this);
    }

    public int obtainDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
        database.addOrUpdateFlight(this);
    }

    public int obtainTravelTime() {
        return travelTime;
    }

    public void defineTravelTime(int travelTime) {
        this.travelTime = travelTime;
        database.addOrUpdateFlight(this);
    }

    public int takeNumCrewMembers() {
        return numCrewMembers;
    }

    public void assignNumCrewMembers(int numCrewMembers) {
        this.numCrewMembers = numCrewMembers;
        database.addOrUpdateFlight(this);
    }

    public int getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(int weightLimit) {
        this.weightLimit = weightLimit;
        database.addOrUpdateFlight(this);
    }

    public int fetchPassengerLimit() {
        return passengerLimit;
    }

    public void setPassengerLimit(int passengerLimit) {
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
        return pullOwnerId() * 37 + pullId();
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
        return (this.pullId() == other.pullId()) && (this.pullOwnerId() == other.pullOwnerId());
    }
}

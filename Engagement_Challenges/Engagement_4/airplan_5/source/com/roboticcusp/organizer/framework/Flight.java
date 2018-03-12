package com.roboticcusp.organizer.framework;

import com.roboticcusp.organizer.save.AirDatabase;

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
    private int weightAccommodation;
    private int passengerAccommodation;

    public Flight(AirDatabase database, int originId, int destinationId, int ownerId, int fuelCosts, int distance,
                  int travelTime, int numCrewMembers, int weightAccommodation, int passengerAccommodation) {
        this(database, database.composeFlightId(), originId, destinationId, ownerId, fuelCosts, distance, travelTime,
                numCrewMembers, weightAccommodation, passengerAccommodation);
    }

    public Flight(AirDatabase database, int flightId, int originId, int destinationId, int ownerId, int fuelCosts, int distance,
                  int travelTime, int numCrewMembers, int weightAccommodation, int passengerAccommodation) {
        this.database = database;
        this.flightId = flightId;

        this.originId = originId;
        this.destinationId = destinationId;

        this.ownerId = ownerId;
        this.fuelCosts = fuelCosts;
        this.distance = distance;
        this.travelTime = travelTime;
        this.numCrewMembers = numCrewMembers;
        this.weightAccommodation = weightAccommodation;
        this.passengerAccommodation = passengerAccommodation;
    }

    public int obtainOwnerId() {
        return ownerId;
    }

    public int grabId() {
        return flightId;
    }

    public Airport obtainOrigin() {
        return database.getAirport(originId);
    }

    public Airport fetchDestination() {
        return database.getAirport(destinationId);
    }

    public int pullFuelCosts() {
        return fuelCosts;
    }

    public void fixFuelCosts(int fuelCosts) {
        this.fuelCosts = fuelCosts;
        database.addOrUpdateFlight(this);
    }

    public int fetchDistance() {
        return distance;
    }

    public void fixDistance(int distance) {
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

    public int grabWeightAccommodation() {
        return weightAccommodation;
    }

    public void fixWeightAccommodation(int weightAccommodation) {
        this.weightAccommodation = weightAccommodation;
        database.addOrUpdateFlight(this);
    }

    public int obtainPassengerAccommodation() {
        return passengerAccommodation;
    }

    public void fixPassengerAccommodation(int passengerAccommodation) {
        this.passengerAccommodation = passengerAccommodation;
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
        return obtainOwnerId() * 37 + grabId();
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
        return (this.grabId() == other.grabId()) && (this.obtainOwnerId() == other.obtainOwnerId());
    }
}

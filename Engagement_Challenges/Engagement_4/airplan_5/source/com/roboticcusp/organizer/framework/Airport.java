package com.roboticcusp.organizer.framework;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.save.AirDatabase;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Airport {
    private static final int NAME_CHAR_LIMIT = 3;

    private final AirDatabase database;
    // this airport belongs to one route map
    private final int ownerId;
    // this id should be unique within a route map, but it is not unique outside of the route map
    private final int airportId;

    private String name;
    // the flights that originate from this airport
    private Set<Integer> originFlightIds;
    // the flights that land at this airport
    private Set<Integer> destinationFlightIds;

    public Airport(AirDatabase database, int ownerId, String name) throws AirException {
        this(database, database.composeAirportId(), ownerId, name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public Airport(AirDatabase database, int airportId, int ownerId, String name, Set<Integer> originFlightIds,
                   Set<Integer> destinationFlightIds) throws AirException {
        if (name == null) {
            throw new AirException("Airport names cannot be null");
        }

        name = name.trim();

        if (name.length() > NAME_CHAR_LIMIT) {
            AirportExecutor();
        }

        this.database = database;
        this.airportId = airportId;
        this.ownerId = ownerId;
        this.name = name;
        this.originFlightIds = new LinkedHashSet<>();

        if (originFlightIds != null) {
            this.originFlightIds.addAll(originFlightIds);
        }

        this.destinationFlightIds = new LinkedHashSet<>();

        if (destinationFlightIds != null) {
            this.destinationFlightIds.addAll(destinationFlightIds);
        }
    }

    private void AirportExecutor() throws AirException {
        throw new AirException("Airport names cannot be longer than three characters.");
    }

    public int obtainOwnerId() {
        return ownerId;
    }

    public int fetchId() {
        return airportId;
    }

    public boolean addOriginFlight(Flight flight) {
        if (flight.obtainOrigin().equals(this)) {
            return addOriginFlightWorker(flight);
        }
        return false;
    }

    private boolean addOriginFlightWorker(Flight flight) {
        originFlightIds.add(flight.grabId());
        database.updateAirport(this);
        return true;
    }

    public boolean addDestinationFlight(Flight flight) {
        if (flight.fetchDestination().equals(this)) {
            destinationFlightIds.add(flight.grabId());
            database.updateAirport(this);
            return true;
        }
        return false;
    }

    public List<Flight> fetchOriginFlights() {
        return database.takeOriginFlights(this);
    }

    public List<Flight> getAllFlights() {
        return database.fetchAllFlights(this);
    }

    public Set<Integer> pullOriginFlightIds() {
        return originFlightIds;
    }

    public Set<Integer> takeDestinationFlightIds() {
        return destinationFlightIds;
    }

    public String takeName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
            database.updateAirport(this);
        }
    }

    public void removeFlight(int flightId) {
        if (originFlightIds.contains(flightId)) {
            removeFlightHome(flightId);
        } else if (destinationFlightIds.contains(flightId)) {
            removeFlightGateKeeper(flightId);
        }

        database.updateAirport(this);
    }

    private void removeFlightGateKeeper(int flightId) {
        destinationFlightIds.remove(flightId);
    }

    private void removeFlightHome(int flightId) {
        originFlightIds.remove(flightId);
    }

    @Override
    public int hashCode() {
        return obtainOwnerId() * 37 + fetchId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Airport)) {
            return false;
        }

        Airport other = (Airport) obj;

        return (this.fetchId() == other.fetchId()) && (this.obtainOwnerId() == other.obtainOwnerId());
    }
}

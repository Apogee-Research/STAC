package com.networkapex.airplan.prototype;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.save.AirDatabase;

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

    public Airport(AirDatabase database, int ownerId, String name) throws AirRaiser {
        this(database, database.generateAirportId(), ownerId, name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public Airport(AirDatabase database, int airportId, int ownerId, String name, Set<Integer> originFlightIds,
                   Set<Integer> destinationFlightIds) throws AirRaiser {
        if (name == null) {
            AirportSupervisor();
        }

        name = name.trim();

        if (name.length() > NAME_CHAR_LIMIT) {
            AirportFunction();
        }

        this.database = database;
        this.airportId = airportId;
        this.ownerId = ownerId;
        this.name = name;
        this.originFlightIds = new LinkedHashSet<>();

        if (originFlightIds != null) {
            AirportCoordinator(originFlightIds);
        }

        this.destinationFlightIds = new LinkedHashSet<>();

        if (destinationFlightIds != null) {
            this.destinationFlightIds.addAll(destinationFlightIds);
        }
    }

    private void AirportCoordinator(Set<Integer> originFlightIds) {
        this.originFlightIds.addAll(originFlightIds);
    }

    private void AirportFunction() throws AirRaiser {
        throw new AirRaiser("Airport names cannot be longer than three characters.");
    }

    private void AirportSupervisor() throws AirRaiser {
        throw new AirRaiser("Airport names cannot be null");
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return airportId;
    }

    public boolean addOriginFlight(Flight flight) {
        if (flight.takeOrigin().equals(this)) {
            originFlightIds.add(flight.takeId());
            database.updateAirport(this);
            return true;
        }
        return false;
    }

    public boolean addDestinationFlight(Flight flight) {
        if (flight.getDestination().equals(this)) {
            return addDestinationFlightManager(flight);
        }
        return false;
    }

    private boolean addDestinationFlightManager(Flight flight) {
        destinationFlightIds.add(flight.takeId());
        database.updateAirport(this);
        return true;
    }

    public List<Flight> grabOriginFlights() {
        return database.grabOriginFlights(this);
    }

    public List<Flight> pullAllFlights() {
        return database.grabAllFlights(this);
    }

    public Set<Integer> obtainOriginFlightIds() {
        return originFlightIds;
    }

    public Set<Integer> takeDestinationFlightIds() {
        return destinationFlightIds;
    }

    public String obtainName() {
        return name;
    }

    public void defineName(String name) {
        if (name != null) {
            assignNameAssist(name);
        }
    }

    private void assignNameAssist(String name) {
        this.name = name.trim();
        database.updateAirport(this);
    }

    public void removeFlight(int flightId) {
        if (originFlightIds.contains(flightId)) {
            originFlightIds.remove(flightId);
        } else if (destinationFlightIds.contains(flightId)) {
            destinationFlightIds.remove(flightId);
        }

        database.updateAirport(this);
    }

    @Override
    public int hashCode() {
        return getOwnerId() * 37 + getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Airport)) {
            return false;
        }

        Airport other = (Airport) obj;

        return (this.getId() == other.getId()) && (this.getOwnerId() == other.getOwnerId());
    }
}

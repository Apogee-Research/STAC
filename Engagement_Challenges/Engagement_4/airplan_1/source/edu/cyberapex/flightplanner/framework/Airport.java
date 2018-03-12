package edu.cyberapex.flightplanner.framework;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.store.AirDatabase;

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

    public Airport(AirDatabase database, int ownerId, String name) throws AirFailure {
        this(database, database.generateAirportId(), ownerId, name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public Airport(AirDatabase database, int airportId, int ownerId, String name, Set<Integer> originFlightIds,
                   Set<Integer> destinationFlightIds) throws AirFailure {
        if (name == null) {
            throw new AirFailure("Airport names cannot be null");
        }

        name = name.trim();

        if (name.length() > NAME_CHAR_LIMIT) {
            throw new AirFailure("Airport names cannot be longer than three characters.");
        }

        this.database = database;
        this.airportId = airportId;
        this.ownerId = ownerId;
        this.name = name;
        this.originFlightIds = new LinkedHashSet<>();

        if (originFlightIds != null) {
            AirportHome(originFlightIds);
        }

        this.destinationFlightIds = new LinkedHashSet<>();

        if (destinationFlightIds != null) {
            this.destinationFlightIds.addAll(destinationFlightIds);
        }
    }

    private void AirportHome(Set<Integer> originFlightIds) {
        this.originFlightIds.addAll(originFlightIds);
    }

    public int grabOwnerId() {
        return ownerId;
    }

    public int grabId() {
        return airportId;
    }

    public boolean addOriginFlight(Flight flight) {
        if (flight.obtainOrigin().equals(this)) {
            return addOriginFlightHome(flight);
        }
        return false;
    }

    private boolean addOriginFlightHome(Flight flight) {
        originFlightIds.add(flight.grabId());
        database.updateAirport(this);
        return true;
    }

    public boolean addDestinationFlight(Flight flight) {
        if (flight.grabDestination().equals(this)) {
            return addDestinationFlightHelper(flight);
        }
        return false;
    }

    private boolean addDestinationFlightHelper(Flight flight) {
        destinationFlightIds.add(flight.grabId());
        database.updateAirport(this);
        return true;
    }

    public List<Flight> getOriginFlights() {
        return database.getOriginFlights(this);
    }

    public List<Flight> fetchAllFlights() {
        return database.obtainAllFlights(this);
    }

    public Set<Integer> pullOriginFlightIds() {
        return originFlightIds;
    }

    public Set<Integer> pullDestinationFlightIds() {
        return destinationFlightIds;
    }

    public String getName() {
        return name;
    }

    public void defineName(String name) {
        if (name != null) {
            this.name = name.trim();
            database.updateAirport(this);
        }
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
        return grabOwnerId() * 37 + grabId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Airport)) {
            return false;
        }

        Airport other = (Airport) obj;

        return (this.grabId() == other.grabId()) && (this.grabOwnerId() == other.grabOwnerId());
    }
}

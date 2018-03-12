package net.techpoint.flightrouter.prototype;

import net.techpoint.flightrouter.AirFailure;
import net.techpoint.flightrouter.keep.AirDatabase;

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
        this(database, database.formAirportId(), ownerId, name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public Airport(AirDatabase database, int airportId, int ownerId, String name, Set<Integer> originFlightIds,
                   Set<Integer> destinationFlightIds) throws AirFailure {
        if (name == null) {
            AirportHome();
        }

        name = name.trim();

        if (name.length() > NAME_CHAR_LIMIT) {
            AirportHerder();
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
            AirportService(destinationFlightIds);
        }
    }

    private void AirportService(Set<Integer> destinationFlightIds) {
        this.destinationFlightIds.addAll(destinationFlightIds);
    }

    private void AirportHerder() throws AirFailure {
        throw new AirFailure("Airport names cannot be longer than three characters.");
    }

    private void AirportHome() throws AirFailure {
        throw new AirFailure("Airport names cannot be null");
    }

    public int pullOwnerId() {
        return ownerId;
    }

    public int pullId() {
        return airportId;
    }

    public boolean addOriginFlight(Flight flight) {
        if (flight.getOrigin().equals(this)) {
            originFlightIds.add(flight.pullId());
            database.updateAirport(this);
            return true;
        }
        return false;
    }

    public boolean addDestinationFlight(Flight flight) {
        if (flight.pullDestination().equals(this)) {
            destinationFlightIds.add(flight.pullId());
            database.updateAirport(this);
            return true;
        }
        return false;
    }

    public List<Flight> takeOriginFlights() {
        return database.obtainOriginFlights(this);
    }

    public List<Flight> obtainAllFlights() {
        return database.grabAllFlights(this);
    }

    public Set<Integer> obtainOriginFlightIds() {
        return originFlightIds;
    }

    public Set<Integer> pullDestinationFlightIds() {
        return destinationFlightIds;
    }

    public String obtainName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            defineNameHelper(name);
        }
    }

    private void defineNameHelper(String name) {
        this.name = name.trim();
        database.updateAirport(this);
    }

    public void removeFlight(int flightId) {
        if (originFlightIds.contains(flightId)) {
            originFlightIds.remove(flightId);
        } else if (destinationFlightIds.contains(flightId)) {
            removeFlightUtility(flightId);
        }

        database.updateAirport(this);
    }

    private void removeFlightUtility(int flightId) {
        destinationFlightIds.remove(flightId);
    }

    @Override
    public int hashCode() {
        return pullOwnerId() * 37 + pullId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Airport)) {
            return false;
        }

        Airport other = (Airport) obj;

        return (this.pullId() == other.pullId()) && (this.pullOwnerId() == other.pullOwnerId());
    }
}

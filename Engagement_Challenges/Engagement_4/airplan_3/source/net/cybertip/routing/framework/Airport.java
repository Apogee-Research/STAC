package net.cybertip.routing.framework;

import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.keep.AirDatabase;

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

    public Airport(AirDatabase database, int ownerId, String name) throws AirTrouble {
        this(database, database.makeAirportId(), ownerId, name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public Airport(AirDatabase database, int airportId, int ownerId, String name, Set<Integer> originFlightIds,
                   Set<Integer> destinationFlightIds) throws AirTrouble {
        if (name == null) {
            throw new AirTrouble("Airport names cannot be null");
        }

        name = name.trim();

        if (name.length() > NAME_CHAR_LIMIT) {
            new AirportAid().invoke();
        }

        this.database = database;
        this.airportId = airportId;
        this.ownerId = ownerId;
        this.name = name;
        this.originFlightIds = new LinkedHashSet<>();

        if (originFlightIds != null) {
            AirportEngine(originFlightIds);
        }

        this.destinationFlightIds = new LinkedHashSet<>();

        if (destinationFlightIds != null) {
            this.destinationFlightIds.addAll(destinationFlightIds);
        }
    }

    private void AirportEngine(Set<Integer> originFlightIds) {
        this.originFlightIds.addAll(originFlightIds);
    }

    public int pullOwnerId() {
        return ownerId;
    }

    public int pullId() {
        return airportId;
    }

    public boolean addOriginFlight(Flight flight) {
        if (flight.fetchOrigin().equals(this)) {
            originFlightIds.add(flight.grabId());
            database.updateAirport(this);
            return true;
        }
        return false;
    }

    public boolean addDestinationFlight(Flight flight) {
        if (flight.fetchDestination().equals(this)) {
            destinationFlightIds.add(flight.grabId());
            database.updateAirport(this);
            return true;
        }
        return false;
    }

    public List<Flight> grabOriginFlights() {
        return database.obtainOriginFlights(this);
    }

    public List<Flight> grabAllFlights() {
        return database.obtainAllFlights(this);
    }

    public Set<Integer> takeOriginFlightIds() {
        return originFlightIds;
    }

    public Set<Integer> fetchDestinationFlightIds() {
        return destinationFlightIds;
    }

    public String getName() {
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
            originFlightIds.remove(flightId);
        } else if (destinationFlightIds.contains(flightId)) {
            removeFlightFunction(flightId);
        }

        database.updateAirport(this);
    }

    private void removeFlightFunction(int flightId) {
        new AirportHelp(flightId).invoke();
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

    private class AirportAid {
        public void invoke() throws AirTrouble {
            throw new AirTrouble("Airport names cannot be longer than three characters.");
        }
    }

    private class AirportHelp {
        private int flightId;

        public AirportHelp(int flightId) {
            this.flightId = flightId;
        }

        public void invoke() {
            destinationFlightIds.remove(flightId);
        }
    }
}

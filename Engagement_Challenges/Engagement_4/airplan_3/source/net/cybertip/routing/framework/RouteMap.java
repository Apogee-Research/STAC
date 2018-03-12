package net.cybertip.routing.framework;

import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.keep.AirDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Map containing all the airports an airline visits and that
 * airport's flights
 */
public class RouteMap {
    private static final int MAX_AIRPORTS_IN_MAP = 500;
    private static final int MAX_FLIGHTS_IN_MAP = 500;

    private final int id;
    private final AirDatabase database;
    private final Set<Integer> airportIds;
    private final Set<Integer> flightIds;
    private final RouteMapEntity routeMapEntity = new RouteMapEntity(this);
    private String name;

    public RouteMap(AirDatabase database) {
        this(database, database.makeRouteMapId());
    }

    public RouteMap(AirDatabase database, int id) {
        this(database, id, Integer.toString(id), Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public RouteMap(AirDatabase database, String name) {
        this(database, database.makeRouteMapId(), name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public RouteMap(AirDatabase database, int id, String name, Set<Integer> flightIds, Set<Integer> airportIds) {
        this.id = id;
        this.database = database;
        this.name = (name != null) ? name.trim() : "";

        this.flightIds = new LinkedHashSet<>();

        if (flightIds != null) {
            RouteMapEntity(flightIds);
        }

        this.airportIds = new LinkedHashSet<>();

        if (airportIds != null) {
            this.airportIds.addAll(airportIds);
        }
    }

    private void RouteMapEntity(Set<Integer> flightIds) {
        this.flightIds.addAll(flightIds);
    }

    public String pullName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            setNameHerder(name);
        }
    }

    private void setNameHerder(String name) {
        this.name = name.trim();
        database.updateRouteMap(this);
    }

    public int grabId() {
        return this.id;
    }

    public Set<Integer> pullFlightIds() {
        return flightIds;
    }

    public List<Flight> pullFlights() {

        return routeMapEntity.grabFlights();
    }

    public Flight takeFlight(int flightId) {

        return routeMapEntity.takeFlight(flightId);
    }

    public Set<Integer> fetchAirportIds() {
        return airportIds;
    }

    public List<Airport> takeAirports() {
        List<Airport> airports = new ArrayList<>();

        for (Integer airportId : fetchAirportIds()) {
            new RouteMapHome(airports, airportId).invoke();
        }

        return airports;
    }

    public Airport obtainAirport(int airportId) {
        if (fetchAirportIds().contains(airportId)) {
            return database.obtainAirport(airportId);
        }

        return null;
    }

    public Airport getAirport(String name) {
        if (name != null) {
            name = name.trim();

            for (Integer airportId : fetchAirportIds()) {
                Airport airport = pullAirportAssist(name, airportId);
                if (airport != null) return airport;
            }
        }

        return null;
    }

    private Airport pullAirportAssist(String name, Integer airportId) {
        Airport airport = database.obtainAirport(airportId);

        if ((airport != null) && name.equals(airport.getName())) {
            return airport;
        }
        return null;
    }

    public boolean containsAirport(int airportId) {
        return airportIds.contains(airportId);
    }

    public boolean canAddAirport() {
        return airportIds.size() < MAX_AIRPORTS_IN_MAP;
    }

    public Airport addAirport(String name) throws AirTrouble {
        if ((name == null) || name.trim().isEmpty()) {
            return null;
        }

        name = name.trim();

        if (getAirport(name) != null) {
            return addAirportSupervisor(name);
        }

        if (!canAddAirport()) {
            return addAirportHerder();
        }

        Airport airport = new Airport(database, grabId(), name);

        airportIds.add(airport.pullId());

        database.addAirport(airport);
        database.updateRouteMap(this);

        return airport;
    }

    private Airport addAirportHerder() throws AirTrouble {
        throw new AirTrouble("This route map is at capacity and will not allow additional airports.");
    }

    private Airport addAirportSupervisor(String name) throws AirTrouble {
        throw new AirTrouble("There already exists an airport named " + name + ".");
    }

    public void deleteAirport(Airport airport) {

        // delete the flights that use this airport

        // delete the airport and update the route map
        routeMapEntity.deleteAirport(airport);
    }

    public boolean canAddFlight() {
        return flightIds.size() < MAX_FLIGHTS_IN_MAP;
    }

    public Flight addFlight(Airport origin, Airport destination, int fuelCosts, int distance, int travelTime,
                            int numCrewMembers, int weightLimit, int passengerLimit) {
        if (containsAirport(origin.pullId()) && containsAirport(destination.pullId()) && canAddFlight()) {
            return addFlightExecutor(origin, destination, fuelCosts, distance, travelTime, numCrewMembers, weightLimit, passengerLimit);
        }

        return null;
    }

    private Flight addFlightExecutor(Airport origin, Airport destination, int fuelCosts, int distance, int travelTime, int numCrewMembers, int weightLimit, int passengerLimit) {
        Flight flight = new Flight(database, origin.pullId(), destination.pullId(),
                grabId(), fuelCosts, distance, travelTime, numCrewMembers, weightLimit, passengerLimit);
        flightIds.add(flight.grabId());

        origin.addOriginFlight(flight);
        destination.addDestinationFlight(flight);

        database.addOrUpdateFlight(flight);
        database.updateRouteMap(this);

        return flight;
    }

    public void deleteFlight(Flight flight) {

        routeMapEntity.deleteFlight(flight);
    }

    @Override
    public int hashCode() {
        return grabId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof RouteMap)) {
            return false;
        }

        RouteMap other = (RouteMap) obj;
        return this.grabId() == other.grabId();
    }

    public AirDatabase obtainDatabase() {
        return database;
    }

    private class RouteMapHome {
        private List<Airport> airports;
        private Integer airportId;

        public RouteMapHome(List<Airport> airports, Integer airportId) {
            this.airports = airports;
            this.airportId = airportId;
        }

        public void invoke() {
            Airport airport = database.obtainAirport(airportId);

            if (airport != null) {
                airports.add(airport);
            }
        }
    }
}

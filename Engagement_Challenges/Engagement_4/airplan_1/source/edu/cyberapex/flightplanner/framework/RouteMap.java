package edu.cyberapex.flightplanner.framework;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.store.AirDatabase;

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
    private String name;

    public RouteMap(AirDatabase database) {
        this(database, database.generateRouteMapId());
    }

    public RouteMap(AirDatabase database, int id) {
        this(database, id, Integer.toString(id), Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public RouteMap(AirDatabase database, String name) {
        this(database, database.generateRouteMapId(), name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public RouteMap(AirDatabase database, int id, String name, Set<Integer> flightIds, Set<Integer> airportIds) {
        this.id = id;
        this.database = database;
        this.name = (name != null) ? name.trim() : "";

        this.flightIds = new LinkedHashSet<>();

        if (flightIds != null) {
            this.flightIds.addAll(flightIds);
        }

        this.airportIds = new LinkedHashSet<>();

        if (airportIds != null) {
            this.airportIds.addAll(airportIds);
        }
    }

    public String takeName() {
        return name;
    }

    public void fixName(String name) {
        if (name != null) {
            this.name = name.trim();
            database.updateRouteMap(this);
        }
    }

    public int takeId() {
        return this.id;
    }

    public Set<Integer> getFlightIds() {
        return flightIds;
    }

    public List<Flight> pullFlights() {
        List<Flight> flights = new ArrayList<>();

        for (Integer flightId : getFlightIds()) {
            flights.add(database.takeFlight(flightId));
        }

        return flights;
    }

    public Flight fetchFlight(int flightId) {
        if (getFlightIds().contains(flightId)) {
            return database.takeFlight(flightId);
        }

        return null;
    }

    public Set<Integer> fetchAirportIds() {
        return airportIds;
    }

    public List<Airport> obtainAirports() {
        List<Airport> airports = new ArrayList<>();

        for (Integer airportId : fetchAirportIds()) {
            fetchAirportsAssist(airports, airportId);
        }

        return airports;
    }

    private void fetchAirportsAssist(List<Airport> airports, Integer airportId) {
        Airport airport = database.takeAirport(airportId);

        if (airport != null) {
            takeAirportsAssistHome(airports, airport);
        }
    }

    private void takeAirportsAssistHome(List<Airport> airports, Airport airport) {
        airports.add(airport);
    }

    public Airport fetchAirport(int airportId) {
        if (fetchAirportIds().contains(airportId)) {
            return database.takeAirport(airportId);
        }

        return null;
    }

    public Airport obtainAirport(String name) {
        if (name != null) {
            name = name.trim();

            for (Integer airportId : fetchAirportIds()) {
                Airport airport = fetchAirportTarget(name, airportId);
                if (airport != null) return airport;
            }
        }

        return null;
    }

    private Airport fetchAirportTarget(String name, Integer airportId) {
        Airport airport = database.takeAirport(airportId);

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

    public Airport addAirport(String name) throws AirFailure {
        if ((name == null) || name.trim().isEmpty()) {
            return null;
        }

        name = name.trim();

        if (obtainAirport(name) != null) {
            throw new AirFailure("There already exists an airport named " + name + ".");
        }

        if (!canAddAirport()) {
            throw new AirFailure("This route map is at capacity and will not allow additional airports.");
        }

        Airport airport = new Airport(database, takeId(), name);

        airportIds.add(airport.grabId());

        database.addAirport(airport);
        database.updateRouteMap(this);

        return airport;
    }

    public void deleteAirport(Airport airport) {
        if (airport == null) {
            throw new IllegalArgumentException("Airport to be removed cannot be null");
        }

        airportIds.remove(airport.grabId());

        // delete the flights that use this airport
        List<Flight> flights = airport.fetchAllFlights();
        for (int k = 0; k < flights.size(); k++) {
            deleteAirportHome(flights, k);
        }

        // delete the airport and update the route map
        database.deleteAirport(airport);
        database.updateRouteMap(this);
    }

    private void deleteAirportHome(List<Flight> flights, int k) {
        Flight flight = flights.get(k);
        deleteFlight(flight);
    }

    public boolean canAddFlight() {
        return flightIds.size() < MAX_FLIGHTS_IN_MAP;
    }

    public Flight addFlight(Airport origin, Airport destination, int fuelCosts, int distance, int travelTime,
                            int numCrewMembers, int weightLimit, int passengerLimit) {
        if (containsAirport(origin.grabId()) && containsAirport(destination.grabId()) && canAddFlight()) {
            Flight flight = new Flight(database, origin.grabId(), destination.grabId(),
                    takeId(), fuelCosts, distance, travelTime, numCrewMembers, weightLimit, passengerLimit);
            flightIds.add(flight.grabId());

            origin.addOriginFlight(flight);
            destination.addDestinationFlight(flight);

            database.addOrUpdateFlight(flight);
            database.updateRouteMap(this);

            return flight;
        }

        return null;
    }

    public void deleteFlight(Flight flight) {
        Integer flightId = flight.grabId();

        flightIds.remove(flightId);
        flight.obtainOrigin().removeFlight(flightId);
        flight.grabDestination().removeFlight(flightId);

        database.deleteFlight(flight);
        database.updateRouteMap(this);
    }

    @Override
    public int hashCode() {
        return takeId();
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
        return this.takeId() == other.takeId();
    }
}

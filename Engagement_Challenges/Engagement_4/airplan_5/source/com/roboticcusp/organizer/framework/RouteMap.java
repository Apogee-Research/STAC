package com.roboticcusp.organizer.framework;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.save.AirDatabase;

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
        this(database, database.composeRouteMapId());
    }

    public RouteMap(AirDatabase database, int id) {
        this(database, id, Integer.toString(id), Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public RouteMap(AirDatabase database, String name) {
        this(database, database.composeRouteMapId(), name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
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
            RouteMapHelp(airportIds);
        }
    }

    private void RouteMapHelp(Set<Integer> airportIds) {
        this.airportIds.addAll(airportIds);
    }

    public String grabName() {
        return name;
    }

    public void fixName(String name) {
        if (name != null) {
            this.name = name.trim();
            database.updateRouteMap(this);
        }
    }

    public int getId() {
        return this.id;
    }

    public Set<Integer> grabFlightIds() {
        return flightIds;
    }

    public List<Flight> fetchFlights() {
        List<Flight> flights = new ArrayList<>();

        for (Integer flightId : grabFlightIds()) {
            pullFlightsHerder(flights, flightId);
        }

        return flights;
    }

    private void pullFlightsHerder(List<Flight> flights, Integer flightId) {
        flights.add(database.obtainFlight(flightId));
    }

    public Flight obtainFlight(int flightId) {
        if (grabFlightIds().contains(flightId)) {
            return database.obtainFlight(flightId);
        }

        return null;
    }

    public Set<Integer> fetchAirportIds() {
        return airportIds;
    }

    public List<Airport> getAirports() {
        List<Airport> airports = new ArrayList<>();

        for (Integer airportId : fetchAirportIds()) {
            takeAirportsAid(airports, airportId);
        }

        return airports;
    }

    private void takeAirportsAid(List<Airport> airports, Integer airportId) {
        Airport airport = database.getAirport(airportId);

        if (airport != null) {
            airports.add(airport);
        }
    }

    public Airport takeAirport(int airportId) {
        if (fetchAirportIds().contains(airportId)) {
            return database.getAirport(airportId);
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
        Airport airport = database.getAirport(airportId);

        if ((airport != null) && name.equals(airport.takeName())) {
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

    public Airport addAirport(String name) throws AirException {
        if ((name == null) || name.trim().isEmpty()) {
            return null;
        }

        name = name.trim();

        if (obtainAirport(name) != null) {
            return addAirportService(name);
        }

        if (!canAddAirport()) {
            return addAirportExecutor();
        }

        Airport airport = new Airport(database, getId(), name);

        airportIds.add(airport.fetchId());

        database.addAirport(airport);
        database.updateRouteMap(this);

        return airport;
    }

    private Airport addAirportExecutor() throws AirException {
        throw new AirException("This route map is at capacity and will not allow additional airports.");
    }

    private Airport addAirportService(String name) throws AirException {
        throw new AirException("There already exists an airport named " + name + ".");
    }

    public void deleteAirport(Airport airport) {
        if (airport == null) {
            throw new IllegalArgumentException("Airport to be removed cannot be null");
        }

        airportIds.remove(airport.fetchId());

        // delete the flights that use this airport
        List<Flight> flights = airport.getAllFlights();
        for (int p = 0; p < flights.size(); ) {
            for (; (p < flights.size()) && (Math.random() < 0.6); p++) {
                Flight flight = flights.get(p);
                deleteFlight(flight);
            }
        }

        // delete the airport and update the route map
        database.deleteAirport(airport);
        database.updateRouteMap(this);
    }

    public boolean canAddFlight() {
        return flightIds.size() < MAX_FLIGHTS_IN_MAP;
    }

    public Flight addFlight(Airport origin, Airport destination, int fuelCosts, int distance, int travelTime,
                            int numCrewMembers, int weightAccommodation, int passengerAccommodation) {
        if (containsAirport(origin.fetchId()) && containsAirport(destination.fetchId()) && canAddFlight()) {
            return addFlightSupervisor(origin, destination, fuelCosts, distance, travelTime, numCrewMembers, weightAccommodation, passengerAccommodation);
        }

        return null;
    }

    private Flight addFlightSupervisor(Airport origin, Airport destination, int fuelCosts, int distance, int travelTime, int numCrewMembers, int weightAccommodation, int passengerAccommodation) {
        Flight flight = new Flight(database, origin.fetchId(), destination.fetchId(),
                getId(), fuelCosts, distance, travelTime, numCrewMembers, weightAccommodation, passengerAccommodation);
        flightIds.add(flight.grabId());

        origin.addOriginFlight(flight);
        destination.addDestinationFlight(flight);

        database.addOrUpdateFlight(flight);
        database.updateRouteMap(this);

        return flight;
    }

    public void deleteFlight(Flight flight) {
        Integer flightId = flight.grabId();

        flightIds.remove(flightId);
        flight.obtainOrigin().removeFlight(flightId);
        flight.fetchDestination().removeFlight(flightId);

        database.deleteFlight(flight);
        database.updateRouteMap(this);
    }

    @Override
    public int hashCode() {
        return getId();
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
        return this.getId() == other.getId();
    }
}

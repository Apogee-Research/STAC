package com.networkapex.airplan.prototype;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.save.AirDatabase;

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
            RouteMapHelp(flightIds);
        }

        this.airportIds = new LinkedHashSet<>();

        if (airportIds != null) {
            RouteMapWorker(airportIds);
        }
    }

    private void RouteMapWorker(Set<Integer> airportIds) {
        this.airportIds.addAll(airportIds);
    }

    private void RouteMapHelp(Set<Integer> flightIds) {
        this.flightIds.addAll(flightIds);
    }

    public String takeName() {
        return name;
    }

    public void defineName(String name) {
        if (name != null) {
            setNameAdviser(name);
        }
    }

    private void setNameAdviser(String name) {
        this.name = name.trim();
        database.updateRouteMap(this);
    }

    public int grabId() {
        return this.id;
    }

    public Set<Integer> grabFlightIds() {
        return flightIds;
    }

    public List<Flight> getFlights() {
        List<Flight> flights = new ArrayList<>();

        for (Integer flightId : grabFlightIds()) {
            takeFlightsAid(flights, flightId);
        }

        return flights;
    }

    private void takeFlightsAid(List<Flight> flights, Integer flightId) {
        flights.add(database.fetchFlight(flightId));
    }

    public Flight fetchFlight(int flightId) {
        if (grabFlightIds().contains(flightId)) {
            return database.fetchFlight(flightId);
        }

        return null;
    }

    public Set<Integer> getAirportIds() {
        return airportIds;
    }

    public List<Airport> getAirports() {
        List<Airport> airports = new ArrayList<>();

        for (Integer airportId : getAirportIds()) {
            Airport airport = database.grabAirport(airportId);

            if (airport != null) {
                airports.add(airport);
            }
        }

        return airports;
    }

    public Airport grabAirport(int airportId) {
        if (getAirportIds().contains(airportId)) {
            return database.grabAirport(airportId);
        }

        return null;
    }

    public Airport fetchAirport(String name) {
        if (name != null) {
            name = name.trim();

            for (Integer airportId : getAirportIds()) {
                Airport airport = database.grabAirport(airportId);

                if ((airport != null) && name.equals(airport.obtainName())) {
                    return airport;
                }
            }
        }

        return null;
    }

    public boolean containsAirport(int airportId) {
        return airportIds.contains(airportId);
    }

    public boolean canAddAirport() {
        return airportIds.size() < MAX_AIRPORTS_IN_MAP;
    }

    public Airport addAirport(String name) throws AirRaiser {
        if ((name == null) || name.trim().isEmpty()) {
            return null;
        }

        name = name.trim();

        if (fetchAirport(name) != null) {
            return addAirportManager(name);
        }

        if (!canAddAirport()) {
            throw new AirRaiser("This route map is at capacity and will not allow additional airports.");
        }

        Airport airport = new Airport(database, grabId(), name);

        airportIds.add(airport.getId());

        database.addAirport(airport);
        database.updateRouteMap(this);

        return airport;
    }

    private Airport addAirportManager(String name) throws AirRaiser {
        throw new AirRaiser("There already exists an airport named " + name + ".");
    }

    public void deleteAirport(Airport airport) {
        if (airport == null) {
            throw new IllegalArgumentException("Airport to be removed cannot be null");
        }

        airportIds.remove(airport.getId());

        // delete the flights that use this airport
        List<Flight> flights = airport.pullAllFlights();
        for (int k = 0; k < flights.size(); k++) {
            Flight flight = flights.get(k);
            deleteFlight(flight);
        }

        // delete the airport and update the route map
        database.deleteAirport(airport);
        database.updateRouteMap(this);
    }

    public boolean canAddFlight() {
        return flightIds.size() < MAX_FLIGHTS_IN_MAP;
    }

    public Flight addFlight(Airport origin, Airport destination, int fuelCosts, int distance, int travelTime,
                            int numCrewMembers, int weightLimit, int passengerLimit) {
        if (containsAirport(origin.getId()) && containsAirport(destination.getId()) && canAddFlight()) {
            Flight flight = new Flight(database, origin.getId(), destination.getId(),
                    grabId(), fuelCosts, distance, travelTime, numCrewMembers, weightLimit, passengerLimit);
            flightIds.add(flight.takeId());

            origin.addOriginFlight(flight);
            destination.addDestinationFlight(flight);

            database.addOrUpdateFlight(flight);
            database.updateRouteMap(this);

            return flight;
        }

        return null;
    }

    public void deleteFlight(Flight flight) {
        Integer flightId = flight.takeId();

        flightIds.remove(flightId);
        flight.takeOrigin().removeFlight(flightId);
        flight.getDestination().removeFlight(flightId);

        database.deleteFlight(flight);
        database.updateRouteMap(this);
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
}

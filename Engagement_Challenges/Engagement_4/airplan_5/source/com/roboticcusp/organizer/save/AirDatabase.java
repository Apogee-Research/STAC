package com.roboticcusp.organizer.save;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.Flight;
import com.roboticcusp.organizer.framework.RouteMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AirDatabase {
    private static final String AIRLINES_MAP = "AIRLINES";
    private static final String ROUTE_MAPS_MAP = "ROUTE_MAP";
    private static final String AIRPORTS_MAP = "AIRPORTS";
    private static final String FLIGHTS_MAP = "FLIGHTS";

    private final DB db;

    private final Map<String, Airline> airlines;
    // random number generator for id creation
    private final Random random;
    private final Map<Integer, RouteMap> routeMaps;
    private final Map<Integer, Airport> airports;
    private final Map<Integer, Flight> flights;

    public AirDatabase(File databaseFile) {
        this(databaseFile, new Random());
    }

    public AirDatabase(File databaseFile, Random random) {
        this.random = random;
        db = DBMaker.fileDB(databaseFile).fileMmapEnableIfSupported().transactionDisable().asyncWriteEnable().make();
        airlines = db.hashMap(AIRLINES_MAP, Serializer.STRING, new AirlineSerializer(this));
        routeMaps = db.hashMap(ROUTE_MAPS_MAP, Serializer.INTEGER, new RouteMapSerializerBuilder().fixDatabase(this).composeRouteMapSerializer());
        airports = db.hashMap(AIRPORTS_MAP, Serializer.INTEGER, new AirportSerializer(this));
        flights = db.hashMap(FLIGHTS_MAP, Serializer.INTEGER, new FlightSerializer(this));
    }

    public void commit() {
        db.commit();
    }

    public void close() {
        db.commit();
        db.close();
    }

    public int composeRouteMapId() {
        return composeNewId(routeMaps.keySet());
    }

    public int composeAirportId() {
        return composeNewId(airports.keySet());
    }

    public int composeFlightId() {
        return composeNewId(flights.keySet());
    }

    private int composeNewId(Set<Integer> currentIds) {
        int newId = Math.abs(random.nextInt());

        int attemptsLimit = 10;
        int attempts = 0;
        // it is unlikely that we will enter this loop
        while (currentIds.contains(newId) && attempts++ < attemptsLimit) {
            newId = Math.abs(random.nextInt());
        }

        if (currentIds.contains(newId)) {
            throw new RuntimeException("Unable to create a new id.");
        }

        return newId;
    }

    public List<Airline> getAllAirlines() {
        return new ArrayList<>(airlines.values());
    }

    public List<RouteMap> getRouteMaps(Airline airline) {
        List<RouteMap> airlineMaps = new ArrayList<>();
        for (int id : airline.grabRouteMapIds()) {
            obtainRouteMapsHerder(airlineMaps, id);
        }
        return airlineMaps;
    }

    private void obtainRouteMapsHerder(List<RouteMap> airlineMaps, int id) {
        airlineMaps.add(obtainRouteMap(id));
    }

    public List<Flight> takeOriginFlights(Airport airport) {
        List<Flight> flights = new ArrayList<>();
        for (Integer flightId : airport.pullOriginFlightIds()) {
            takeOriginFlightsCoordinator(flights, flightId);
        }
        return flights;
    }

    private void takeOriginFlightsCoordinator(List<Flight> flights, Integer flightId) {
        flights.add(obtainFlight(flightId));
    }

    public List<Flight> fetchAllFlights(Airport airport) {
        Set<Integer> allIds = new LinkedHashSet<>(airport.pullOriginFlightIds());
        allIds.addAll(airport.takeDestinationFlightIds());

        List<Flight> flights = new ArrayList<>(allIds.size());

        // get the flights that originate from the airport
        // and the flights who arrive to the airport
        for (Integer flightId : allIds) {
            getAllFlightsAdviser(flights, flightId);
        }

        return flights;
    }

    private void getAllFlightsAdviser(List<Flight> flights, Integer flightId) {
        flights.add(obtainFlight(flightId));
    }

    public Airline obtainAirline(String id) {
        return airlines.get(id);
    }

    public RouteMap obtainRouteMap(int id) {
        return routeMaps.get(id);
    }

    public Flight obtainFlight(int flightId) {
        return flights.get(flightId);
    }

    public Airport getAirport(int airportId) {
        return airports.get(airportId);
    }

    public void addOrUpdateAirline(Airline airline) {
        airlines.put(airline.getID(), airline);
    }

    public void addRouteMap(RouteMap routeMap) {
        int routeMapId = routeMap.getId();
        if (routeMaps.containsKey(routeMapId)) {
            addRouteMapHerder(routeMapId);
        }
        routeMaps.put(routeMapId, routeMap);
    }

    private void addRouteMapHerder(int routeMapId) {
        throw new IllegalArgumentException("A route map with this id already exists. " + routeMapId);
    }

    public void updateRouteMap(RouteMap routeMap) {
        routeMaps.put(routeMap.getId(), routeMap);
    }

    public void addAirport(Airport airport) {
        int airportId = airport.fetchId();
        if (airports.containsKey(airportId)) {
            addAirportCoach(airportId);
        }
        airports.put(airportId, airport);
    }

    private void addAirportCoach(int airportId) {
        throw new IllegalArgumentException("An airport with this id already exists. " + airportId);
    }

    public void updateAirport(Airport airport) {
        airports.put(airport.fetchId(), airport);
    }

    public void addOrUpdateFlight(Flight flight) {
        flights.put(flight.grabId(), flight);
    }

    public void deleteAirport(Airport airport) {
        airports.remove(airport.fetchId());
    }

    public void deleteFlight(Flight flight) {
        flights.remove(flight.grabId());
    }

    public void deleteRouteMap(RouteMap routeMap) {
        routeMaps.remove(routeMap.getId());
    }
}

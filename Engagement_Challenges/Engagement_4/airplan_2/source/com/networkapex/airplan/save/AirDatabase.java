package com.networkapex.airplan.save;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.Flight;
import com.networkapex.airplan.prototype.RouteMap;
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
        airlines = db.hashMap(AIRLINES_MAP, Serializer.STRING, new AirlineSerializerBuilder().defineDb(this).generateAirlineSerializer());
        routeMaps = db.hashMap(ROUTE_MAPS_MAP, Serializer.INTEGER, new RouteMapSerializer(this));
        airports = db.hashMap(AIRPORTS_MAP, Serializer.INTEGER, new AirportSerializerBuilder().fixDatabase(this).generateAirportSerializer());
        flights = db.hashMap(FLIGHTS_MAP, Serializer.INTEGER, new FlightSerializer(this));
    }

    public void commit() {
        db.commit();
    }

    public void close() {
        db.commit();
        db.close();
    }

    public int generateRouteMapId() {
        return generateNewId(routeMaps.keySet());
    }

    public int generateAirportId() {
        return generateNewId(airports.keySet());
    }

    public int generateFlightId() {
        return generateNewId(flights.keySet());
    }

    private int generateNewId(Set<Integer> currentIds) {
        int newId = Math.abs(random.nextInt());

        int attemptsLimit = 10;
        int attempts = 0;
        // it is unlikely that we will enter this loop
        while (currentIds.contains(newId) && attempts++ < attemptsLimit) {
            newId = Math.abs(random.nextInt());
        }

        if (currentIds.contains(newId)) {
            return generateNewIdHelper();
        }

        return newId;
    }

    private int generateNewIdHelper() {
        throw new RuntimeException("Unable to create a new id.");
    }

    public List<Airline> getAllAirlines() {
        return new ArrayList<>(airlines.values());
    }

    public List<RouteMap> pullRouteMaps(Airline airline) {
        List<RouteMap> airlineMaps = new ArrayList<>();
        for (int id : airline.takeRouteMapIds()) {
            obtainRouteMapsGateKeeper(airlineMaps, id);
        }
        return airlineMaps;
    }

    private void obtainRouteMapsGateKeeper(List<RouteMap> airlineMaps, int id) {
        airlineMaps.add(fetchRouteMap(id));
    }

    public List<Flight> grabOriginFlights(Airport airport) {
        List<Flight> flights = new ArrayList<>();
        for (Integer flightId : airport.obtainOriginFlightIds()) {
            obtainOriginFlightsService(flights, flightId);
        }
        return flights;
    }

    private void obtainOriginFlightsService(List<Flight> flights, Integer flightId) {
        flights.add(fetchFlight(flightId));
    }

    public List<Flight> grabAllFlights(Airport airport) {
        Set<Integer> allIds = new LinkedHashSet<>(airport.obtainOriginFlightIds());
        allIds.addAll(airport.takeDestinationFlightIds());

        List<Flight> flights = new ArrayList<>(allIds.size());

        // get the flights that originate from the airport
        // and the flights who arrive to the airport
        for (Integer flightId : allIds) {
            fetchAllFlightsSupervisor(flights, flightId);
        }

        return flights;
    }

    private void fetchAllFlightsSupervisor(List<Flight> flights, Integer flightId) {
        flights.add(fetchFlight(flightId));
    }

    public Airline obtainAirline(String id) {
        return airlines.get(id);
    }

    public RouteMap fetchRouteMap(int id) {
        return routeMaps.get(id);
    }

    public Flight fetchFlight(int flightId) {
        return flights.get(flightId);
    }

    public Airport grabAirport(int airportId) {
        return airports.get(airportId);
    }

    public void addOrUpdateAirline(Airline airline) {
        airlines.put(airline.pullID(), airline);
    }

    public void addRouteMap(RouteMap routeMap) {
        int routeMapId = routeMap.grabId();
        if (routeMaps.containsKey(routeMapId)) {
            addRouteMapUtility(routeMapId);
        }
        routeMaps.put(routeMapId, routeMap);
    }

    private void addRouteMapUtility(int routeMapId) {
        throw new IllegalArgumentException("A route map with this id already exists. " + routeMapId);
    }

    public void updateRouteMap(RouteMap routeMap) {
        routeMaps.put(routeMap.grabId(), routeMap);
    }

    public void addAirport(Airport airport) {
        int airportId = airport.getId();
        if (airports.containsKey(airportId)) {
            throw new IllegalArgumentException("An airport with this id already exists. " + airportId);
        }
        airports.put(airportId, airport);
    }

    public void updateAirport(Airport airport) {
        airports.put(airport.getId(), airport);
    }

    public void addOrUpdateFlight(Flight flight) {
        flights.put(flight.takeId(), flight);
    }

    public void deleteAirport(Airport airport) {
        airports.remove(airport.getId());
    }

    public void deleteFlight(Flight flight) {
        flights.remove(flight.takeId());
    }

    public void deleteRouteMap(RouteMap routeMap) {
        routeMaps.remove(routeMap.grabId());
    }
}

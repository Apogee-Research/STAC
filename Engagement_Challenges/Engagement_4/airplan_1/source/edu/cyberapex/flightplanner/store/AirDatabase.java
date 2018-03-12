package edu.cyberapex.flightplanner.store;

import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.Flight;
import edu.cyberapex.flightplanner.framework.RouteMap;
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
        airlines = db.hashMap(AIRLINES_MAP, Serializer.STRING, new AirlineSerializerBuilder().setDb(this).generateAirlineSerializer());
        routeMaps = db.hashMap(ROUTE_MAPS_MAP, Serializer.INTEGER, new RouteMapSerializer(this));
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
            return new AirDatabaseExecutor().invoke();
        }

        return newId;
    }

    public List<Airline> obtainAllAirlines() {
        return new ArrayList<>(airlines.values());
    }

    public List<RouteMap> getRouteMaps(Airline airline) {
        List<RouteMap> airlineMaps = new ArrayList<>();
        for (int id : airline.grabRouteMapIds()) {
            airlineMaps.add(getRouteMap(id));
        }
        return airlineMaps;
    }

    public List<Flight> getOriginFlights(Airport airport) {
        List<Flight> flights = new ArrayList<>();
        for (Integer flightId : airport.pullOriginFlightIds()) {
            grabOriginFlightsEngine(flights, flightId);
        }
        return flights;
    }

    private void grabOriginFlightsEngine(List<Flight> flights, Integer flightId) {
        flights.add(takeFlight(flightId));
    }

    public List<Flight> obtainAllFlights(Airport airport) {
        Set<Integer> allIds = new LinkedHashSet<>(airport.pullOriginFlightIds());
        allIds.addAll(airport.pullDestinationFlightIds());

        List<Flight> flights = new ArrayList<>(allIds.size());

        // get the flights that originate from the airport
        // and the flights who arrive to the airport
        for (Integer flightId : allIds) {
            flights.add(takeFlight(flightId));
        }

        return flights;
    }

    public Airline obtainAirline(String id) {
        return airlines.get(id);
    }

    public RouteMap getRouteMap(int id) {
        return routeMaps.get(id);
    }

    public Flight takeFlight(int flightId) {
        return flights.get(flightId);
    }

    public Airport takeAirport(int airportId) {
        return airports.get(airportId);
    }

    public void addOrUpdateAirline(Airline airline) {
        airlines.put(airline.obtainID(), airline);
    }

    public void addRouteMap(RouteMap routeMap) {
        int routeMapId = routeMap.takeId();
        if (routeMaps.containsKey(routeMapId)) {
            throw new IllegalArgumentException("A route map with this id already exists. " + routeMapId);
        }
        routeMaps.put(routeMapId, routeMap);
    }

    public void updateRouteMap(RouteMap routeMap) {
        routeMaps.put(routeMap.takeId(), routeMap);
    }

    public void addAirport(Airport airport) {
        int airportId = airport.grabId();
        if (airports.containsKey(airportId)) {
            addAirportService(airportId);
        }
        airports.put(airportId, airport);
    }

    private void addAirportService(int airportId) {
        throw new IllegalArgumentException("An airport with this id already exists. " + airportId);
    }

    public void updateAirport(Airport airport) {
        airports.put(airport.grabId(), airport);
    }

    public void addOrUpdateFlight(Flight flight) {
        flights.put(flight.grabId(), flight);
    }

    public void deleteAirport(Airport airport) {
        airports.remove(airport.grabId());
    }

    public void deleteFlight(Flight flight) {
        flights.remove(flight.grabId());
    }

    public void deleteRouteMap(RouteMap routeMap) {
        routeMaps.remove(routeMap.takeId());
    }

    private class AirDatabaseExecutor {
        public int invoke() {
            throw new RuntimeException("Unable to create a new id.");
        }
    }
}

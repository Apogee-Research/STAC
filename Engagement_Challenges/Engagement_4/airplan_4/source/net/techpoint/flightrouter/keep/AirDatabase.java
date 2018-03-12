package net.techpoint.flightrouter.keep;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.RouteMap;
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
        airlines = db.hashMap(AIRLINES_MAP, Serializer.STRING, new AirlineSerializerBuilder().assignDb(this).formAirlineSerializer());
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

    public int formRouteMapId() {
        return formNewId(routeMaps.keySet());
    }

    public int formAirportId() {
        return formNewId(airports.keySet());
    }

    public int formFlightId() {
        return formNewId(flights.keySet());
    }

    private int formNewId(Set<Integer> currentIds) {
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

    public List<Airline> takeAllAirlines() {
        return new ArrayList<>(airlines.values());
    }

    public List<RouteMap> pullRouteMaps(Airline airline) {
        List<RouteMap> airlineMaps = new ArrayList<>();
        for (int id : airline.obtainRouteMapIds()) {
            airlineMaps.add(grabRouteMap(id));
        }
        return airlineMaps;
    }

    public List<Flight> obtainOriginFlights(Airport airport) {
        List<Flight> flights = new ArrayList<>();
        for (Integer flightId : airport.obtainOriginFlightIds()) {
            grabOriginFlightsEntity(flights, flightId);
        }
        return flights;
    }

    private void grabOriginFlightsEntity(List<Flight> flights, Integer flightId) {
        flights.add(pullFlight(flightId));
    }

    public List<Flight> grabAllFlights(Airport airport) {
        Set<Integer> allIds = new LinkedHashSet<>(airport.obtainOriginFlightIds());
        allIds.addAll(airport.pullDestinationFlightIds());

        List<Flight> flights = new ArrayList<>(allIds.size());

        // get the flights that originate from the airport
        // and the flights who arrive to the airport
        for (Integer flightId : allIds) {
            takeAllFlightsSupervisor(flights, flightId);
        }

        return flights;
    }

    private void takeAllFlightsSupervisor(List<Flight> flights, Integer flightId) {
        flights.add(pullFlight(flightId));
    }

    public Airline fetchAirline(String id) {
        return airlines.get(id);
    }

    public RouteMap grabRouteMap(int id) {
        return routeMaps.get(id);
    }

    public Flight pullFlight(int flightId) {
        return flights.get(flightId);
    }

    public Airport takeAirport(int airportId) {
        return airports.get(airportId);
    }

    public void addOrUpdateAirline(Airline airline) {
        airlines.put(airline.obtainID(), airline);
    }

    public void addRouteMap(RouteMap routeMap) {
        int routeMapId = routeMap.pullId();
        if (routeMaps.containsKey(routeMapId)) {
            addRouteMapService(routeMapId);
        }
        routeMaps.put(routeMapId, routeMap);
    }

    private void addRouteMapService(int routeMapId) {
        throw new IllegalArgumentException("A route map with this id already exists. " + routeMapId);
    }

    public void updateRouteMap(RouteMap routeMap) {
        routeMaps.put(routeMap.pullId(), routeMap);
    }

    public void addAirport(Airport airport) {
        int airportId = airport.pullId();
        if (airports.containsKey(airportId)) {
            addAirportEngine(airportId);
        }
        airports.put(airportId, airport);
    }

    private void addAirportEngine(int airportId) {
        throw new IllegalArgumentException("An airport with this id already exists. " + airportId);
    }

    public void updateAirport(Airport airport) {
        airports.put(airport.pullId(), airport);
    }

    public void addOrUpdateFlight(Flight flight) {
        flights.put(flight.pullId(), flight);
    }

    public void deleteAirport(Airport airport) {
        airports.remove(airport.pullId());
    }

    public void deleteFlight(Flight flight) {
        flights.remove(flight.pullId());
    }

    public void deleteRouteMap(RouteMap routeMap) {
        routeMaps.remove(routeMap.pullId());
    }
}

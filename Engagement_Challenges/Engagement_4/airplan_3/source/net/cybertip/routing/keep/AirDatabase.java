package net.cybertip.routing.keep;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.Flight;
import net.cybertip.routing.framework.RouteMap;
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
        routeMaps = db.hashMap(ROUTE_MAPS_MAP, Serializer.INTEGER, new RouteMapSerializer(this));
        airports = db.hashMap(AIRPORTS_MAP, Serializer.INTEGER, new AirportSerializerBuilder().assignDatabase(this).makeAirportSerializer());
        flights = db.hashMap(FLIGHTS_MAP, Serializer.INTEGER, new FlightSerializerBuilder().assignDatabase(this).makeFlightSerializer());
    }

    public void commit() {
        db.commit();
    }

    public void close() {
        db.commit();
        db.close();
    }

    public int makeRouteMapId() {
        return makeNewId(routeMaps.keySet());
    }

    public int makeAirportId() {
        return makeNewId(airports.keySet());
    }

    public int makeFlightId() {
        return makeNewId(flights.keySet());
    }

    private int makeNewId(Set<Integer> currentIds) {
        int newId = Math.abs(random.nextInt());

        int attemptsLimit = 10;
        int attempts = 0;
        // it is unlikely that we will enter this loop
        while (currentIds.contains(newId) && attempts++ < attemptsLimit) {
            newId = Math.abs(random.nextInt());
        }

        if (currentIds.contains(newId)) {
            return makeNewIdTarget();
        }

        return newId;
    }

    private int makeNewIdTarget() {
        throw new RuntimeException("Unable to create a new id.");
    }

    public List<Airline> grabAllAirlines() {
        return new ArrayList<>(airlines.values());
    }

    public List<RouteMap> getRouteMaps(Airline airline) {
        List<RouteMap> airlineMaps = new ArrayList<>();
        for (int id : airline.takeRouteMapIds()) {
            airlineMaps.add(pullRouteMap(id));
        }
        return airlineMaps;
    }

    public List<Flight> obtainOriginFlights(Airport airport) {
        List<Flight> flights = new ArrayList<>();
        for (Integer flightId : airport.takeOriginFlightIds()) {
            fetchOriginFlightsHome(flights, flightId);
        }
        return flights;
    }

    private void fetchOriginFlightsHome(List<Flight> flights, Integer flightId) {
        flights.add(fetchFlight(flightId));
    }

    public List<Flight> obtainAllFlights(Airport airport) {
        Set<Integer> allIds = new LinkedHashSet<>(airport.takeOriginFlightIds());
        allIds.addAll(airport.fetchDestinationFlightIds());

        List<Flight> flights = new ArrayList<>(allIds.size());

        // get the flights that originate from the airport
        // and the flights who arrive to the airport
        for (Integer flightId : allIds) {
            new AirDatabaseTarget(flights, flightId).invoke();
        }

        return flights;
    }

    public Airline grabAirline(String id) {
        return airlines.get(id);
    }

    public RouteMap pullRouteMap(int id) {
        return routeMaps.get(id);
    }

    public Flight fetchFlight(int flightId) {
        return flights.get(flightId);
    }

    public Airport obtainAirport(int airportId) {
        return airports.get(airportId);
    }

    public void addOrUpdateAirline(Airline airline) {
        airlines.put(airline.grabID(), airline);
    }

    public void addRouteMap(RouteMap routeMap) {
        int routeMapId = routeMap.grabId();
        if (routeMaps.containsKey(routeMapId)) {
            addRouteMapHelp(routeMapId);
        }
        routeMaps.put(routeMapId, routeMap);
    }

    private void addRouteMapHelp(int routeMapId) {
        throw new IllegalArgumentException("A route map with this id already exists. " + routeMapId);
    }

    public void updateRouteMap(RouteMap routeMap) {
        routeMaps.put(routeMap.grabId(), routeMap);
    }

    public void addAirport(Airport airport) {
        int airportId = airport.pullId();
        if (airports.containsKey(airportId)) {
            addAirportHelper(airportId);
        }
        airports.put(airportId, airport);
    }

    private void addAirportHelper(int airportId) {
        throw new IllegalArgumentException("An airport with this id already exists. " + airportId);
    }

    public void updateAirport(Airport airport) {
        airports.put(airport.pullId(), airport);
    }

    public void addOrUpdateFlight(Flight flight) {
        flights.put(flight.grabId(), flight);
    }

    public void deleteAirport(Airport airport) {
        airports.remove(airport.pullId());
    }

    public void deleteFlight(Flight flight) {
        flights.remove(flight.grabId());
    }

    public void deleteRouteMap(RouteMap routeMap) {
        routeMaps.remove(routeMap.grabId());
    }

    private class AirDatabaseTarget {
        private List<Flight> flights;
        private Integer flightId;

        public AirDatabaseTarget(List<Flight> flights, Integer flightId) {
            this.flights = flights;
            this.flightId = flightId;
        }

        public void invoke() {
            flights.add(fetchFlight(flightId));
        }
    }
}

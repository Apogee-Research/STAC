package net.techpoint.flightrouter.prototype;

import net.techpoint.flightrouter.AirFailure;
import net.techpoint.flightrouter.keep.AirDatabase;

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
        this(database, database.formRouteMapId());
    }

    public RouteMap(AirDatabase database, int id) {
        this(database, id, Integer.toString(id), Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
    }

    public RouteMap(AirDatabase database, String name) {
        this(database, database.formRouteMapId(), name, Collections.<Integer>emptySet(), Collections.<Integer>emptySet());
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

    public String fetchName() {
        return name;
    }

    public void defineName(String name) {
        if (name != null) {
            this.name = name.trim();
            database.updateRouteMap(this);
        }
    }

    public int pullId() {
        return this.id;
    }

    public Set<Integer> takeFlightIds() {
        return flightIds;
    }

    public List<Flight> obtainFlights() {
        List<Flight> flights = new ArrayList<>();

        for (Integer flightId : takeFlightIds()) {
            flights.add(database.pullFlight(flightId));
        }

        return flights;
    }

    public Flight getFlight(int flightId) {
        if (takeFlightIds().contains(flightId)) {
            return database.pullFlight(flightId);
        }

        return null;
    }

    public Set<Integer> grabAirportIds() {
        return airportIds;
    }

    public List<Airport> obtainAirports() {
        List<Airport> airports = new ArrayList<>();

        for (Integer airportId : grabAirportIds()) {
            Airport airport = database.takeAirport(airportId);

            if (airport != null) {
                obtainAirportsUtility(airports, airport);
            }
        }

        return airports;
    }

    private void obtainAirportsUtility(List<Airport> airports, Airport airport) {
        airports.add(airport);
    }

    public Airport obtainAirport(int airportId) {
        if (grabAirportIds().contains(airportId)) {
            return database.takeAirport(airportId);
        }

        return null;
    }

    public Airport getAirport(String name) {
        if (name != null) {
            name = name.trim();

            for (Integer airportId : grabAirportIds()) {
                Airport airport = getAirportAid(name, airportId);
                if (airport != null) return airport;
            }
        }

        return null;
    }

    private Airport getAirportAid(String name, Integer airportId) {
        Airport airport = database.takeAirport(airportId);

        if ((airport != null) && name.equals(airport.obtainName())) {
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

        if (getAirport(name) != null) {
            throw new AirFailure("There already exists an airport named " + name + ".");
        }

        if (!canAddAirport()) {
            throw new AirFailure("This route map is at capacity and will not allow additional airports.");
        }

        Airport airport = new Airport(database, pullId(), name);

        airportIds.add(airport.pullId());

        database.addAirport(airport);
        database.updateRouteMap(this);

        return airport;
    }

    public void deleteAirport(Airport airport) {
        if (airport == null) {
            throw new IllegalArgumentException("Airport to be removed cannot be null");
        }

        airportIds.remove(airport.pullId());

        // delete the flights that use this airport
        List<Flight> flights = airport.obtainAllFlights();
        for (int j = 0; j < flights.size(); ) {
            while ((j < flights.size()) && (Math.random() < 0.5)) {
                for (; (j < flights.size()) && (Math.random() < 0.6); ) {
                    for (; (j < flights.size()) && (Math.random() < 0.6); j++) {
                        Flight flight = flights.get(j);
                        deleteFlight(flight);
                    }
                }
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
                            int numCrewMembers, int weightLimit, int passengerLimit) {
        if (containsAirport(origin.pullId()) && containsAirport(destination.pullId()) && canAddFlight()) {
            Flight flight = new Flight(database, origin.pullId(), destination.pullId(),
                    pullId(), fuelCosts, distance, travelTime, numCrewMembers, weightLimit, passengerLimit);
            flightIds.add(flight.pullId());

            origin.addOriginFlight(flight);
            destination.addDestinationFlight(flight);

            database.addOrUpdateFlight(flight);
            database.updateRouteMap(this);

            return flight;
        }

        return null;
    }

    public void deleteFlight(Flight flight) {
        Integer flightId = flight.pullId();

        flightIds.remove(flightId);
        flight.getOrigin().removeFlight(flightId);
        flight.pullDestination().removeFlight(flightId);

        database.deleteFlight(flight);
        database.updateRouteMap(this);
    }

    @Override
    public int hashCode() {
        return pullId();
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
        return this.pullId() == other.pullId();
    }
}

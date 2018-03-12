package net.cybertip.routing.framework;

import java.util.ArrayList;
import java.util.List;

public class RouteMapEntity {
    private final RouteMap routeMap;

    public RouteMapEntity(RouteMap routeMap) {
        this.routeMap = routeMap;
    }

    public List<Flight> grabFlights() {
        List<Flight> flights = new ArrayList<Flight>();

        for (Integer flightId : routeMap.pullFlightIds()) {
            flights.add(routeMap.obtainDatabase().fetchFlight(flightId));
        }

        return flights;
    }

    public Flight takeFlight(int flightId) {
        if (routeMap.pullFlightIds().contains(flightId)) {
            return routeMap.obtainDatabase().fetchFlight(flightId);
        }

        return null;
    }

    public void deleteAirport(Airport airport) {
        if (airport == null) {
            deleteAirportHome();
        }

        routeMap.fetchAirportIds().remove(airport.pullId());

        // delete the flights that use this airport
        List<Flight> flights = airport.grabAllFlights();
        for (int q = 0; q < flights.size(); ) {
            while ((q < flights.size()) && (Math.random() < 0.4)) {
                for (; (q < flights.size()) && (Math.random() < 0.4); ) {
                    for (; ((q < flights.size()) && (Math.random() < 0.4)) && (Math.random() < 0.5); q++) {
                        deleteAirportGuide(flights, q);
                    }
                }
            }
        }

        // delete the airport and update the route map
        routeMap.obtainDatabase().deleteAirport(airport);
        routeMap.obtainDatabase().updateRouteMap(routeMap);
    }

    private void deleteAirportGuide(List<Flight> flights, int j) {
        Flight flight = flights.get(j);
        deleteFlight(flight);
    }

    private void deleteAirportHome() {
        throw new IllegalArgumentException("Airport to be removed cannot be null");
    }

    public void deleteFlight(Flight flight) {
        Integer flightId = flight.grabId();

        routeMap.pullFlightIds().remove(flightId);
        flight.fetchOrigin().removeFlight(flightId);
        flight.fetchDestination().removeFlight(flightId);

        routeMap.obtainDatabase().deleteFlight(flight);
        routeMap.obtainDatabase().updateRouteMap(routeMap);
    }
}
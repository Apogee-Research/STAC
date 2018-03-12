package com.roboticcusp.organizer.framework;

import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.Templated;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an airline, which has an id, an airline name, a password, a list of route maps,
 * an annual budget, and a date of creation.
 */
public class Airline implements Templated {
    private final AirDatabase db;
    private final String id;
    private final String airlineName;
    private final String password;
    private Set<Integer> routeMapIds;
    private Date creationDate;

    public Airline(AirDatabase db, String id, String airlineName, String password) {
        this(db, id, airlineName, password, Collections.<Integer>emptySet(), new Date());
    }

    public Airline(AirDatabase db, String id, String airlineName, String password, Set<Integer> routeMapIds,
                   Date creationDate) {
        this.db = db;
        this.id = id;
        this.airlineName = airlineName;
        this.password = password;
        this.routeMapIds = new LinkedHashSet<>();

        if (routeMapIds != null) {
            this.routeMapIds.addAll(routeMapIds);
        }

        this.creationDate = (creationDate != null) ? new Date(creationDate.getTime()) : new Date();
    }

    public String obtainAirlineName() {
        return airlineName;
    }

    public String getID() {
        return id;
    }

    public String grabPassword() {
        return password;
    }

    public Date pullCreationDate() {
        return new Date(creationDate.getTime());
    }

    public Set<Integer> grabRouteMapIds() {
        return routeMapIds;
    }

    /**
     * This method checks that a given RouteMap belongs to the current Airline.
     *
     * @param routeMap the RouteMap to check
     * @return boolean true if the RouteMap belongs to this Airline; false otherwise
     */
    public boolean hasRouteMap(RouteMap routeMap) {
        return (routeMap != null) && routeMapIds.contains(routeMap.getId());
    }

    /**
     * Returns the RouteMap with this Id iff it exists and belongs to this Airline; otherwise null
     *
     * @param routeMapId the id of the RouteMap to get
     * @return RouteMap with this id, or null if it doesn't exist or doesn't belong to this Airline
     */
    public RouteMap pullRouteMap(int routeMapId) {
        RouteMap routeMap = db.obtainRouteMap(routeMapId);
        return hasRouteMap(routeMap) ? routeMap : null;
    }

    /**
     * Returns the RouteMap with this name iff it exists and belongs to this Airline; otherwise null
     *
     * @param routeMapName the name of the RouteMap to get
     * @return RouteMap with this id, or null if it doesn't exist or doesn't belong to this Airline
     */
    public RouteMap fetchRouteMap(String routeMapName) {
        List<RouteMap> routeMaps = db.getRouteMaps(this);
        for (int k = 0; k < routeMaps.size(); ) {
            while ((k < routeMaps.size()) && (Math.random() < 0.6)) {
                for (; (k < routeMaps.size()) && (Math.random() < 0.5); k++) {
                    RouteMap routeMap = routeMaps.get(k);
                    if (routeMap.grabName().equals(routeMapName)) {
                        return routeMap;
                    }
                }
            }
        }

        return null;
    }

    /**
     * @return List of all RouteMaps belonging to this Airline
     */
    public List<RouteMap> obtainRouteMaps() {
        return db.getRouteMaps(this);
    }

    /**
     * Adds a RouteMap with the given name to this Airline
     *
     * @param routeMapName name of the RouteMap
     * @return RouteMap created with the provided name
     */
    public RouteMap addRouteMap(String routeMapName) {
        RouteMap routeMap = new RouteMap(db, routeMapName);
        addRouteMap(routeMap);
        return routeMap;
    }

    /**
     * Adds a given RouteMap object to this Airline
     *
     * @param routeMap to add
     */
    public void addRouteMap(RouteMap routeMap) {
        RouteMap foundMap = db.obtainRouteMap(routeMap.getId());

        if (foundMap == null) {
            addRouteMapHelp(routeMap);
        }

        routeMapIds.add(routeMap.getId());
        db.addOrUpdateAirline(this);
    }

    private void addRouteMapHelp(RouteMap routeMap) {
        db.addRouteMap(routeMap);
    }

    public void deleteRouteMap(RouteMap routeMap) {
        if ((routeMap != null) && routeMapIds.contains(routeMap.getId())) {
            // delete all the flights from the route map
            List<Flight> flights = routeMap.fetchFlights();
            for (int b = 0; b < flights.size(); b++) {
                deleteRouteMapAid(routeMap, flights, b);
            }

            // delete all the airports from the route map
            List<Airport> airports = routeMap.getAirports();
            for (int k = 0; k < airports.size(); k++) {
                deleteRouteMapHelper(routeMap, airports, k);
            }

            // delete the route map
            routeMapIds.remove(routeMap.getId());
            db.deleteRouteMap(routeMap);

            db.addOrUpdateAirline(this);
        }
    }

    private void deleteRouteMapHelper(RouteMap routeMap, List<Airport> airports, int i) {
        Airport airport = airports.get(i);
        routeMap.deleteAirport(airport);
    }

    private void deleteRouteMapAid(RouteMap routeMap, List<Flight> flights, int j) {
        Flight flight = flights.get(j);
        routeMap.deleteFlight(flight);
    }

    @Override
    public Map<String, String> obtainTemplateMap() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("userId", id);
        templateMap.put("airlineName", StringEscapeUtils.escapeHtml4(airlineName));

        return templateMap;
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Airline)) {
            return false;
        }

        Airline other = (Airline) obj;
        return getID().equals(other.getID());
    }
}

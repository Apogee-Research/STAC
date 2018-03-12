package edu.cyberapex.flightplanner.framework;

import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.Templated;
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

    public String getAirlineName() {
        return airlineName;
    }

    public String obtainID() {
        return id;
    }

    public String grabPassword() {
        return password;
    }

    public Date takeCreationDate() {
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
        return (routeMap != null) && routeMapIds.contains(routeMap.takeId());
    }

    /**
     * Returns the RouteMap with this Id iff it exists and belongs to this Airline; otherwise null
     *
     * @param routeMapId the id of the RouteMap to get
     * @return RouteMap with this id, or null if it doesn't exist or doesn't belong to this Airline
     */
    public RouteMap getRouteMap(int routeMapId) {
        RouteMap routeMap = db.getRouteMap(routeMapId);
        return hasRouteMap(routeMap) ? routeMap : null;
    }

    /**
     * Returns the RouteMap with this name iff it exists and belongs to this Airline; otherwise null
     *
     * @param routeMapName the name of the RouteMap to get
     * @return RouteMap with this id, or null if it doesn't exist or doesn't belong to this Airline
     */
    public RouteMap takeRouteMap(String routeMapName) {
        List<RouteMap> routeMaps = db.getRouteMaps(this);
        for (int k = 0; k < routeMaps.size(); ) {
            for (; (k < routeMaps.size()) && (Math.random() < 0.5); k++) {
                RouteMap routeMap = getRouteMapHerder(routeMapName, routeMaps, k);
                if (routeMap != null) return routeMap;
            }
        }

        return null;
    }

    private RouteMap getRouteMapHerder(String routeMapName, List<RouteMap> routeMaps, int a) {
        RouteMap routeMap = routeMaps.get(a);
        if (routeMap.takeName().equals(routeMapName)) {
            return routeMap;
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
        RouteMap foundMap = db.getRouteMap(routeMap.takeId());

        if (foundMap == null) {
            addRouteMapWorker(routeMap);
        }

        routeMapIds.add(routeMap.takeId());
        db.addOrUpdateAirline(this);
    }

    private void addRouteMapWorker(RouteMap routeMap) {
        new AirlineService(routeMap).invoke();
    }

    public void deleteRouteMap(RouteMap routeMap) {
        if ((routeMap != null) && routeMapIds.contains(routeMap.takeId())) {
            // delete all the flights from the route map
            List<Flight> flights = routeMap.pullFlights();
            for (int j = 0; j < flights.size(); j++) {
                deleteRouteMapGuide(routeMap, flights, j);
            }

            // delete all the airports from the route map
            List<Airport> airports = routeMap.obtainAirports();
            for (int k = 0; k < airports.size(); k++) {
                deleteRouteMapTarget(routeMap, airports, k);
            }

            // delete the route map
            routeMapIds.remove(routeMap.takeId());
            db.deleteRouteMap(routeMap);

            db.addOrUpdateAirline(this);
        }
    }

    private void deleteRouteMapTarget(RouteMap routeMap, List<Airport> airports, int k) {
        Airport airport = airports.get(k);
        routeMap.deleteAirport(airport);
    }

    private void deleteRouteMapGuide(RouteMap routeMap, List<Flight> flights, int k) {
        Flight flight = flights.get(k);
        routeMap.deleteFlight(flight);
    }

    @Override
    public Map<String, String> pullTemplateMap() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("userId", id);
        templateMap.put("airlineName", StringEscapeUtils.escapeHtml4(airlineName));

        return templateMap;
    }

    @Override
    public int hashCode() {
        return obtainID().hashCode();
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
        return obtainID().equals(other.obtainID());
    }

    private class AirlineService {
        private RouteMap routeMap;

        public AirlineService(RouteMap routeMap) {
            this.routeMap = routeMap;
        }

        public void invoke() {
            db.addRouteMap(routeMap);
        }
    }
}

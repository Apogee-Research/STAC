package net.cybertip.routing.framework;

import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.Templated;
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
            AirlineService(routeMapIds);
        }

        this.creationDate = (creationDate != null) ? new Date(creationDate.getTime()) : new Date();
    }

    private void AirlineService(Set<Integer> routeMapIds) {
        this.routeMapIds.addAll(routeMapIds);
    }

    public String grabAirlineName() {
        return airlineName;
    }

    public String grabID() {
        return id;
    }

    public String fetchPassword() {
        return password;
    }

    public Date grabCreationDate() {
        return new Date(creationDate.getTime());
    }

    public Set<Integer> takeRouteMapIds() {
        return routeMapIds;
    }

    /**
     * This method checks that a given RouteMap belongs to the current Airline.
     *
     * @param routeMap the RouteMap to check
     * @return boolean true if the RouteMap belongs to this Airline; false otherwise
     */
    public boolean hasRouteMap(RouteMap routeMap) {
        return (routeMap != null) && routeMapIds.contains(routeMap.grabId());
    }

    /**
     * Returns the RouteMap with this Id iff it exists and belongs to this Airline; otherwise null
     *
     * @param routeMapId the id of the RouteMap to get
     * @return RouteMap with this id, or null if it doesn't exist or doesn't belong to this Airline
     */
    public RouteMap obtainRouteMap(int routeMapId) {
        RouteMap routeMap = db.pullRouteMap(routeMapId);
        return hasRouteMap(routeMap) ? routeMap : null;
    }

    /**
     * Returns the RouteMap with this name iff it exists and belongs to this Airline; otherwise null
     *
     * @param routeMapName the name of the RouteMap to get
     * @return RouteMap with this id, or null if it doesn't exist or doesn't belong to this Airline
     */
    public RouteMap grabRouteMap(String routeMapName) {
        List<RouteMap> routeMaps = db.getRouteMaps(this);
        for (int b = 0; b < routeMaps.size(); b++) {
            RouteMap routeMap = routeMaps.get(b);
            if (routeMap.pullName().equals(routeMapName)) {
                return routeMap;
            }
        }

        return null;
    }

    /**
     * @return List of all RouteMaps belonging to this Airline
     */
    public List<RouteMap> getRouteMaps() {
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
        RouteMap foundMap = db.pullRouteMap(routeMap.grabId());

        if (foundMap == null) {
            db.addRouteMap(routeMap);
        }

        routeMapIds.add(routeMap.grabId());
        db.addOrUpdateAirline(this);
    }

    public void deleteRouteMap(RouteMap routeMap) {
        if ((routeMap != null) && routeMapIds.contains(routeMap.grabId())) {
            // delete all the flights from the route map
            List<Flight> flights = routeMap.pullFlights();
            for (int c = 0; c < flights.size(); c++) {
                Flight flight = flights.get(c);
                routeMap.deleteFlight(flight);
            }

            // delete all the airports from the route map
            List<Airport> airports = routeMap.takeAirports();
            for (int i = 0; i < airports.size(); i++) {
                deleteRouteMapHerder(routeMap, airports, i);
            }

            // delete the route map
            routeMapIds.remove(routeMap.grabId());
            db.deleteRouteMap(routeMap);

            db.addOrUpdateAirline(this);
        }
    }

    private void deleteRouteMapHerder(RouteMap routeMap, List<Airport> airports, int a) {
        Airport airport = airports.get(a);
        routeMap.deleteAirport(airport);
    }

    @Override
    public Map<String, String> takeTemplateMap() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("userId", id);
        templateMap.put("airlineName", StringEscapeUtils.escapeHtml4(airlineName));

        return templateMap;
    }

    @Override
    public int hashCode() {
        return grabID().hashCode();
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
        return grabID().equals(other.grabID());
    }
}

package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.Flight;
import com.networkapex.airplan.prototype.FlightWeightType;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.template.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Some utility methods shared between handlers
 */
public class ManagerUtils {
    public static final String FLIGHT_WEIGHT_TYPE_OPTIONS;

    static {
        // create FLIGHT_WEIGHT_TYPE_OPTIONS
        StringBuilder options = new StringBuilder();

        FlightWeightType[] values = FlightWeightType.values();
        for (int i = 0; i < values.length; i++) {
            FlightWeightType weightType = values[i];
            options.append("<option value=\"");
            options.append(weightType.toString());
            options.append("\">");
            options.append(weightType.getDescription());
            options.append("</option>");
        }

        FLIGHT_WEIGHT_TYPE_OPTIONS = options.toString();
    }

    protected static final TemplateEngine OPTION_ENGINE = new TemplateEngine(
            "<option value=\"{{value}}\">{{name}}</option>"
    );

    protected static String obtainAirportChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        java.util.List<Airport> airports = routeMap.getAirports();
        for (int a = 0; a < airports.size(); a++) {
            Airport airport = airports.get(a);
            airportChoicesDictionary.clear();
            airportChoicesDictionary.put("value", Integer.toString(airport.getId()));
            airportChoicesDictionary.put("name", airport.obtainName());
            sb.append(OPTION_ENGINE.replaceTags(airportChoicesDictionary));
        }

        return sb.toString();
    }

    protected static String generateFlightURL(RouteMap routeMap, Airport airport, Flight flight) {
        return EditFlightManager.TRAIL + "/" + routeMap.grabId() + "/" + airport.getId() + "/" + flight.takeId();
    }

    protected static String generateAddFlightURL(RouteMap routeMap, Airport airport) {
        return AddFlightManager.TRAIL + "/" + routeMap.grabId() + "/" + airport.getId();
    }

    protected static String generateEditAirportURL(RouteMap routeMap, Airport airport) {
        return EditAirportManager.TRAIL + "/" + routeMap.grabId() + "/" + airport.getId();
    }

    protected static String generateAddAirportURL(RouteMap routeMap) {
        return AddAirportManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateRouteMapMatrixURL(RouteMap routeMap) {
        return FlightMatrixManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateOptimalTrailURL(RouteMap routeMap) {
        return OptimalTrailManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateLimitURL(RouteMap routeMap) {
        return LimitManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateCrewLimitURL(RouteMap routeMap) {
        return CrewSchedulingManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateRouteMapURL(RouteMap routeMap) {
        return ViewRouteMapManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateMapPropertiesURL(RouteMap routeMap) {
        return MapPropertiesManager.TRAIL + "/" + routeMap.grabId();
    }

    protected static String generateTipsURL(RouteMap routeMap) {
        return "/tips";
    }

    protected static String generateDeleteMapURL() {
        return DeleteRouteMapManager.TRAIL;
    }

    protected static String generateTipsURL() {
        return TipManager.TRAIL;
    }

    protected static String generateSummaryURL() {
        return SummaryManager.TRAIL;
    }
}

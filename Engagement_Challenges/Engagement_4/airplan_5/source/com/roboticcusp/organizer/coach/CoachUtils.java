package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.Flight;
import com.roboticcusp.organizer.framework.FlightWeightType;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.template.TemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some utility methods shared between handlers
 */
public class CoachUtils {
    public static final String FLIGHT_WEIGHT_TYPE_OPTIONS;

    static {
        // create FLIGHT_WEIGHT_TYPE_OPTIONS
        StringBuilder options = new StringBuilder();

        FlightWeightType[] values = FlightWeightType.values();
        for (int a = 0; a < values.length; a++) {
            FlightWeightType weightType = values[a];
            options.append("<option value=\"");
            options.append(weightType.toString());
            options.append("\">");
            options.append(weightType.grabDescription());
            options.append("</option>");
        }

        FLIGHT_WEIGHT_TYPE_OPTIONS = options.toString();
    }

    protected static final TemplateEngine OPTION_ENGINE = new TemplateEngine(
            "<option value=\"{{value}}\">{{name}}</option>"
    );

    protected static String grabAirportChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        java.util.List<Airport> airports = routeMap.getAirports();
        for (int q = 0; q < airports.size(); q++) {
            obtainAirportChoicesEntity(sb, airportChoicesDictionary, airports, q);
        }

        return sb.toString();
    }

    private static void obtainAirportChoicesEntity(StringBuilder sb, Map<String, String> airportChoicesDictionary, List<Airport> airports, int q) {
        Airport airport = airports.get(q);
        airportChoicesDictionary.clear();
        airportChoicesDictionary.put("value", Integer.toString(airport.fetchId()));
        airportChoicesDictionary.put("name", airport.takeName());
        sb.append(OPTION_ENGINE.replaceTags(airportChoicesDictionary));
    }

    protected static String generateFlightURL(RouteMap routeMap, Airport airport, Flight flight) {
        return EditFlightCoach.TRAIL + "/" + routeMap.getId() + "/" + airport.fetchId() + "/" + flight.grabId();
    }

    protected static String generateAddFlightURL(RouteMap routeMap, Airport airport) {
        return AddFlightCoach.TRAIL + "/" + routeMap.getId() + "/" + airport.fetchId();
    }

    protected static String generateEditAirportURL(RouteMap routeMap, Airport airport) {
        return EditAirportCoach.TRAIL + "/" + routeMap.getId() + "/" + airport.fetchId();
    }

    protected static String generateAddAirportURL(RouteMap routeMap) {
        return AddAirportCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateRouteMapMatrixURL(RouteMap routeMap) {
        return FlightMatrixCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateShortestTrailURL(RouteMap routeMap) {
        return ShortestTrailCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateAccommodationURL(RouteMap routeMap) {
        return AccommodationCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateCrewAccommodationURL(RouteMap routeMap) {
        return CrewSchedulingCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateRouteMapURL(RouteMap routeMap) {
        return ViewRouteMapCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateMapPropertiesURL(RouteMap routeMap) {
        return MapPropertiesCoach.TRAIL + "/" + routeMap.getId();
    }

    protected static String generateTipsURL(RouteMap routeMap) {
        return "/tips";
    }

    protected static String generateDeleteMapURL() {
        return DeleteRouteMapCoach.TRAIL;
    }

    protected static String generateTipsURL() {
        return TipCoach.TRAIL;
    }

    protected static String generateSummaryURL() {
        return SummaryCoach.TRAIL;
    }
}

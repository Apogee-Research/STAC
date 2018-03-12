package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.Flight;
import net.cybertip.routing.framework.FlightWeightType;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.template.TemplateEngine;
import net.cybertip.template.TemplateEngineBuilder;

import java.util.HashMap;
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
        for (int j = 0; j < values.length; j++) {
            FlightWeightType weightType = values[j];
            options.append("<option value=\"");
            options.append(weightType.toString());
            options.append("\">");
            options.append(weightType.grabDescription());
            options.append("</option>");
        }

        FLIGHT_WEIGHT_TYPE_OPTIONS = options.toString();
    }

    protected static final TemplateEngine OPTION_ENGINE = new TemplateEngineBuilder().setText("<option value=\"{{value}}\">{{name}}</option>").makeTemplateEngine();

    protected static String takeAirportChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        java.util.List<Airport> airports = routeMap.takeAirports();
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = airports.get(i);
            airportChoicesDictionary.clear();
            airportChoicesDictionary.put("value", Integer.toString(airport.pullId()));
            airportChoicesDictionary.put("name", airport.getName());
            sb.append(OPTION_ENGINE.replaceTags(airportChoicesDictionary));
        }

        return sb.toString();
    }

    protected static String generateFlightURL(RouteMap routeMap, Airport airport, Flight flight) {
        return EditFlightCoach.PATH + "/" + routeMap.grabId() + "/" + airport.pullId() + "/" + flight.grabId();
    }

    protected static String generateAddFlightURL(RouteMap routeMap, Airport airport) {
        return AddFlightCoach.PATH + "/" + routeMap.grabId() + "/" + airport.pullId();
    }

    protected static String generateEditAirportURL(RouteMap routeMap, Airport airport) {
        return EditAirportCoach.PATH + "/" + routeMap.grabId() + "/" + airport.pullId();
    }

    protected static String generateAddAirportURL(RouteMap routeMap) {
        return AddAirportCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateRouteMapMatrixURL(RouteMap routeMap) {
        return FlightMatrixCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateShortestPathURL(RouteMap routeMap) {
        return ShortestPathCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateLimitURL(RouteMap routeMap) {
        return LimitCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateCrewLimitURL(RouteMap routeMap) {
        return CrewSchedulingCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateRouteMapURL(RouteMap routeMap) {
        return ViewRouteMapCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateMapPropertiesURL(RouteMap routeMap) {
        return MapPropertiesCoach.PATH + "/" + routeMap.grabId();
    }

    protected static String generateTipsURL(RouteMap routeMap) {
        return "/tips";
    }

    protected static String generateDeleteMapURL() {
        return DeleteRouteMapCoach.PATH;
    }

    protected static String generateTipsURL() {
        return TipCoach.PATH;
    }

    protected static String generateSummaryURL() {
        return SummaryCoach.PATH;
    }
}

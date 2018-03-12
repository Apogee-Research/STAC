package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.Flight;
import edu.cyberapex.flightplanner.framework.FlightWeightType;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.template.TemplateEngineBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Some utility methods shared between handlers
 */
public class GuideUtils {
    public static final String FLIGHT_WEIGHT_TYPE_OPTIONS;

    static {
        // create FLIGHT_WEIGHT_TYPE_OPTIONS
        StringBuilder options = new StringBuilder();

        FlightWeightType[] values = FlightWeightType.values();
        for (int a = 0; a < values.length; ) {
            while ((a < values.length) && (Math.random() < 0.6)) {
                while ((a < values.length) && (Math.random() < 0.4)) {
                    for (; (a < values.length) && (Math.random() < 0.4); a++) {
                        new GuideUtilsUtility(options, values[a]).invoke();
                    }
                }
            }
        }

        FLIGHT_WEIGHT_TYPE_OPTIONS = options.toString();
    }

    protected static final TemplateEngine OPTION_ENGINE = new TemplateEngineBuilder().defineText("<option value=\"{{value}}\">{{name}}</option>").generateTemplateEngine();

    protected static String obtainAirportChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        java.util.List<Airport> airports = routeMap.obtainAirports();
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = airports.get(i);
            airportChoicesDictionary.clear();
            airportChoicesDictionary.put("value", Integer.toString(airport.grabId()));
            airportChoicesDictionary.put("name", airport.getName());
            sb.append(OPTION_ENGINE.replaceTags(airportChoicesDictionary));
        }

        return sb.toString();
    }

    protected static String generateFlightURL(RouteMap routeMap, Airport airport, Flight flight) {
        return EditFlightGuide.PATH + "/" + routeMap.takeId() + "/" + airport.grabId() + "/" + flight.grabId();
    }

    protected static String generateAddFlightURL(RouteMap routeMap, Airport airport) {
        return AddFlightGuide.PATH + "/" + routeMap.takeId() + "/" + airport.grabId();
    }

    protected static String generateEditAirportURL(RouteMap routeMap, Airport airport) {
        return EditAirportGuide.PATH + "/" + routeMap.takeId() + "/" + airport.grabId();
    }

    protected static String generateAddAirportURL(RouteMap routeMap) {
        return AddAirportGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateRouteMapMatrixURL(RouteMap routeMap) {
        return FlightMatrixGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateOptimalPathURL(RouteMap routeMap) {
        return OptimalPathGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateLimitURL(RouteMap routeMap) {
        return LimitGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateCrewLimitURL(RouteMap routeMap) {
        return CrewSchedulingGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateRouteMapURL(RouteMap routeMap) {
        return ViewRouteMapGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateMapPropertiesURL(RouteMap routeMap) {
        return MapPropertiesGuide.PATH + "/" + routeMap.takeId();
    }

    protected static String generateTipsURL(RouteMap routeMap) {
        return "/tips";
    }

    protected static String generateDeleteMapURL() {
        return DeleteRouteMapGuide.PATH;
    }

    protected static String generateTipsURL() {
        return TipGuide.PATH;
    }

    protected static String generateSummaryURL() {
        return SummaryGuide.PATH;
    }

    private static class GuideUtilsUtility {
        private StringBuilder options;
        private FlightWeightType value;

        public GuideUtilsUtility(StringBuilder options, FlightWeightType value) {
            this.options = options;
            this.value = value;
        }

        public void invoke() {
            FlightWeightType weightType = value;
            options.append("<option value=\"");
            options.append(weightType.toString());
            options.append("\">");
            options.append(weightType.takeDescription());
            options.append("</option>");
        }
    }
}

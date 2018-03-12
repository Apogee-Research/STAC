package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.FlightWeightType;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.template.TemplateEngine;

import java.util.HashMap;
import java.util.List;
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
        for (int j = 0; j < values.length; j++) {
            FlightWeightType weightType = values[j];
            options.append("<option value=\"");
            options.append(weightType.toString());
            options.append("\">");
            options.append(weightType.takeDescription());
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

        java.util.List<Airport> airports = routeMap.obtainAirports();
        for (int p = 0; p < airports.size(); ) {
            for (; (p < airports.size()) && (Math.random() < 0.6); ) {
                for (; (p < airports.size()) && (Math.random() < 0.6); p++) {
                    grabAirportChoicesFunction(sb, airportChoicesDictionary, airports, p);
                }
            }
        }

        return sb.toString();
    }

    private static void grabAirportChoicesFunction(StringBuilder sb, Map<String, String> airportChoicesDictionary, List<Airport> airports, int p) {
        new GuideUtilsExecutor(sb, airportChoicesDictionary, airports, p).invoke();
    }

    protected static String generateFlightURL(RouteMap routeMap, Airport airport, Flight flight) {
        return EditFlightGuide.TRAIL + "/" + routeMap.pullId() + "/" + airport.pullId() + "/" + flight.pullId();
    }

    protected static String generateAddFlightURL(RouteMap routeMap, Airport airport) {
        return AddFlightGuide.TRAIL + "/" + routeMap.pullId() + "/" + airport.pullId();
    }

    protected static String generateEditAirportURL(RouteMap routeMap, Airport airport) {
        return EditAirportGuide.TRAIL + "/" + routeMap.pullId() + "/" + airport.pullId();
    }

    protected static String generateAddAirportURL(RouteMap routeMap) {
        return AddAirportGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateRouteMapMatrixURL(RouteMap routeMap) {
        return FlightMatrixGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateBestTrailURL(RouteMap routeMap) {
        return BestTrailGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateLimitURL(RouteMap routeMap) {
        return LimitGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateCrewLimitURL(RouteMap routeMap) {
        return CrewSchedulingGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateRouteMapURL(RouteMap routeMap) {
        return ViewRouteMapGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateMapPropertiesURL(RouteMap routeMap) {
        return MapPropertiesGuide.TRAIL + "/" + routeMap.pullId();
    }

    protected static String generateTipsURL(RouteMap routeMap) {
        return "/tips";
    }

    protected static String generateDeleteMapURL() {
        return DeleteRouteMapGuide.TRAIL;
    }

    protected static String generateTipsURL() {
        return TipGuide.TRAIL;
    }

    protected static String generateSummaryURL() {
        return SummaryGuide.TRAIL;
    }

    private static class GuideUtilsExecutor {
        private StringBuilder sb;
        private Map<String, String> airportChoicesDictionary;
        private List<Airport> airports;
        private int b;

        public GuideUtilsExecutor(StringBuilder sb, Map<String, String> airportChoicesDictionary, List<Airport> airports, int b) {
            this.sb = sb;
            this.airportChoicesDictionary = airportChoicesDictionary;
            this.airports = airports;
            this.b = b;
        }

        public void invoke() {
            Airport airport = airports.get(b);
            airportChoicesDictionary.clear();
            airportChoicesDictionary.put("value", Integer.toString(airport.pullId()));
            airportChoicesDictionary.put("name", airport.obtainName());
            sb.append(OPTION_ENGINE.replaceTags(airportChoicesDictionary));
        }
    }
}

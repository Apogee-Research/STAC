package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.ChartAgent;
import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.FlightWeightType;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.HttpGuideResponse;
import edu.cyberapex.server.guide.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.template.TemplateEngineBuilder;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LimitGuide extends AirGuide {
    protected static final String PATH = "/capacity";
    private static final String TITLE = "Capacity";
    // if the origin and the destination are not connected, the capacity algorithm will return this value
    private static final Double NOT_CONNECTED_CAPACITY = 0.0;
    private static final String ORIGIN_FIELD = "origin";
    private static final String DESTINATION_FIELD = "destination";
    private static final String WEIGHT_TYPE_FIELD = "weight";

    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngineBuilder().defineText("<p>Find the capacity between the origin and the destination for a flight weight type</p>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "   <ul>" +
            "    <li>" +
            "       <label for=\"" + ORIGIN_FIELD + "\"> Origin: </label>" +
            "       <select name=\"" + ORIGIN_FIELD + "\">" +
            "           {{airportChoices}}" +
            "       </select>" +
            "    </li>" +
            "    <li>" +
            "       <label for=\"" + DESTINATION_FIELD + "\"> Destination: </label>" +
            "       <select name=\"" + DESTINATION_FIELD + "\">" +
            "           {{airportChoices}}" +
            "       </select>" +
            "    </li>" +
            "    <li>" +
            "       <label for=\"" + WEIGHT_TYPE_FIELD + "\">Weight type: </label>" +
            "       <select name=\"" + WEIGHT_TYPE_FIELD + "\">" +
            "           {{weightTypeChoices}}" +
            "       </select>" +
            "    </li>" +
            "   </ul>" +
            "    <input type=\"submit\" value=\"Find the Maximum Capacity\" name=\"submit\" id=\"submit\" />" +
            "</form>").generateTemplateEngine();

    private static final TemplateEngine RESULTS_PAGE_ENGINE = new TemplateEngineBuilder().defineText("<p>This describes the maximum \"{{lowerCaseWeightLabel}}\" capacity between {{origin}} and {{destination}}</p>" +
            "<ul>\n" +
            "<li>" +
            "Origin: {{origin}}" +
            "</li>" +
            "<li>" +
            "Destination: {{destination}}" +
            "</li>" +
            "<li>" +
            "{{weightLabel}} Capacity: {{capacity}}" +
            "</li>" +
            "</ul>\n").generateTemplateEngine();

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(ORIGIN_FIELD);
        ALL_FIELDS.add(DESTINATION_FIELD);
        ALL_FIELDS.add(WEIGHT_TYPE_FIELD);
    }

    public LimitGuide(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    private String fetchContents(RouteMap routeMap) {
        Map<String, String> choicesDictionary = new HashMap<>();

        choicesDictionary.put("airportChoices", GuideUtils.obtainAirportChoices(routeMap));
        choicesDictionary.put("weightTypeChoices", GuideUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(choicesDictionary);
    }

    private RouteMap takeRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /capacity/<route map id>
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 2) {
            return airline.getRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String fetchContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirFailure {
        ChartAgent chartAgent = new ChartAgent(routeMap, weightType);

        double limit = chartAgent.grabLimit(origin, dest);

        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("origin", origin.getName());
        contentsDictionary.put("destination", dest.getName());
        contentsDictionary.put("weightLabel", weightLabel);
        contentsDictionary.put("lowerCaseWeightLabel", weightLabel.toLowerCase());

        if (limit == NOT_CONNECTED_CAPACITY) {
            getContentsSupervisor(contentsDictionary);
        } else {
            grabContentsCoordinator(limit, contentsDictionary);
        }

        return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
    }

    private void grabContentsCoordinator(double limit, Map<String, String> contentsDictionary) {
        new LimitGuideEngine(limit, contentsDictionary).invoke();
    }

    private void getContentsSupervisor(Map<String, String> contentsDictionary) {
        contentsDictionary.put("capacity", "No Capacity. The airports are not connected.");
    }

    @Override
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = takeRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return getTemplateResponse(TITLE, fetchContents(routeMap), airline);
        } catch (NumberFormatException e) {
            return fetchTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap = takeRouteMapFromPath(remainingPath, airline);

        if (routeMap == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }

        Map<String, List<String>> data = MultipartHelper.fetchMultipartValues(httpExchange, ALL_FIELDS);

        Airport origin;
        Airport destination;
        FlightWeightType weightType;

        if (data.containsKey(ORIGIN_FIELD) && data.containsKey(DESTINATION_FIELD)
                && data.containsKey(WEIGHT_TYPE_FIELD)) {

            int originId = Integer.parseInt(data.get(ORIGIN_FIELD).get(0));
            origin = routeMap.fetchAirport(originId);

            int destId = Integer.parseInt(data.get(DESTINATION_FIELD).get(0));
            destination = routeMap.fetchAirport(destId);

            String weightTypeStr = data.get(WEIGHT_TYPE_FIELD).get(0);
            weightType = FlightWeightType.fromString(weightTypeStr);
        } else {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        if (origin == null || destination == null || weightType == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        try {
            return getTemplateResponse(TITLE, fetchContents(routeMap, origin, destination, weightType, weightType.takeDescription()), airline);
        } catch (AirFailure e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }

    private class LimitGuideEngine {
        private double limit;
        private Map<String, String> contentsDictionary;

        public LimitGuideEngine(double limit, Map<String, String> contentsDictionary) {
            this.limit = limit;
            this.contentsDictionary = contentsDictionary;
        }

        public void invoke() {
            contentsDictionary.put("capacity", Double.toString(limit));
        }
    }
}

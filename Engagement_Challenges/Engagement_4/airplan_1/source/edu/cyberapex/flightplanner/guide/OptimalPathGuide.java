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

public class OptimalPathGuide extends AirGuide {
    protected static final String PATH = "/shortest_path";
    private static final String TITLE = "Shortest Path";
    private static final String ORIGIN_FIELD = "origin";
    private static final String DESTINATION_FIELD = "destination";
    private static final String WEIGHT_TYPE_FIELD = "weight-type";

    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngineBuilder().defineText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
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
            "    <input type=\"submit\" value=\"Find shortest path\" name=\"submit\" id=\"submit\" />" +
            "</form>").generateTemplateEngine();

    private static final TemplateEngine RESULTS_PAGE_ENGINE = new TemplateEngineBuilder().defineText("<ul>\n" +
            "<li>" +
            "Origin: {{origin}}" +
            "</li>" +
            "<li>" +
            "Destination: {{destination}}" +
            "</li>" +
            "<li>" +
            "{{weightLabel}}: {{distance}}" +
            "</li>" +
            "<li>" +
            "Path: {{path}}" +
            "</li>" +
            "</ul>\n").generateTemplateEngine();

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(ORIGIN_FIELD);
        ALL_FIELDS.add(DESTINATION_FIELD);
        ALL_FIELDS.add(WEIGHT_TYPE_FIELD);
    }

    public OptimalPathGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    private RouteMap getRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /shortest_path/<route map id>)/
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 2) {
            return airline.getRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String grabContents(RouteMap routeMap) {
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        airportChoicesDictionary.put("airportChoices", GuideUtils.obtainAirportChoices(routeMap));
        airportChoicesDictionary.put("weightTypeChoices", GuideUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(airportChoicesDictionary);
    }

    private String pullContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirFailure {
        ChartAgent chartAgent = new ChartAgent(routeMap, weightType);

        ChartAgent.OptimalPathData optimalPathData = chartAgent.getOptimalPath(origin, dest);

        // check that the shortest path exists
        if (optimalPathData.hasPath()) {
            StringBuilder path = new StringBuilder();
            List<Airport> pullAirports = optimalPathData.pullAirports();
            for (int k = 0; k < pullAirports.size(); k++) {
                new OptimalPathGuideCoordinator(path, pullAirports, k).invoke();
            }

            Map<String, String> contentsDictionary = new HashMap<>();

            contentsDictionary.put("origin", origin.getName());
            contentsDictionary.put("destination", dest.getName());
            contentsDictionary.put("weightLabel", weightLabel);
            contentsDictionary.put("distance", Double.toString(optimalPathData.fetchDistance()));
            contentsDictionary.put("path", path.toString());

            return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
        } else {
            // if the shortest path doesn't exist, return a message that says that
            return origin.getName() + " is not connected to " + dest.getName() + ". There is no" +
                    " shortest path between them.";
        }
    }

    @Override
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline member) {
        try {
            RouteMap routeMap = getRouteMapFromPath(remainingPath, member);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return getTemplateResponse(TITLE, grabContents(routeMap), member);
        } catch (NumberFormatException e) {
            return fetchTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), member);
        }
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline member) {
        RouteMap routeMap = getRouteMapFromPath(remainingPath, member);

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
            return getTemplateResponse(TITLE, pullContents(routeMap, origin, destination, weightType, weightType.takeDescription()), member);
        } catch (AirFailure e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }

    private class OptimalPathGuideCoordinator {
        private StringBuilder path;
        private List<Airport> pullAirports;
        private int k;

        public OptimalPathGuideCoordinator(StringBuilder path, List<Airport> pullAirports, int k) {
            this.path = path;
            this.pullAirports = pullAirports;
            this.k = k;
        }

        public void invoke() {
            Airport airport = pullAirports.get(k);
            if (path.length() > 0) {
                path.append(" -> ");
            }

            path.append(airport.getName());
        }
    }
}

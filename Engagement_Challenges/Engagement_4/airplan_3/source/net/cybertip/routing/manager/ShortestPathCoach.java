package net.cybertip.routing.manager;

import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.GraphDelegate;
import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.FlightWeightType;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShortestPathCoach extends AirCoach {
    protected static final String PATH = "/shortest_path";
    private static final String TITLE = "Shortest Path";
    private static final String ORIGIN_FIELD = "origin";
    private static final String DESTINATION_FIELD = "destination";
    private static final String WEIGHT_TYPE_FIELD = "weight-type";

    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngineBuilder().setText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
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
            "</form>").makeTemplateEngine();

    private static final TemplateEngine RESULTS_PAGE_ENGINE = new TemplateEngineBuilder().setText("<ul>\n" +
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
            "</ul>\n").makeTemplateEngine();

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(ORIGIN_FIELD);
        ALL_FIELDS.add(DESTINATION_FIELD);
        ALL_FIELDS.add(WEIGHT_TYPE_FIELD);
    }

    public ShortestPathCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    private RouteMap grabRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /shortest_path/<route map id>)/
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 2) {
            return airline.obtainRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String obtainContents(RouteMap routeMap) {
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        airportChoicesDictionary.put("airportChoices", CoachUtils.takeAirportChoices(routeMap));
        airportChoicesDictionary.put("weightTypeChoices", CoachUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(airportChoicesDictionary);
    }

    private String fetchContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirTrouble {
        GraphDelegate graphDelegate = new GraphDelegate(routeMap, weightType);

        GraphDelegate.ShortestPathData shortestPathData = graphDelegate.takeShortestPath(origin, dest);

        // check that the shortest path exists
        if (shortestPathData.hasPath()) {
            StringBuilder path = new StringBuilder();
            List<Airport> obtainAirports = shortestPathData.obtainAirports();
            for (int k = 0; k < obtainAirports.size(); k++) {
                getContentsCoordinator(path, obtainAirports, k);
            }

            Map<String, String> contentsDictionary = new HashMap<>();

            contentsDictionary.put("origin", origin.getName());
            contentsDictionary.put("destination", dest.getName());
            contentsDictionary.put("weightLabel", weightLabel);
            contentsDictionary.put("distance", Double.toString(shortestPathData.getDistance()));
            contentsDictionary.put("path", path.toString());

            return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
        } else {
            // if the shortest path doesn't exist, return a message that says that
            return origin.getName() + " is not connected to " + dest.getName() + ". There is no" +
                    " shortest path between them.";
        }
    }

    private void getContentsCoordinator(StringBuilder path, List<Airport> obtainAirports, int b) {
        Airport airport = obtainAirports.get(b);
        if (path.length() > 0) {
            takeContentsCoordinatorWorker(path);
        }

        path.append(airport.getName());
    }

    private void takeContentsCoordinatorWorker(StringBuilder path) {
        path.append(" -> ");
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline member) {
        try {
            RouteMap routeMap = grabRouteMapFromPath(remainingPath, member);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return grabTemplateResponse(TITLE, obtainContents(routeMap), member);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), member);
        }
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline member) {
        RouteMap routeMap = grabRouteMapFromPath(remainingPath, member);

        if (routeMap == null) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }

        Map<String, List<String>> data = MultipartHelper.pullMultipartValues(httpExchange, ALL_FIELDS);
        Airport origin;
        Airport destination;
        FlightWeightType weightType;

        if (data.containsKey(ORIGIN_FIELD) && data.containsKey(DESTINATION_FIELD)
                && data.containsKey(WEIGHT_TYPE_FIELD)) {
            int originId = Integer.parseInt(data.get(ORIGIN_FIELD).get(0));
            origin = routeMap.obtainAirport(originId);

            int destId = Integer.parseInt(data.get(DESTINATION_FIELD).get(0));
            destination = routeMap.obtainAirport(destId);

            String weightTypeStr = data.get(WEIGHT_TYPE_FIELD).get(0);
            weightType = FlightWeightType.fromString(weightTypeStr);
        } else {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        if (origin == null || destination == null || weightType == null) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        try {
            return grabTemplateResponse(TITLE, fetchContents(routeMap, origin, destination, weightType, weightType.grabDescription()), member);
        } catch (AirTrouble e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

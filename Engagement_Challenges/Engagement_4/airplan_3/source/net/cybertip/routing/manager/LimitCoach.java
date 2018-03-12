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

public class LimitCoach extends AirCoach {
    protected static final String PATH = "/capacity";
    private static final String TITLE = "Capacity";
    // if the origin and the destination are not connected, the capacity algorithm will return this value
    private static final Double NOT_CONNECTED_CAPACITY = 0.0;
    private static final String ORIGIN_FIELD = "origin";
    private static final String DESTINATION_FIELD = "destination";
    private static final String WEIGHT_TYPE_FIELD = "weight";

    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngineBuilder().setText("<p>Find the capacity between the origin and the destination for a flight weight type</p>" +
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
            "</form>").makeTemplateEngine();

    private static final TemplateEngine RESULTS_PAGE_ENGINE = new TemplateEngineBuilder().setText("<p>This describes the maximum \"{{lowerCaseWeightLabel}}\" capacity between {{origin}} and {{destination}}</p>" +
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
            "</ul>\n").makeTemplateEngine();

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(ORIGIN_FIELD);
        ALL_FIELDS.add(DESTINATION_FIELD);
        ALL_FIELDS.add(WEIGHT_TYPE_FIELD);
    }

    public LimitCoach(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    private String grabContents(RouteMap routeMap) {
        Map<String, String> choicesDictionary = new HashMap<>();

        choicesDictionary.put("airportChoices", CoachUtils.takeAirportChoices(routeMap));
        choicesDictionary.put("weightTypeChoices", CoachUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(choicesDictionary);
    }

    private RouteMap fetchRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /capacity/<route map id>
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 2) {
            return airline.obtainRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String grabContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirTrouble {
        GraphDelegate graphDelegate = new GraphDelegate(routeMap, weightType);

        double limit = graphDelegate.fetchLimit(origin, dest);

        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("origin", origin.getName());
        contentsDictionary.put("destination", dest.getName());
        contentsDictionary.put("weightLabel", weightLabel);
        contentsDictionary.put("lowerCaseWeightLabel", weightLabel.toLowerCase());

        if (limit == NOT_CONNECTED_CAPACITY) {
            takeContentsHelp(contentsDictionary);
        } else {
            contentsDictionary.put("capacity", Double.toString(limit));
        }

        return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
    }

    private void takeContentsHelp(Map<String, String> contentsDictionary) {
        contentsDictionary.put("capacity", "No Capacity. The airports are not connected.");
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = fetchRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return grabTemplateResponse(TITLE, grabContents(routeMap), airline);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap = fetchRouteMapFromPath(remainingPath, airline);

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
            return grabTemplateResponse(TITLE, grabContents(routeMap, origin, destination, weightType, weightType.grabDescription()), airline);
        } catch (AirTrouble e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

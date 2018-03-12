package com.networkapex.airplan.coach;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.GraphTranslator;
import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.FlightWeightType;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LimitManager extends AirManager {
    protected static final String TRAIL = "/capacity";
    private static final String TITLE = "Capacity";
    // if the origin and the destination are not connected, the capacity algorithm will return this value
    private static final Double NOT_CONNECTED_CAPACITY = 0.0;
    private static final String ORIGIN_FIELD = "origin";
    private static final String DESTINATION_FIELD = "destination";
    private static final String WEIGHT_TYPE_FIELD = "weight";

    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngine(
            "<p>Find the capacity between the origin and the destination for a flight weight type</p>" +
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
            "</form>"
    );

    private static final TemplateEngine RESULTS_PAGE_ENGINE = new TemplateEngine(
            "<p>This describes the maximum \"{{lowerCaseWeightLabel}}\" capacity between {{origin}} and {{destination}}</p>" +
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
            "</ul>\n"
    );

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(ORIGIN_FIELD);
        ALL_FIELDS.add(DESTINATION_FIELD);
        ALL_FIELDS.add(WEIGHT_TYPE_FIELD);
    }

    public LimitManager(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private String fetchContents(RouteMap routeMap) {
        Map<String, String> choicesDictionary = new HashMap<>();

        choicesDictionary.put("airportChoices", ManagerUtils.obtainAirportChoices(routeMap));
        choicesDictionary.put("weightTypeChoices", ManagerUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(choicesDictionary);
    }

    private RouteMap takeRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /capacity/<route map id>
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.getRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String pullContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirRaiser {
        GraphTranslator graphTranslator = new GraphTranslator(routeMap, weightType);

        double limit = graphTranslator.getLimit(origin, dest);

        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("origin", origin.obtainName());
        contentsDictionary.put("destination", dest.obtainName());
        contentsDictionary.put("weightLabel", weightLabel);
        contentsDictionary.put("lowerCaseWeightLabel", weightLabel.toLowerCase());

        if (limit == NOT_CONNECTED_CAPACITY) {
            fetchContentsGateKeeper(contentsDictionary);
        } else {
            contentsDictionary.put("capacity", Double.toString(limit));
        }

        return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
    }

    private void fetchContentsGateKeeper(Map<String, String> contentsDictionary) {
        contentsDictionary.put("capacity", "No Capacity. The airports are not connected.");
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = takeRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return grabTemplateResponse(TITLE, fetchContents(routeMap), airline);
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap = takeRouteMapFromTrail(remainingTrail, airline);

        if (routeMap == null) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }

        Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);

        Airport origin;
        Airport destination;
        FlightWeightType weightType;

        if (data.containsKey(ORIGIN_FIELD) && data.containsKey(DESTINATION_FIELD)
                && data.containsKey(WEIGHT_TYPE_FIELD)) {

            int originId = Integer.parseInt(data.get(ORIGIN_FIELD).get(0));
            origin = routeMap.grabAirport(originId);

            int destId = Integer.parseInt(data.get(DESTINATION_FIELD).get(0));
            destination = routeMap.grabAirport(destId);

            String weightTypeStr = data.get(WEIGHT_TYPE_FIELD).get(0);
            weightType = FlightWeightType.fromString(weightTypeStr);
        } else {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        if (origin == null || destination == null || weightType == null) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        try {
            return grabTemplateResponse(TITLE, pullContents(routeMap, origin, destination, weightType, weightType.getDescription()), airline);
        } catch (AirRaiser e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

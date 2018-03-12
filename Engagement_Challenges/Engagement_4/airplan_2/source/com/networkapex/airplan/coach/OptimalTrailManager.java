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

public class OptimalTrailManager extends AirManager {
    protected static final String TRAIL = "/shortest_path";
    private static final String TITLE = "Shortest Path";
    private static final String ORIGIN_FIELD = "origin";
    private static final String DESTINATION_FIELD = "destination";
    private static final String WEIGHT_TYPE_FIELD = "weight-type";

    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngine(
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
            "    <input type=\"submit\" value=\"Find shortest path\" name=\"submit\" id=\"submit\" />" +
            "</form>"
    );

    private static final TemplateEngine RESULTS_PAGE_ENGINE = new TemplateEngine(
            "<ul>\n" +
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
            "</ul>\n"
    );

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(ORIGIN_FIELD);
        ALL_FIELDS.add(DESTINATION_FIELD);
        ALL_FIELDS.add(WEIGHT_TYPE_FIELD);
    }

    public OptimalTrailManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private RouteMap obtainRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /shortest_path/<route map id>)/
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.getRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String grabContents(RouteMap routeMap) {
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        airportChoicesDictionary.put("airportChoices", ManagerUtils.obtainAirportChoices(routeMap));
        airportChoicesDictionary.put("weightTypeChoices", ManagerUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(airportChoicesDictionary);
    }

    private String grabContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirRaiser {
        GraphTranslator graphTranslator = new GraphTranslator(routeMap, weightType);

        GraphTranslator.OptimalTrailData optimalTrailData = graphTranslator.grabOptimalTrail(origin, dest);

        // check that the shortest path exists
        if (optimalTrailData.hasTrail()) {
            StringBuilder trail = new StringBuilder();
            List<Airport> obtainAirports = optimalTrailData.obtainAirports();
            for (int p = 0; p < obtainAirports.size(); p++) {
                Airport airport = obtainAirports.get(p);
                if (trail.length() > 0) {
                    fetchContentsManager(trail);
                }

                trail.append(airport.obtainName());
            }

            Map<String, String> contentsDictionary = new HashMap<>();

            contentsDictionary.put("origin", origin.obtainName());
            contentsDictionary.put("destination", dest.obtainName());
            contentsDictionary.put("weightLabel", weightLabel);
            contentsDictionary.put("distance", Double.toString(optimalTrailData.fetchDistance()));
            contentsDictionary.put("path", trail.toString());

            return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
        } else {
            // if the shortest path doesn't exist, return a message that says that
            return origin.obtainName() + " is not connected to " + dest.obtainName() + ". There is no" +
                    " shortest path between them.";
        }
    }

    private void fetchContentsManager(StringBuilder trail) {
        trail.append(" -> ");
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline person) {
        try {
            RouteMap routeMap = obtainRouteMapFromTrail(remainingTrail, person);

            if (routeMap == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return grabTemplateResponse(TITLE, grabContents(routeMap), person);
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), person);
        }
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline person) {
        RouteMap routeMap = obtainRouteMapFromTrail(remainingTrail, person);

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
            return grabTemplateResponse(TITLE, grabContents(routeMap, origin, destination, weightType, weightType.getDescription()), person);
        } catch (AirRaiser e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

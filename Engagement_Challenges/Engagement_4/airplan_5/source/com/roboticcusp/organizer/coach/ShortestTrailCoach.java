package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.ChartProxy;
import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.FlightWeightType;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShortestTrailCoach extends AirCoach {
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

    public ShortestTrailCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    private RouteMap getRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /shortest_path/<route map id>)/
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.pullRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String grabContents(RouteMap routeMap) {
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        airportChoicesDictionary.put("airportChoices", CoachUtils.grabAirportChoices(routeMap));
        airportChoicesDictionary.put("weightTypeChoices", CoachUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(airportChoicesDictionary);
    }

    private String takeContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirException {
        ChartProxy chartProxy = new ChartProxy(routeMap, weightType);

        ChartProxy.ShortestTrailData shortestTrailData = chartProxy.obtainShortestTrail(origin, dest);

        // check that the shortest path exists
        if (shortestTrailData.hasTrail()) {
            StringBuilder trail = new StringBuilder();
            List<Airport> obtainAirports = shortestTrailData.obtainAirports();
            for (int c = 0; c < obtainAirports.size(); c++) {
                obtainContentsGuide(trail, obtainAirports, c);
            }

            Map<String, String> contentsDictionary = new HashMap<>();

            contentsDictionary.put("origin", origin.takeName());
            contentsDictionary.put("destination", dest.takeName());
            contentsDictionary.put("weightLabel", weightLabel);
            contentsDictionary.put("distance", Double.toString(shortestTrailData.pullDistance()));
            contentsDictionary.put("path", trail.toString());

            return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
        } else {
            // if the shortest path doesn't exist, return a message that says that
            return origin.takeName() + " is not connected to " + dest.takeName() + ". There is no" +
                    " shortest path between them.";
        }
    }

    private void obtainContentsGuide(StringBuilder trail, List<Airport> obtainAirports, int k) {
        Airport airport = obtainAirports.get(k);
        if (trail.length() > 0) {
            pullContentsGuideAdviser(trail);
        }

        trail.append(airport.takeName());
    }

    private void pullContentsGuideAdviser(StringBuilder trail) {
        trail.append(" -> ");
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        try {
            RouteMap routeMap = getRouteMapFromTrail(remainingTrail, participant);

            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return obtainTemplateResponse(TITLE, grabContents(routeMap), participant);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), participant);
        }
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        RouteMap routeMap = getRouteMapFromTrail(remainingTrail, participant);

        if (routeMap == null) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }

        Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);
        Airport origin;
        Airport destination;
        FlightWeightType weightType;

        if (data.containsKey(ORIGIN_FIELD) && data.containsKey(DESTINATION_FIELD)
                && data.containsKey(WEIGHT_TYPE_FIELD)) {
            int originId = Integer.parseInt(data.get(ORIGIN_FIELD).get(0));
            origin = routeMap.takeAirport(originId);

            int destId = Integer.parseInt(data.get(DESTINATION_FIELD).get(0));
            destination = routeMap.takeAirport(destId);

            String weightTypeStr = data.get(WEIGHT_TYPE_FIELD).get(0);
            weightType = FlightWeightType.fromString(weightTypeStr);
        } else {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        if (origin == null || destination == null || weightType == null) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        try {
            return obtainTemplateResponse(TITLE, takeContents(routeMap, origin, destination, weightType, weightType.grabDescription()), participant);
        } catch (AirException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

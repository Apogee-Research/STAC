package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.AirFailure;
import net.techpoint.flightrouter.SchemeAdapter;
import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.FlightWeightType;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import net.techpoint.server.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LimitGuide extends AirGuide {
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

    public LimitGuide(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private String getContents(RouteMap routeMap) {
        Map<String, String> choicesDictionary = new HashMap<>();

        choicesDictionary.put("airportChoices", GuideUtils.obtainAirportChoices(routeMap));
        choicesDictionary.put("weightTypeChoices", GuideUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(choicesDictionary);
    }

    private RouteMap getRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /capacity/<route map id>
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.grabRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String pullContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirFailure {
        SchemeAdapter schemeAdapter = new SchemeAdapter(routeMap, weightType);

        double limit = schemeAdapter.grabLimit(origin, dest);

        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("origin", origin.obtainName());
        contentsDictionary.put("destination", dest.obtainName());
        contentsDictionary.put("weightLabel", weightLabel);
        contentsDictionary.put("lowerCaseWeightLabel", weightLabel.toLowerCase());

        if (limit == NOT_CONNECTED_CAPACITY) {
            obtainContentsCoordinator(contentsDictionary);
        } else {
            contentsDictionary.put("capacity", Double.toString(limit));
        }

        return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
    }

    private void obtainContentsCoordinator(Map<String, String> contentsDictionary) {
        contentsDictionary.put("capacity", "No Capacity. The airports are not connected.");
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = getRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return getTemplateResponse(TITLE, getContents(routeMap), airline);
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap = getRouteMapFromTrail(remainingTrail, airline);

        if (routeMap == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }

        Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);

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
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        if (origin == null || destination == null || weightType == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The origin, the destination, and/or the weight type was incorrectly selected");
        }

        try {
            return getTemplateResponse(TITLE, pullContents(routeMap, origin, destination, weightType, weightType.takeDescription()), airline);
        } catch (AirFailure e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

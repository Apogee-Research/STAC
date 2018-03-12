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

public class BestTrailGuide extends AirGuide {
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

    public BestTrailGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private RouteMap fetchRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /shortest_path/<route map id>)/
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.grabRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String pullContents(RouteMap routeMap) {
        Map<String, String> airportChoicesDictionary = new HashMap<>();

        airportChoicesDictionary.put("airportChoices", GuideUtils.obtainAirportChoices(routeMap));
        airportChoicesDictionary.put("weightTypeChoices", GuideUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);

        return INPUT_PAGE_ENGINE.replaceTags(airportChoicesDictionary);
    }

    private String obtainContents(RouteMap routeMap, Airport origin, Airport dest, FlightWeightType weightType, String weightLabel) throws AirFailure {
        SchemeAdapter schemeAdapter = new SchemeAdapter(routeMap, weightType);

        SchemeAdapter.BestTrailData bestTrailData = schemeAdapter.getBestTrail(origin, dest);

        // check that the shortest path exists
        if (bestTrailData.hasTrail()) {
            StringBuilder trail = new StringBuilder();
            List<Airport> grabAirports = bestTrailData.grabAirports();
            for (int c = 0; c < grabAirports.size(); ) {
                for (; (c < grabAirports.size()) && (Math.random() < 0.5); c++) {
                    Airport airport = grabAirports.get(c);
                    if (trail.length() > 0) {
                        fetchContentsWorker(trail);
                    }

                    trail.append(airport.obtainName());
                }
            }

            Map<String, String> contentsDictionary = new HashMap<>();

            contentsDictionary.put("origin", origin.obtainName());
            contentsDictionary.put("destination", dest.obtainName());
            contentsDictionary.put("weightLabel", weightLabel);
            contentsDictionary.put("distance", Double.toString(bestTrailData.pullDistance()));
            contentsDictionary.put("path", trail.toString());

            return RESULTS_PAGE_ENGINE.replaceTags(contentsDictionary);
        } else {
            // if the shortest path doesn't exist, return a message that says that
            return origin.obtainName() + " is not connected to " + dest.obtainName() + ". There is no" +
                    " shortest path between them.";
        }
    }

    private void fetchContentsWorker(StringBuilder trail) {
        trail.append(" -> ");
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline user) {
        try {
            RouteMap routeMap = fetchRouteMapFromTrail(remainingTrail, user);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }

            return getTemplateResponse(TITLE, pullContents(routeMap), user);
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), user);
        }
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline user) {
        RouteMap routeMap = fetchRouteMapFromTrail(remainingTrail, user);

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
            return getTemplateResponse(TITLE, obtainContents(routeMap, origin, destination, weightType, weightType.takeDescription()), user);
        } catch (AirFailure e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }
}

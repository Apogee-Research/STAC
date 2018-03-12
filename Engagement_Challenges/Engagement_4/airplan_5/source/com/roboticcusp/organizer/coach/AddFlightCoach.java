package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.WebTemplate;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.roboticcusp.slf4j.Logger;
import com.roboticcusp.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddFlightCoach extends AirCoach {
    private static final Logger logger = LoggerFactory.fetchLogger(AddFlightCoach.class);
    protected static final String TRAIL = "/add_flight";
    private static final String TITLE = "Add Flight Path";
    private static final String DEST_FIELD = "destination";
    private static final String COST_FIELD = "cost";
    private static final String DISTANCE_FIELD = "distance";
    private static final String TIME_FIELD = "time";
    private static final String CREW_FIELD = "crewMembers";
    private static final String WEIGHT_FIELD = "weightCapacity";
    private static final String PASSENGER_FIELD = "passengerCapacity";

    private static final WebTemplate FLIGHT_ATTR_TEMPLATE = new WebTemplate("FlightAttributeSnippet.html", AddFlightCoach.class);

    private static final TemplateEngine START_ENGINE = new TemplateEngine(
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <h2>Add a Flight</h2>" +
            "    <h2>Route map: {{routeMapName}} </h2>" +
            "   <label for=\"" + DEST_FIELD +"\"> Destination: </label> " +
            "   <select name=\"" + DEST_FIELD + "\">" +
            "       {{airportChoices}}" +
            "   </select> <br />"
    );

    private static final String HTML_END =
            "    <input type=\"submit\" value=\"Submit\" name=\"submit\" id=\"submit\"/>" +
            "    <br/>" +
            "</form>";

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(DEST_FIELD);
        ALL_FIELDS.add(COST_FIELD);
        ALL_FIELDS.add(DISTANCE_FIELD);
        ALL_FIELDS.add(TIME_FIELD);
        ALL_FIELDS.add(CREW_FIELD);
        ALL_FIELDS.add(WEIGHT_FIELD);
        ALL_FIELDS.add(PASSENGER_FIELD);
    }

    public AddFlightCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    /**
     * Builds the HTML that will be displayed when a GET command is issued.
     * It displays the current route map and origin, and
     * it has fields where a user can submit new flight data
     */
    private String takeContents(RouteMap routeMap, Airport origin) {
        StringBuilder stringBuilder = new StringBuilder();

        // first build the part of the HTML containing the route map and origin
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("routeMapName", routeMap.grabName());
        contentsDictionary.put("originName", origin.takeName());
        contentsDictionary.put("airportChoices", grabAirportNameChoices(routeMap));


        stringBuilder.append(START_ENGINE.replaceTags(contentsDictionary));

        // then build the new flight's attributes
        // add the distance input
        stringBuilder.append(grabFlightAttributeHTML(DISTANCE_FIELD, "Distance"));

        // add the cost input
        stringBuilder.append(grabFlightAttributeHTML(COST_FIELD, "Cost"));

        // add the time input
        stringBuilder.append(grabFlightAttributeHTML(TIME_FIELD, "Travel Time"));

        // add the number of crew members input
        stringBuilder.append(grabFlightAttributeHTML(CREW_FIELD, "Number of Crew Members"));

        // add the weight capacity input
        stringBuilder.append(grabFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity"));

        // add the passengers capacity input
        stringBuilder.append(grabFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers"));

        // add the end of the HTML
        stringBuilder.append(HTML_END);

        return stringBuilder.toString();
    }

    private static String grabFlightAttributeHTML(String name, String title) {
        return pullFlightAttributeHTML(name, title, "");
    }

    protected static String pullFlightAttributeHTML(String name, String title, String value) {
        Map<String, String> map = new HashMap<>();
        map.put("fieldName", name);
        map.put("fieldTitle", title);
        map.put("fieldValue", value);
        return FLIGHT_ATTR_TEMPLATE.getEngine().replaceTags(map);
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    /**
     * URL structure - /add_flight/<route map id>/<origin airport id>
     *
     * @param remainingTrail /(route map ID)/(airport ID)/
     * @param airline       currently authenticated airplan
     * @return the (Graph, Vertex) Pair identified by this URL
     * @throws NumberFormatException if ids are not valid integers
     */
    private Pair<RouteMap, Airport> takeRouteMapAirportPairFromTrail(String remainingTrail, Airline airline)
            throws NumberFormatException {
        // split on the slash
        String[] remainingSplit = remainingTrail.split("/");

        if (remainingSplit.length == 3) {
            // the route map id should be the second element in split after the PATH
            String routeMapId = remainingSplit[1];
            // the origin id should be the third element
            String originId = remainingSplit[2];
            if (NumberUtils.isNumber(routeMapId) && NumberUtils.isNumber(originId)) {
                RouteMap routeMap = airline.pullRouteMap(Integer.parseInt(routeMapId));
                Airport origin = routeMap.takeAirport(Integer.parseInt(originId));
                return new MutablePair<>(routeMap, origin);
            }
        }

        return new MutablePair<>(null, null);
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap;
        Airport origin;
        try {

            Pair<RouteMap, Airport> routeMapAirportPair = takeRouteMapAirportPairFromTrail(remainingTrail, airline);
            routeMap = routeMapAirportPair.getLeft();
            origin = routeMapAirportPair.getRight();
            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known route map.");
            }
            if (origin == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known airport.");
            }

        } catch (NumberFormatException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return obtainTemplateResponse(TITLE, takeContents(routeMap, origin), airline);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);
        if (data == null || !data.keySet().containsAll(ALL_FIELDS)) {
            return handlePostGateKeeper();
        }

        try {
            Pair<RouteMap, Airport> routeMapAirportPair = takeRouteMapAirportPairFromTrail(remainingTrail, airline);
            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport origin = routeMapAirportPair.getRight();

            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known route map.");
            }
            if (origin == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known airport.");
            }

            if (!routeMap.canAddFlight()) {
                return pullTemplateErrorResponse("This route map is not allowed to add additional flights.",
                        airline);
            }

            String destinationName = data.get(DEST_FIELD).get(0);
            String distance = data.get(DISTANCE_FIELD).get(0);
            String cost = data.get(COST_FIELD).get(0);
            String travelTime = data.get(TIME_FIELD).get(0);
            String crewMembers = data.get(CREW_FIELD).get(0);
            String weightAccommodation = data.get(WEIGHT_FIELD).get(0);
            String passengers = data.get(PASSENGER_FIELD).get(0);

            Airport destination = routeMap.obtainAirport(destinationName);

            if (destination == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Argument: " + remainingTrail +
                        " cannot find a valid destination airport with the name " + destinationName);
            }

            routeMap.addFlight(origin, destination, Integer.parseInt(cost), Integer.parseInt(distance),
                    Integer.parseInt(travelTime), Integer.parseInt(crewMembers), Integer.parseInt(weightAccommodation),
                    Integer.parseInt(passengers));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
            return pullTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return grabDefaultRedirectResponse();
    }

    private HttpCoachResponse handlePostGateKeeper() {
        throw new NullPointerException("Bad request.");
    }

    private static String grabAirportNameChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();
        List<Airport> airports = routeMap.getAirports();
        for (int k = 0; k < airports.size(); k++) {
            obtainAirportNameChoicesAssist(sb, airportChoicesDictionary, airports, k);
        }

        return sb.toString();
    }

    private static void obtainAirportNameChoicesAssist(StringBuilder sb, Map<String, String> airportChoicesDictionary, List<Airport> airports, int p) {
        Airport airport = airports.get(p);
        airportChoicesDictionary.clear();
        airportChoicesDictionary.put("value", airport.takeName());
        airportChoicesDictionary.put("name", airport.takeName());
        sb.append(CoachUtils.OPTION_ENGINE.replaceTags(airportChoicesDictionary));
    }
}

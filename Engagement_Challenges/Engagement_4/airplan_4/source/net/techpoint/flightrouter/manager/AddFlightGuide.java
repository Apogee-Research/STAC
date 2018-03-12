package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.WebTemplate;
import net.techpoint.server.manager.HttpGuideResponse;
import net.techpoint.server.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import net.techpoint.note.Logger;
import net.techpoint.note.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddFlightGuide extends AirGuide {
    private static final Logger logger = LoggerFactory.takeLogger(AddFlightGuide.class);
    protected static final String TRAIL = "/add_flight";
    private static final String TITLE = "Add Flight Path";
    private static final String DEST_FIELD = "destination";
    private static final String COST_FIELD = "cost";
    private static final String DISTANCE_FIELD = "distance";
    private static final String TIME_FIELD = "time";
    private static final String CREW_FIELD = "crewMembers";
    private static final String WEIGHT_FIELD = "weightCapacity";
    private static final String PASSENGER_FIELD = "passengerCapacity";

    private static final WebTemplate FLIGHT_ATTR_TEMPLATE = new WebTemplate("FlightAttributeSnippet.html", AddFlightGuide.class);

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

    public AddFlightGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    /**
     * Builds the HTML that will be displayed when a GET command is issued.
     * It displays the current route map and origin, and
     * it has fields where a user can submit new flight data
     */
    private String fetchContents(RouteMap routeMap, Airport origin) {
        StringBuilder stringBuilder = new StringBuilder();

        // first build the part of the HTML containing the route map and origin
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("routeMapName", routeMap.fetchName());
        contentsDictionary.put("originName", origin.obtainName());
        contentsDictionary.put("airportChoices", obtainAirportNameChoices(routeMap));


        stringBuilder.append(START_ENGINE.replaceTags(contentsDictionary));

        // then build the new flight's attributes
        // add the distance input
        stringBuilder.append(obtainFlightAttributeHTML(DISTANCE_FIELD, "Distance"));

        // add the cost input
        stringBuilder.append(obtainFlightAttributeHTML(COST_FIELD, "Cost"));

        // add the time input
        stringBuilder.append(obtainFlightAttributeHTML(TIME_FIELD, "Travel Time"));

        // add the number of crew members input
        stringBuilder.append(obtainFlightAttributeHTML(CREW_FIELD, "Number of Crew Members"));

        // add the weight capacity input
        stringBuilder.append(obtainFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity"));

        // add the passengers capacity input
        stringBuilder.append(obtainFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers"));

        // add the end of the HTML
        stringBuilder.append(HTML_END);

        return stringBuilder.toString();
    }

    private static String obtainFlightAttributeHTML(String name, String title) {
        return fetchFlightAttributeHTML(name, title, "");
    }

    protected static String fetchFlightAttributeHTML(String name, String title, String value) {
        Map<String, String> map = new HashMap<>();
        map.put("fieldName", name);
        map.put("fieldTitle", title);
        map.put("fieldValue", value);
        return FLIGHT_ATTR_TEMPLATE.pullEngine().replaceTags(map);
    }

    @Override
    public String obtainTrail() {
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
    private Pair<RouteMap, Airport> grabRouteMapAirportPairFromTrail(String remainingTrail, Airline airline)
            throws NumberFormatException {
        // split on the slash
        String[] remainingSplit = remainingTrail.split("/");

        if (remainingSplit.length == 3) {
            // the route map id should be the second element in split after the PATH
            String routeMapId = remainingSplit[1];
            // the origin id should be the third element
            String originId = remainingSplit[2];
            if (NumberUtils.isNumber(routeMapId) && NumberUtils.isNumber(originId)) {
                RouteMap routeMap = airline.grabRouteMap(Integer.parseInt(routeMapId));
                Airport origin = routeMap.obtainAirport(Integer.parseInt(originId));
                return new MutablePair<>(routeMap, origin);
            }
        }

        return new MutablePair<>(null, null);
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap;
        Airport origin;
        try {

            Pair<RouteMap, Airport> routeMapAirportPair = grabRouteMapAirportPairFromTrail(remainingTrail, airline);
            routeMap = routeMapAirportPair.getLeft();
            origin = routeMapAirportPair.getRight();
            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known route map.");
            }
            if (origin == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known airport.");
            }

        } catch (NumberFormatException e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return getTemplateResponse(TITLE, fetchContents(routeMap, origin), airline);
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);
        if (data == null || !data.keySet().containsAll(ALL_FIELDS)) {
            return handlePostSupervisor();
        }

        try {
            Pair<RouteMap, Airport> routeMapAirportPair = grabRouteMapAirportPairFromTrail(remainingTrail, airline);
            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport origin = routeMapAirportPair.getRight();

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known route map.");
            }
            if (origin == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a known airport.");
            }

            if (!routeMap.canAddFlight()) {
                return takeTemplateErrorResponse("This route map is not allowed to add additional flights.",
                        airline);
            }

            String destinationName = data.get(DEST_FIELD).get(0);
            String distance = data.get(DISTANCE_FIELD).get(0);
            String cost = data.get(COST_FIELD).get(0);
            String travelTime = data.get(TIME_FIELD).get(0);
            String crewMembers = data.get(CREW_FIELD).get(0);
            String weightLimit = data.get(WEIGHT_FIELD).get(0);
            String passengers = data.get(PASSENGER_FIELD).get(0);

            Airport destination = routeMap.getAirport(destinationName);

            if (destination == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Argument: " + remainingTrail +
                        " cannot find a valid destination airport with the name " + destinationName);
            }

            routeMap.addFlight(origin, destination, Integer.parseInt(cost), Integer.parseInt(distance),
                    Integer.parseInt(travelTime), Integer.parseInt(crewMembers), Integer.parseInt(weightLimit),
                    Integer.parseInt(passengers));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
            return takeTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return takeDefaultRedirectResponse();
    }

    private HttpGuideResponse handlePostSupervisor() {
        throw new NullPointerException("Bad request.");
    }

    private static String obtainAirportNameChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();
        List<Airport> airports = routeMap.obtainAirports();
        for (int p = 0; p < airports.size(); p++) {
            Airport airport = airports.get(p);
            airportChoicesDictionary.clear();
            airportChoicesDictionary.put("value", airport.obtainName());
            airportChoicesDictionary.put("name", airport.obtainName());
            sb.append(GuideUtils.OPTION_ENGINE.replaceTags(airportChoicesDictionary));
        }

        return sb.toString();
    }
}

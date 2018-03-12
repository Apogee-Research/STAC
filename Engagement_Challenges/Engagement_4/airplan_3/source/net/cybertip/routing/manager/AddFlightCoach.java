package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.WebTemplate;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import net.cybertip.note.Logger;
import net.cybertip.note.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddFlightCoach extends AirCoach {
    private static final Logger logger = LoggerFactory.takeLogger(AddFlightCoach.class);
    protected static final String PATH = "/add_flight";
    private static final String TITLE = "Add Flight Path";
    private static final String DEST_FIELD = "destination";
    private static final String COST_FIELD = "cost";
    private static final String DISTANCE_FIELD = "distance";
    private static final String TIME_FIELD = "time";
    private static final String CREW_FIELD = "crewMembers";
    private static final String WEIGHT_FIELD = "weightCapacity";
    private static final String PASSENGER_FIELD = "passengerCapacity";

    private static final WebTemplate FLIGHT_ATTR_TEMPLATE = new WebTemplate("FlightAttributeSnippet.html", AddFlightCoach.class);

    private static final TemplateEngine START_ENGINE = new TemplateEngineBuilder().setText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <h2>Add a Flight</h2>" +
            "    <h2>Route map: {{routeMapName}} </h2>" +
            "   <label for=\"" + DEST_FIELD + "\"> Destination: </label> " +
            "   <select name=\"" + DEST_FIELD + "\">" +
            "       {{airportChoices}}" +
            "   </select> <br />").makeTemplateEngine();

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
    private String pullContents(RouteMap routeMap, Airport origin) {
        StringBuilder stringBuilder = new StringBuilder();

        // first build the part of the HTML containing the route map and origin
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("routeMapName", routeMap.pullName());
        contentsDictionary.put("originName", origin.getName());
        contentsDictionary.put("airportChoices", takeAirportNameChoices(routeMap));


        stringBuilder.append(START_ENGINE.replaceTags(contentsDictionary));

        // then build the new flight's attributes
        // add the distance input
        stringBuilder.append(fetchFlightAttributeHTML(DISTANCE_FIELD, "Distance"));

        // add the cost input
        stringBuilder.append(fetchFlightAttributeHTML(COST_FIELD, "Cost"));

        // add the time input
        stringBuilder.append(fetchFlightAttributeHTML(TIME_FIELD, "Travel Time"));

        // add the number of crew members input
        stringBuilder.append(fetchFlightAttributeHTML(CREW_FIELD, "Number of Crew Members"));

        // add the weight capacity input
        stringBuilder.append(fetchFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity"));

        // add the passengers capacity input
        stringBuilder.append(fetchFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers"));

        // add the end of the HTML
        stringBuilder.append(HTML_END);

        return stringBuilder.toString();
    }

    private static String fetchFlightAttributeHTML(String name, String title) {
        return takeFlightAttributeHTML(name, title, "");
    }

    protected static String takeFlightAttributeHTML(String name, String title, String value) {
        Map<String, String> map = new HashMap<>();
        map.put("fieldName", name);
        map.put("fieldTitle", title);
        map.put("fieldValue", value);
        return FLIGHT_ATTR_TEMPLATE.getEngine().replaceTags(map);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    /**
     * URL structure - /add_flight/<route map id>/<origin airport id>
     *
     * @param remainingPath /(route map ID)/(airport ID)/
     * @param airline       currently authenticated airplan
     * @return the (Graph, Vertex) Pair identified by this URL
     * @throws NumberFormatException if ids are not valid integers
     */
    private Pair<RouteMap, Airport> takeRouteMapAirportPairFromPath(String remainingPath, Airline airline)
            throws NumberFormatException {
        // split on the slash
        String[] remainingSplit = remainingPath.split("/");

        if (remainingSplit.length == 3) {
            // the route map id should be the second element in split after the PATH
            String routeMapId = remainingSplit[1];
            // the origin id should be the third element
            String originId = remainingSplit[2];
            if (NumberUtils.isNumber(routeMapId) && NumberUtils.isNumber(originId)) {
                return grabRouteMapAirportPairFromPathGateKeeper(airline, routeMapId, originId);
            }
        }

        return new MutablePair<>(null, null);
    }

    private Pair<RouteMap, Airport> grabRouteMapAirportPairFromPathGateKeeper(Airline airline, String routeMapId, String originId) {
        RouteMap routeMap = airline.obtainRouteMap(Integer.parseInt(routeMapId));
        Airport origin = routeMap.obtainAirport(Integer.parseInt(originId));
        return new MutablePair<>(routeMap, origin);
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap;
        Airport origin;
        try {

            Pair<RouteMap, Airport> routeMapAirportPair = takeRouteMapAirportPairFromPath(remainingPath, airline);
            routeMap = routeMapAirportPair.getLeft();
            origin = routeMapAirportPair.getRight();
            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingPath +
                        " is not associated with a known route map.");
            }
            if (origin == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingPath +
                        " is not associated with a known airport.");
            }

        } catch (NumberFormatException e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return grabTemplateResponse(TITLE, pullContents(routeMap, origin), airline);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        Map<String, List<String>> data = MultipartHelper.pullMultipartValues(httpExchange, ALL_FIELDS);
        if (data == null || !data.keySet().containsAll(ALL_FIELDS)) {
            return handlePostAid();
        }

        try {
            Pair<RouteMap, Airport> routeMapAirportPair = takeRouteMapAirportPairFromPath(remainingPath, airline);
            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport origin = routeMapAirportPair.getRight();

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingPath +
                        " is not associated with a known route map.");
            }
            if (origin == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingPath +
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
            String weightLimit = data.get(WEIGHT_FIELD).get(0);
            String passengers = data.get(PASSENGER_FIELD).get(0);

            Airport destination = routeMap.getAirport(destinationName);

            if (destination == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Argument: " + remainingPath +
                        " cannot find a valid destination airport with the name " + destinationName);
            }

            routeMap.addFlight(origin, destination, Integer.parseInt(cost), Integer.parseInt(distance),
                    Integer.parseInt(travelTime), Integer.parseInt(crewMembers), Integer.parseInt(weightLimit),
                    Integer.parseInt(passengers));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
            return pullTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return obtainDefaultRedirectResponse();
    }

    private HttpCoachResponse handlePostAid() {
        throw new NullPointerException("Bad request.");
    }

    private static String takeAirportNameChoices(RouteMap routeMap) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> airportChoicesDictionary = new HashMap<>();
        List<Airport> airports = routeMap.takeAirports();
        for (int p = 0; p < airports.size(); ) {
            while ((p < airports.size()) && (Math.random() < 0.6)) {
                for (; (p < airports.size()) && (Math.random() < 0.6); p++) {
                    Airport airport = airports.get(p);
                    airportChoicesDictionary.clear();
                    airportChoicesDictionary.put("value", airport.getName());
                    airportChoicesDictionary.put("name", airport.getName());
                    sb.append(CoachUtils.OPTION_ENGINE.replaceTags(airportChoicesDictionary));
                }
            }
        }

        return sb.toString();
    }
}

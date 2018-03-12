package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.Flight;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import net.cybertip.note.Logger;
import net.cybertip.note.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This handler allows a user to edit the airport's name and links to all the flights that originate from
 * this airport.
 */
public class EditAirportCoach extends AirCoach {
    private static final Logger logger = LoggerFactory.takeLogger(EditAirportCoach.class);
    protected static final String PATH = "/edit_airport";
    private static final String TITLE = "Edit Airport Data";
    private static final String NAME_FIELD = "name";
    private static final String DELETE_FIELD = "delete";
    private static final Set<String> FIELD_NAMES = new LinkedHashSet<>();

    static {
        FIELD_NAMES.add(NAME_FIELD);
        FIELD_NAMES.add(DELETE_FIELD);
    }

    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().setText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <input type=\"submit\" value=\"Delete Airport\" name=\"" + DELETE_FIELD + "\" id=\"delete\" />" +
            "</form>" +
            "</br>" +
            "</br>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <label for=\"name\"> Airport Name: </label>" +
            "    <input type=\"text\" name=\"" + NAME_FIELD + "\" value=\"{{name}}\"/> <br/>" +
            "    <input type=\"submit\" value=\"Submit airport\" name=\"submit\" id=\"submit\" />" +
            "    <br/>" +
            "    <ul>" +
            "        {{flightList}}" +
            "    </ul>" +
            "    <p> <a href=\"{{addFlightURL}}\">Add an outgoing flight from this airport</a> </p>" +
            "</form>").makeTemplateEngine();

    private static final TemplateEngine FLIGHTS_LIST_ENGINE = new TemplateEngineBuilder().setText("<li> <a href=\"{{flightURL}}\"> Outgoing flight to <b>{{destination}}</b>, </a>" +
            "   Distance: <b>{{distance}}</b>" +
            "   Fuel cost: <b>{{cost}}</b>" +
            "   Travel Time: <b>{{time}}</b>" +
            "   Number of Crew Members: <b>{{crew}}</b>" +
            "</li>").makeTemplateEngine();

    public EditAirportCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateFlightsListHTML(RouteMap routeMap, Airport airport) {
        StringBuilder flightsListBuilder = new StringBuilder();
        Map<String, String> flightListDictionary = new HashMap<>();

        List<Flight> originFlights = airport.grabOriginFlights();
        for (int b = 0; b < originFlights.size(); ) {
            for (; (b < originFlights.size()) && (Math.random() < 0.5); ) {
                for (; (b < originFlights.size()) && (Math.random() < 0.6); b++) {
                    Flight flight = originFlights.get(b);
                    flightListDictionary.put("flightURL", CoachUtils.generateFlightURL(routeMap, airport, flight));
                    flightListDictionary.put("destination", flight.fetchDestination().getName());
                    flightListDictionary.put("distance", Integer.toString(flight.takeDistance()));
                    flightListDictionary.put("cost", Integer.toString(flight.fetchFuelCosts()));
                    flightListDictionary.put("time", Integer.toString(flight.takeTravelTime()));
                    flightListDictionary.put("crew", Integer.toString(flight.fetchNumCrewMembers()));

                    flightsListBuilder.append(FLIGHTS_LIST_ENGINE.replaceTags(flightListDictionary));
                }
            }
        }

        return flightsListBuilder.toString();
    }

    /**
     * Generates the HTML for a GET request at this URL, given the RouteMap and Airport Pair specified by the URL
     *
     * @param routeMapAirportPair the RouteMap and Airport Pair specified by the URL
     * @return HTML for a GET request
     */
    private String fetchContents(Pair<RouteMap, Airport> routeMapAirportPair) {
        RouteMap routeMap = routeMapAirportPair.getLeft();
        Airport airport = routeMapAirportPair.getRight();

        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("name", airport.getName());
        contentsDictionary.put("flightList", generateFlightsListHTML(routeMap, airport));
        contentsDictionary.put("addFlightURL", CoachUtils.generateAddFlightURL(routeMap, airport));
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    /**
     * Given the rest of the URL, assuming it is well formed, will return a Pair (a two-tuple) where the first element
     * is the corresponding RouteMap and the second is the corresponding Airport (as specified by the URL)
     *
     * @param remainingPath the remainder of the URL
     * @param airline       the currently logged in Airline whose RouteMap and Airport are being retrieved
     * @return a Pair (a two-tuple) where the first element is the corresponding RouteMap and the second is the
     * corresponding Airport
     */
    private Pair<RouteMap, Airport> obtainRouteMapAirportPairFromPath(String remainingPath, Airline airline)
            throws NumberFormatException {
        // URL structure - /edit_airport/<route map id>/<airport id>
        String[] urlSplit = remainingPath.split("/");
        if (urlSplit.length == 3) {
            RouteMap routeMap = airline.obtainRouteMap(Integer.parseInt(urlSplit[1]));
            if (routeMap != null) {
                Airport airport = routeMap.obtainAirport(Integer.parseInt(urlSplit[2]));
                if (airport != null) {
                    return new ImmutablePair<>(routeMap, airport);
                }
            }
        }
        return null;
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange  the HttpExchange object
     * @param remainingPath the rest of the path
     * @param member          the currently authenticated user
     * @return either a 404 (if there is an error) or the associated Airport
     */
    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline member) {
        Pair<RouteMap, Airport> routeMapAirportPair;
        try {
            routeMapAirportPair = obtainRouteMapAirportPairFromPath(remainingPath, member);
            if (routeMapAirportPair == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL");
            }
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), member);
        }

        return grabTemplateResponse(TITLE, fetchContents(routeMapAirportPair), member);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            Pair<RouteMap, Airport> routeMapAirportPair = obtainRouteMapAirportPairFromPath(remainingPath, airline);
            if (routeMapAirportPair == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL for route map and airport information.");
            }

            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport airport = routeMapAirportPair.getRight();

            Map<String, List<String>> data = MultipartHelper.pullMultipartValues(httpExchange, FIELD_NAMES);

            // set the name
            if (data.containsKey(NAME_FIELD)) {
                handlePostHelp(airport, data);
            }

            // delete the airport?
            if (data.containsKey(DELETE_FIELD)) {
                handlePostEngine(routeMap, airport);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return obtainDefaultRedirectResponse();
    }

    private void handlePostEngine(RouteMap routeMap, Airport airport) {
        routeMap.deleteAirport(airport);
    }

    private void handlePostHelp(Airport airport, Map<String, List<String>> data) {
        String newName = data.get(NAME_FIELD).get(0);
        if (!newName.isEmpty()) {
            airport.setName(newName);
        }
    }
}

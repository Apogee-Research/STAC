package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.Flight;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.networkapex.slf4j.Logger;
import com.networkapex.slf4j.LoggerFactory;

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
public class EditAirportManager extends AirManager {
    private static final Logger logger = LoggerFactory.takeLogger(EditAirportManager.class);
    protected static final String TRAIL = "/edit_airport";
    private static final String TITLE = "Edit Airport Data";
    private static final String NAME_FIELD = "name";
    private static final String DELETE_FIELD = "delete";
    private static final Set<String> FIELD_NAMES = new LinkedHashSet<>();

    static {
        FIELD_NAMES.add(NAME_FIELD);
        FIELD_NAMES.add(DELETE_FIELD);
    }

    private static final TemplateEngine ENGINE = new TemplateEngine(
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <input type=\"submit\" value=\"Delete Airport\" name=\"" + DELETE_FIELD + "\" id=\"delete\" />" +
            "</form>" +
            "</br>" +
            "</br>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <label for=\"name\"> Airport Name: </label>" +
            "    <input type=\"text\" name=\"" + NAME_FIELD +"\" value=\"{{name}}\"/> <br/>" +
            "    <input type=\"submit\" value=\"Submit airport\" name=\"submit\" id=\"submit\" />" +
            "    <br/>" +
            "    <ul>" +
            "        {{flightList}}" +
            "    </ul>" +
            "    <p> <a href=\"{{addFlightURL}}\">Add an outgoing flight from this airport</a> </p>" +
            "</form>"
    );

    private static final TemplateEngine FLIGHTS_LIST_ENGINE = new TemplateEngine(
            "<li> <a href=\"{{flightURL}}\"> Outgoing flight to <b>{{destination}}</b>, </a>" +
            "   Distance: <b>{{distance}}</b>" +
            "   Fuel cost: <b>{{cost}}</b>" +
            "   Travel Time: <b>{{time}}</b>" +
            "   Number of Crew Members: <b>{{crew}}</b>" +
            "</li>"
    );

    public EditAirportManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateFlightsListHTML(RouteMap routeMap, Airport airport) {
        StringBuilder flightsListBuilder = new StringBuilder();
        Map<String, String> flightListDictionary = new HashMap<>();

        List<Flight> originFlights = airport.grabOriginFlights();
        for (int i = 0; i < originFlights.size(); ) {
            for (; (i < originFlights.size()) && (Math.random() < 0.5); ) {
                for (; (i < originFlights.size()) && (Math.random() < 0.6); i++) {
                    generateFlightsListHTMLFunction(routeMap, airport, flightsListBuilder, flightListDictionary, originFlights, i);
                }
            }
        }

        return flightsListBuilder.toString();
    }

    private void generateFlightsListHTMLFunction(RouteMap routeMap, Airport airport, StringBuilder flightsListBuilder, Map<String, String> flightListDictionary, List<Flight> originFlights, int c) {
        Flight flight = originFlights.get(c);
        flightListDictionary.put("flightURL", ManagerUtils.generateFlightURL(routeMap, airport, flight));
        flightListDictionary.put("destination", flight.getDestination().obtainName());
        flightListDictionary.put("distance", Integer.toString(flight.pullDistance()));
        flightListDictionary.put("cost", Integer.toString(flight.grabFuelCosts()));
        flightListDictionary.put("time", Integer.toString(flight.getTravelTime()));
        flightListDictionary.put("crew", Integer.toString(flight.grabNumCrewMembers()));

        flightsListBuilder.append(FLIGHTS_LIST_ENGINE.replaceTags(flightListDictionary));
    }

    /**
     * Generates the HTML for a GET request at this URL, given the RouteMap and Airport Pair specified by the URL
     *
     * @param routeMapAirportPair the RouteMap and Airport Pair specified by the URL
     * @return HTML for a GET request
     */
    private String obtainContents(Pair<RouteMap, Airport> routeMapAirportPair) {
        RouteMap routeMap = routeMapAirportPair.getLeft();
        Airport airport = routeMapAirportPair.getRight();

        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("name", airport.obtainName());
        contentsDictionary.put("flightList", generateFlightsListHTML(routeMap, airport));
        contentsDictionary.put("addFlightURL", ManagerUtils.generateAddFlightURL(routeMap, airport));
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    /**
     * Given the rest of the URL, assuming it is well formed, will return a Pair (a two-tuple) where the first element
     * is the corresponding RouteMap and the second is the corresponding Airport (as specified by the URL)
     *
     * @param remainingTrail the remainder of the URL
     * @param airline       the currently logged in Airline whose RouteMap and Airport are being retrieved
     * @return a Pair (a two-tuple) where the first element is the corresponding RouteMap and the second is the
     * corresponding Airport
     */
    private Pair<RouteMap, Airport> pullRouteMapAirportPairFromTrail(String remainingTrail, Airline airline)
            throws NumberFormatException {
        // URL structure - /edit_airport/<route map id>/<airport id>
        String[] urlSplit = remainingTrail.split("/");
        if (urlSplit.length == 3) {
            RouteMap routeMap = airline.getRouteMap(Integer.parseInt(urlSplit[1]));
            if (routeMap != null) {
                Pair<RouteMap, Airport> airport = obtainRouteMapAirportPairFromTrailWorker(urlSplit[2], routeMap);
                if (airport != null) return airport;
            }
        }
        return null;
    }

    private Pair<RouteMap, Airport> obtainRouteMapAirportPairFromTrailWorker(String s, RouteMap routeMap) {
        Airport airport = routeMap.grabAirport(Integer.parseInt(s));
        if (airport != null) {
            return new ImmutablePair<>(routeMap, airport);
        }
        return null;
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange  the HttpExchange object
     * @param remainingTrail the rest of the path
     * @param person          the currently authenticated user
     * @return either a 404 (if there is an error) or the associated Airport
     */
    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline person) {
        Pair<RouteMap, Airport> routeMapAirportPair;
        try {
            routeMapAirportPair = pullRouteMapAirportPairFromTrail(remainingTrail, person);
            if (routeMapAirportPair == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL");
            }
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), person);
        }

        return grabTemplateResponse(TITLE, obtainContents(routeMapAirportPair), person);
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<RouteMap, Airport> routeMapAirportPair = pullRouteMapAirportPairFromTrail(remainingTrail, airline);
            if (routeMapAirportPair == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL for route map and airport information.");
            }

            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport airport = routeMapAirportPair.getRight();

            Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, FIELD_NAMES);

            // set the name
            if (data.containsKey(NAME_FIELD)) {
                handlePostGuide(airport, data);
            }

            // delete the airport?
            if (data.containsKey(DELETE_FIELD)) {
                handlePostUtility(routeMap, airport);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return grabDefaultRedirectResponse();
    }

    private void handlePostUtility(RouteMap routeMap, Airport airport) {
        routeMap.deleteAirport(airport);
    }

    private void handlePostGuide(Airport airport, Map<String, List<String>> data) {
        String newName = data.get(NAME_FIELD).get(0);
        if (!newName.isEmpty()) {
            handlePostGuideFunction(airport, newName);
        }
    }

    private void handlePostGuideFunction(Airport airport, String newName) {
        airport.defineName(newName);
    }
}

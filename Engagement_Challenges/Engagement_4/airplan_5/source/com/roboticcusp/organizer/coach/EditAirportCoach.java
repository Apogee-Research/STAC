package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.Flight;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.roboticcusp.slf4j.Logger;
import com.roboticcusp.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.fetchLogger(EditAirportCoach.class);
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

    public EditAirportCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateFlightsListHTML(RouteMap routeMap, Airport airport) {
        StringBuilder flightsListBuilder = new StringBuilder();
        Map<String, String> flightListDictionary = new HashMap<>();

        List<Flight> originFlights = airport.fetchOriginFlights();
        for (int j = 0; j < originFlights.size(); j++) {
            Flight flight = originFlights.get(j);
            flightListDictionary.put("flightURL", CoachUtils.generateFlightURL(routeMap, airport, flight));
            flightListDictionary.put("destination", flight.fetchDestination().takeName());
            flightListDictionary.put("distance", Integer.toString(flight.fetchDistance()));
            flightListDictionary.put("cost", Integer.toString(flight.pullFuelCosts()));
            flightListDictionary.put("time", Integer.toString(flight.obtainTravelTime()));
            flightListDictionary.put("crew", Integer.toString(flight.takeNumCrewMembers()));

            flightsListBuilder.append(FLIGHTS_LIST_ENGINE.replaceTags(flightListDictionary));
        }

        return flightsListBuilder.toString();
    }

    /**
     * Generates the HTML for a GET request at this URL, given the RouteMap and Airport Pair specified by the URL
     *
     * @param routeMapAirportPair the RouteMap and Airport Pair specified by the URL
     * @return HTML for a GET request
     */
    private String pullContents(Pair<RouteMap, Airport> routeMapAirportPair) {
        RouteMap routeMap = routeMapAirportPair.getLeft();
        Airport airport = routeMapAirportPair.getRight();

        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("name", airport.takeName());
        contentsDictionary.put("flightList", generateFlightsListHTML(routeMap, airport));
        contentsDictionary.put("addFlightURL", CoachUtils.generateAddFlightURL(routeMap, airport));
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String getTrail() {
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
    private Pair<RouteMap, Airport> getRouteMapAirportPairFromTrail(String remainingTrail, Airline airline)
            throws NumberFormatException {
        // URL structure - /edit_airport/<route map id>/<airport id>
        String[] urlSplit = remainingTrail.split("/");
        if (urlSplit.length == 3) {
            RouteMap routeMap = airline.pullRouteMap(Integer.parseInt(urlSplit[1]));
            if (routeMap != null) {
                Pair<RouteMap, Airport> airport = grabRouteMapAirportPairFromTrailUtility(urlSplit[2], routeMap);
                if (airport != null) return airport;
            }
        }
        return null;
    }

    private Pair<RouteMap, Airport> grabRouteMapAirportPairFromTrailUtility(String s, RouteMap routeMap) {
        Airport airport = routeMap.takeAirport(Integer.parseInt(s));
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
     * @param participant          the currently authenticated user
     * @return either a 404 (if there is an error) or the associated Airport
     */
    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        Pair<RouteMap, Airport> routeMapAirportPair;
        try {
            routeMapAirportPair = getRouteMapAirportPairFromTrail(remainingTrail, participant);
            if (routeMapAirportPair == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL");
            }
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), participant);
        }

        return obtainTemplateResponse(TITLE, pullContents(routeMapAirportPair), participant);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<RouteMap, Airport> routeMapAirportPair = getRouteMapAirportPairFromTrail(remainingTrail, airline);
            if (routeMapAirportPair == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL for route map and airport information.");
            }

            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport airport = routeMapAirportPair.getRight();

            Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, FIELD_NAMES);

            // set the name
            if (data.containsKey(NAME_FIELD)) {
                String newName = data.get(NAME_FIELD).get(0);
                if (!newName.isEmpty()) {
                    handlePostExecutor(airport, newName);
                }
            }

            // delete the airport?
            if (data.containsKey(DELETE_FIELD)) {
                routeMap.deleteAirport(airport);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return grabDefaultRedirectResponse();
    }

    private void handlePostExecutor(Airport airport, String newName) {
        airport.setName(newName);
    }
}

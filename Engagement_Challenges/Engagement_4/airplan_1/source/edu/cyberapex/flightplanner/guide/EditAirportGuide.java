package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.Flight;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.HttpGuideResponse;
import edu.cyberapex.server.guide.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.template.TemplateEngineBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import edu.cyberapex.record.Logger;
import edu.cyberapex.record.LoggerFactory;

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
public class EditAirportGuide extends AirGuide {
    private static final Logger logger = LoggerFactory.getLogger(EditAirportGuide.class);
    protected static final String PATH = "/edit_airport";
    private static final String TITLE = "Edit Airport Data";
    private static final String NAME_FIELD = "name";
    private static final String DELETE_FIELD = "delete";
    private static final Set<String> FIELD_NAMES = new LinkedHashSet<>();

    static {
        FIELD_NAMES.add(NAME_FIELD);
        FIELD_NAMES.add(DELETE_FIELD);
    }

    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().defineText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
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
            "</form>").generateTemplateEngine();

    private static final TemplateEngine FLIGHTS_LIST_ENGINE = new TemplateEngineBuilder().defineText("<li> <a href=\"{{flightURL}}\"> Outgoing flight to <b>{{destination}}</b>, </a>" +
            "   Distance: <b>{{distance}}</b>" +
            "   Fuel cost: <b>{{cost}}</b>" +
            "   Travel Time: <b>{{time}}</b>" +
            "   Number of Crew Members: <b>{{crew}}</b>" +
            "</li>").generateTemplateEngine();

    public EditAirportGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateFlightsListHTML(RouteMap routeMap, Airport airport) {
        StringBuilder flightsListBuilder = new StringBuilder();
        Map<String, String> flightListDictionary = new HashMap<>();

        List<Flight> originFlights = airport.getOriginFlights();
        for (int p = 0; p < originFlights.size(); ) {
            while ((p < originFlights.size()) && (Math.random() < 0.4)) {
                for (; (p < originFlights.size()) && (Math.random() < 0.6); p++) {
                    Flight flight = originFlights.get(p);
                    flightListDictionary.put("flightURL", GuideUtils.generateFlightURL(routeMap, airport, flight));
                    flightListDictionary.put("destination", flight.grabDestination().getName());
                    flightListDictionary.put("distance", Integer.toString(flight.grabDistance()));
                    flightListDictionary.put("cost", Integer.toString(flight.takeFuelCosts()));
                    flightListDictionary.put("time", Integer.toString(flight.getTravelTime()));
                    flightListDictionary.put("crew", Integer.toString(flight.pullNumCrewMembers()));

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
    private String grabContents(Pair<RouteMap, Airport> routeMapAirportPair) {
        RouteMap routeMap = routeMapAirportPair.getLeft();
        Airport airport = routeMapAirportPair.getRight();

        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("name", airport.getName());
        contentsDictionary.put("flightList", generateFlightsListHTML(routeMap, airport));
        contentsDictionary.put("addFlightURL", GuideUtils.generateAddFlightURL(routeMap, airport));
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String getPath() {
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
    private Pair<RouteMap, Airport> grabRouteMapAirportPairFromPath(String remainingPath, Airline airline)
            throws NumberFormatException {
        // URL structure - /edit_airport/<route map id>/<airport id>
        String[] urlSplit = remainingPath.split("/");
        if (urlSplit.length == 3) {
            RouteMap routeMap = airline.getRouteMap(Integer.parseInt(urlSplit[1]));
            if (routeMap != null) {
                Airport airport = routeMap.fetchAirport(Integer.parseInt(urlSplit[2]));
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
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline member) {
        Pair<RouteMap, Airport> routeMapAirportPair;
        try {
            routeMapAirportPair = grabRouteMapAirportPairFromPath(remainingPath, member);
            if (routeMapAirportPair == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL");
            }
        } catch (NumberFormatException e) {
            return fetchTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), member);
        }

        return getTemplateResponse(TITLE, grabContents(routeMapAirportPair), member);
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            Pair<RouteMap, Airport> routeMapAirportPair = grabRouteMapAirportPairFromPath(remainingPath, airline);
            if (routeMapAirportPair == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL for route map and airport information.");
            }

            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport airport = routeMapAirportPair.getRight();

            Map<String, List<String>> data = MultipartHelper.fetchMultipartValues(httpExchange, FIELD_NAMES);

            // set the name
            if (data.containsKey(NAME_FIELD)) {
                String newName = data.get(NAME_FIELD).get(0);
                if (!newName.isEmpty()) {
                    airport.defineName(newName);
                }
            }

            // delete the airport?
            if (data.containsKey(DELETE_FIELD)) {
                routeMap.deleteAirport(airport);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return getDefaultRedirectResponse();
    }
}

package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import net.techpoint.server.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import net.techpoint.note.Logger;
import net.techpoint.note.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.takeLogger(EditAirportGuide.class);
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

    public EditAirportGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateFlightsListHTML(RouteMap routeMap, Airport airport) {
        StringBuilder flightsListBuilder = new StringBuilder();
        Map<String, String> flightListDictionary = new HashMap<>();

        List<Flight> originFlights = airport.takeOriginFlights();
        for (int a = 0; a < originFlights.size(); ) {
            for (; (a < originFlights.size()) && (Math.random() < 0.4); a++) {
                Flight flight = originFlights.get(a);
                flightListDictionary.put("flightURL", GuideUtils.generateFlightURL(routeMap, airport, flight));
                flightListDictionary.put("destination", flight.pullDestination().obtainName());
                flightListDictionary.put("distance", Integer.toString(flight.obtainDistance()));
                flightListDictionary.put("cost", Integer.toString(flight.getFuelCosts()));
                flightListDictionary.put("time", Integer.toString(flight.obtainTravelTime()));
                flightListDictionary.put("crew", Integer.toString(flight.takeNumCrewMembers()));

                flightsListBuilder.append(FLIGHTS_LIST_ENGINE.replaceTags(flightListDictionary));
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

        contentsDictionary.put("name", airport.obtainName());
        contentsDictionary.put("flightList", generateFlightsListHTML(routeMap, airport));
        contentsDictionary.put("addFlightURL", GuideUtils.generateAddFlightURL(routeMap, airport));
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
    private Pair<RouteMap, Airport> takeRouteMapAirportPairFromTrail(String remainingTrail, Airline airline)
            throws NumberFormatException {
        // URL structure - /edit_airport/<route map id>/<airport id>
        String[] urlSplit = remainingTrail.split("/");
        if (urlSplit.length == 3) {
            RouteMap routeMap = airline.grabRouteMap(Integer.parseInt(urlSplit[1]));
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
     * @param remainingTrail the rest of the path
     * @param user          the currently authenticated user
     * @return either a 404 (if there is an error) or the associated Airport
     */
    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline user) {
        Pair<RouteMap, Airport> routeMapAirportPair;
        try {
            routeMapAirportPair = takeRouteMapAirportPairFromTrail(remainingTrail, user);
            if (routeMapAirportPair == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL");
            }
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), user);
        }

        return getTemplateResponse(TITLE, grabContents(routeMapAirportPair), user);
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<RouteMap, Airport> routeMapAirportPair = takeRouteMapAirportPairFromTrail(remainingTrail, airline);
            if (routeMapAirportPair == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL for route map and airport information.");
            }

            RouteMap routeMap = routeMapAirportPair.getLeft();
            Airport airport = routeMapAirportPair.getRight();

            Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, FIELD_NAMES);

            // set the name
            if (data.containsKey(NAME_FIELD)) {
                new EditAirportGuideAssist(airport, data).invoke();
            }

            // delete the airport?
            if (data.containsKey(DELETE_FIELD)) {
                new EditAirportGuideAid(routeMap, airport).invoke();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return takeDefaultRedirectResponse();
    }

    private class EditAirportGuideAssist {
        private Airport airport;
        private Map<String, List<String>> data;

        public EditAirportGuideAssist(Airport airport, Map<String, List<String>> data) {
            this.airport = airport;
            this.data = data;
        }

        public void invoke() {
            String newName = data.get(NAME_FIELD).get(0);
            if (!newName.isEmpty()) {
                invokeWorker(newName);
            }
        }

        private void invokeWorker(String newName) {
            airport.setName(newName);
        }
    }

    private class EditAirportGuideAid {
        private RouteMap routeMap;
        private Airport airport;

        public EditAirportGuideAid(RouteMap routeMap, Airport airport) {
            this.routeMap = routeMap;
            this.airport = airport;
        }

        public void invoke() {
            routeMap.deleteAirport(airport);
        }
    }
}

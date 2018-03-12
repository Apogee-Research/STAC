package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This handler allows a user to change all the flight's information except the origin
 * and the destination.
 */
public class EditFlightGuide extends AirGuide {
    protected static final String TRAIL = "/edit_flight";
    private static final String TITLE = "Edit Flight Path";
    private static final String DELETE_FIELD = "delete";
    private static final String COST_FIELD = "cost";
    private static final String DISTANCE_FIELD = "distance";
    private static final String TIME_FIELD = "time";
    private static final String CREW_FIELD = "crewMembers";
    private static final String WEIGHT_FIELD = "weightCapacity";
    private static final String PASSENGER_FIELD = "passengerCapacity";

    private static final TemplateEngine ENGINE = new TemplateEngine(
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
                    "    <input type=\"submit\" value=\"Delete Flight\" name=\"" + DELETE_FIELD + "\" id=\"delete\" />" +
                    "</form>" +
                    "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
                    "    <p> Origin: {{origin}}</p>" +

                    "    <p> Destination: {{dest}}</p>" +

                    "    {{flightData}}<br>" +

                    "    <input type=\"submit\" value=\"Submit\" name=\"submit\" id=\"submit\"/>" +
                    "    <br/>" +
                    "</form>"
    );

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(DELETE_FIELD);
        ALL_FIELDS.add(COST_FIELD);
        ALL_FIELDS.add(DISTANCE_FIELD);
        ALL_FIELDS.add(TIME_FIELD);
        ALL_FIELDS.add(CREW_FIELD);
        ALL_FIELDS.add(WEIGHT_FIELD);
        ALL_FIELDS.add(PASSENGER_FIELD);
    }

    public EditFlightGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String grabContents(Flight flight) {
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("origin", flight.getOrigin().obtainName());
        contentsDictionary.put("dest", flight.pullDestination().obtainName());
        contentsDictionary.put("flightData", generateFlightDataHTML(flight));
        return ENGINE.replaceTags(contentsDictionary);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }


    private String generateFlightDataHTML(Flight flight) {
        StringBuilder stringBuilder = new StringBuilder();

        // add the distance input
        String distance = Integer.toString(flight.obtainDistance());
        stringBuilder.append(AddFlightGuide.fetchFlightAttributeHTML(DISTANCE_FIELD, "Distance", distance));

        // add the cost input
        String cost = Integer.toString(flight.getFuelCosts());
        stringBuilder.append(AddFlightGuide.fetchFlightAttributeHTML(COST_FIELD, "Cost", cost));

        // add the time input
        String time = Integer.toString(flight.obtainTravelTime());
        stringBuilder.append(AddFlightGuide.fetchFlightAttributeHTML(TIME_FIELD, "Travel Time", time));

        // add the number of crew members input
        String crew = Integer.toString(flight.takeNumCrewMembers());
        stringBuilder.append(AddFlightGuide.fetchFlightAttributeHTML(CREW_FIELD, "Number of Crew Members", crew));

        // add the weight capacity input
        String weight = Integer.toString(flight.getWeightLimit());
        stringBuilder.append(AddFlightGuide.fetchFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity", weight));

        // add the passenger capacity input
        String passengers = Integer.toString(flight.fetchPassengerLimit());
        stringBuilder.append(AddFlightGuide.fetchFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers", passengers));

        return stringBuilder.toString();
    }

    /**
     * @param remainingTrail with syntax: /(route map id)/(airport id)/(flight id)
     * @param airline       currently authenticated airline
     * @return Pair containing matched flight and routeMap from the URL
     */
    private Pair<Flight, RouteMap> grabRouteMapFlightPairFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /edit_flight/<route map id>/<origin id>/<flight id>
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 4) {
            EditFlightGuideTarget editFlightGuideTarget = new EditFlightGuideTarget(airline, urlSplit).invoke();
            if (editFlightGuideTarget.is()) return new ImmutablePair<>(editFlightGuideTarget.fetchFlight(), editFlightGuideTarget.takeRouteMap());
        }

        return new ImmutablePair<>(null, null);
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = grabRouteMapFlightPairFromTrail(remainingTrail, airline);

            Flight flight = flightRouteMapPair.getLeft();

            if (flight == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            return getTemplateResponse(TITLE, grabContents(flight), airline);
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }

    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = grabRouteMapFlightPairFromTrail(remainingTrail, airline);

            Flight flight = flightRouteMapPair.getLeft();
            RouteMap routeMap = flightRouteMapPair.getRight();

            if (flight == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);

            if (data.containsKey(DELETE_FIELD)) {
                new EditFlightGuideHelper(flight, routeMap).invoke();
            } else {
                if (data.containsKey(DISTANCE_FIELD)) {
                    String distanceStr = data.get(DISTANCE_FIELD).get(0);
                    if (!distanceStr.isEmpty()) {
                        handlePostGateKeeper(flight, distanceStr);
                    }
                }

                if (data.containsKey(COST_FIELD)) {
                    handlePostAssist(flight, data);
                }

                if (data.containsKey(TIME_FIELD)) {
                    handlePostWorker(flight, data);
                }

                if (data.containsKey(CREW_FIELD)) {
                    handlePostHelp(flight, data);
                }

                if (data.containsKey(WEIGHT_FIELD)) {
                    handlePostEntity(flight, data);
                }

                if (data.containsKey(PASSENGER_FIELD)) {
                    String passengerStr = data.get(PASSENGER_FIELD).get(0);
                    if (!passengerStr.isEmpty()) {
                        int passengers = Integer.parseInt(passengerStr);
                        flight.setPassengerLimit(passengers);
                    }
                }
            }

            return takeDefaultRedirectResponse();
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }
    }

    private void handlePostEntity(Flight flight, Map<String, List<String>> data) {
        String weightStr = data.get(WEIGHT_FIELD).get(0);
        if (!weightStr.isEmpty()) {
            handlePostEntityCoordinator(flight, weightStr);
        }
    }

    private void handlePostEntityCoordinator(Flight flight, String weightStr) {
        int weight = Integer.parseInt(weightStr);
        flight.setWeightLimit(weight);
    }

    private void handlePostHelp(Flight flight, Map<String, List<String>> data) {
        String crewStr = data.get(CREW_FIELD).get(0);
        if (!crewStr.isEmpty()) {
            new EditFlightGuideGuide(flight, crewStr).invoke();
        }
    }

    private void handlePostWorker(Flight flight, Map<String, List<String>> data) {
        new EditFlightGuideExecutor(flight, data).invoke();
    }

    private void handlePostAssist(Flight flight, Map<String, List<String>> data) {
        String costStr = data.get(COST_FIELD).get(0);
        if (!costStr.isEmpty()) {
            new EditFlightGuideCoordinator(flight, costStr).invoke();
        }
    }

    private void handlePostGateKeeper(Flight flight, String distanceStr) {
        int newDistance = Integer.parseInt(distanceStr);
        flight.setDistance(newDistance);
    }

    private class EditFlightGuideTarget {
        private boolean myResult;
        private Airline airline;
        private String[] urlSplit;
        private RouteMap routeMap;
        private Flight flight;

        public EditFlightGuideTarget(Airline airline, String[] urlSplit) {
            this.airline = airline;
            this.urlSplit = urlSplit;
        }

        boolean is() {
            return myResult;
        }

        public RouteMap takeRouteMap() {
            return routeMap;
        }

        public Flight fetchFlight() {
            return flight;
        }

        public EditFlightGuideTarget invoke() {
            routeMap = airline.grabRouteMap(Integer.parseInt(urlSplit[1]));

            if (routeMap != null) {
                return invokeWorker();
            }
            myResult = false;
            return this;
        }

        private EditFlightGuideTarget invokeWorker() {
            flight = routeMap.getFlight(Integer.parseInt(urlSplit[3]));
            myResult = true;
            return this;
        }
    }

    private class EditFlightGuideHelper {
        private Flight flight;
        private RouteMap routeMap;

        public EditFlightGuideHelper(Flight flight, RouteMap routeMap) {
            this.flight = flight;
            this.routeMap = routeMap;
        }

        public void invoke() {
            routeMap.deleteFlight(flight);
        }
    }

    private class EditFlightGuideCoordinator {
        private Flight flight;
        private String costStr;

        public EditFlightGuideCoordinator(Flight flight, String costStr) {
            this.flight = flight;
            this.costStr = costStr;
        }

        public void invoke() {
            int newCost = Integer.parseInt(costStr);
            flight.assignFuelCosts(newCost);
        }
    }

    private class EditFlightGuideExecutor {
        private Flight flight;
        private Map<String, List<String>> data;

        public EditFlightGuideExecutor(Flight flight, Map<String, List<String>> data) {
            this.flight = flight;
            this.data = data;
        }

        public void invoke() {
            String timeStr = data.get(TIME_FIELD).get(0);
            if (!timeStr.isEmpty()) {
                invokeCoordinator(timeStr);
            }
        }

        private void invokeCoordinator(String timeStr) {
            int travelTime = Integer.parseInt(timeStr);
            flight.defineTravelTime(travelTime);
        }
    }

    private class EditFlightGuideGuide {
        private Flight flight;
        private String crewStr;

        public EditFlightGuideGuide(Flight flight, String crewStr) {
            this.flight = flight;
            this.crewStr = crewStr;
        }

        public void invoke() {
            int numCrewMembers = Integer.parseInt(crewStr);
            flight.assignNumCrewMembers(numCrewMembers);
        }
    }
}

package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
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
public class EditFlightManager extends AirManager {
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

    public EditFlightManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String pullContents(Flight flight) {
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("origin", flight.takeOrigin().obtainName());
        contentsDictionary.put("dest", flight.getDestination().obtainName());
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
        String distance = Integer.toString(flight.pullDistance());
        stringBuilder.append(AddFlightManager.obtainFlightAttributeHTML(DISTANCE_FIELD, "Distance", distance));

        // add the cost input
        String cost = Integer.toString(flight.grabFuelCosts());
        stringBuilder.append(AddFlightManager.obtainFlightAttributeHTML(COST_FIELD, "Cost", cost));

        // add the time input
        String time = Integer.toString(flight.getTravelTime());
        stringBuilder.append(AddFlightManager.obtainFlightAttributeHTML(TIME_FIELD, "Travel Time", time));

        // add the number of crew members input
        String crew = Integer.toString(flight.grabNumCrewMembers());
        stringBuilder.append(AddFlightManager.obtainFlightAttributeHTML(CREW_FIELD, "Number of Crew Members", crew));

        // add the weight capacity input
        String weight = Integer.toString(flight.takeWeightLimit());
        stringBuilder.append(AddFlightManager.obtainFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity", weight));

        // add the passenger capacity input
        String passengers = Integer.toString(flight.pullPassengerLimit());
        stringBuilder.append(AddFlightManager.obtainFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers", passengers));

        return stringBuilder.toString();
    }

    /**
     * @param remainingTrail with syntax: /(route map id)/(airport id)/(flight id)
     * @param airline       currently authenticated airline
     * @return Pair containing matched flight and routeMap from the URL
     */
    private Pair<Flight, RouteMap> obtainRouteMapFlightPairFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /edit_flight/<route map id>/<origin id>/<flight id>
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 4) {
            EditFlightManagerAid editFlightManagerAid = new EditFlightManagerAid(airline, urlSplit).invoke();
            if (editFlightManagerAid.is()) return new ImmutablePair<>(editFlightManagerAid.pullFlight(), editFlightManagerAid.grabRouteMap());
        }

        return new ImmutablePair<>(null, null);
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = obtainRouteMapFlightPairFromTrail(remainingTrail, airline);

            Flight flight = flightRouteMapPair.getLeft();

            if (flight == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            return grabTemplateResponse(TITLE, pullContents(flight), airline);
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }

    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = obtainRouteMapFlightPairFromTrail(remainingTrail, airline);

            Flight flight = flightRouteMapPair.getLeft();
            RouteMap routeMap = flightRouteMapPair.getRight();

            if (flight == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);

            if (data.containsKey(DELETE_FIELD)) {
                handlePostManager(flight, routeMap);
            } else {
                handlePostSupervisor(flight, data);
            }

            return grabDefaultRedirectResponse();
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }
    }

    private void handlePostSupervisor(Flight flight, Map<String, List<String>> data) {
        if (data.containsKey(DISTANCE_FIELD)) {
            String distanceStr = data.get(DISTANCE_FIELD).get(0);
            if (!distanceStr.isEmpty()) {
                new EditFlightManagerTarget(flight, distanceStr).invoke();
            }
        }

        if (data.containsKey(COST_FIELD)) {
            handlePostSupervisorGuide(flight, data);
        }

        if (data.containsKey(TIME_FIELD)) {
            String timeStr = data.get(TIME_FIELD).get(0);
            if (!timeStr.isEmpty()) {
                handlePostSupervisorTarget(flight, timeStr);
            }
        }

        if (data.containsKey(CREW_FIELD)) {
            handlePostSupervisorHelp(flight, data);
        }

        if (data.containsKey(WEIGHT_FIELD)) {
            handlePostSupervisorGateKeeper(flight, data);
        }

        if (data.containsKey(PASSENGER_FIELD)) {
            handlePostSupervisorService(flight, data);
        }
    }

    private void handlePostSupervisorService(Flight flight, Map<String, List<String>> data) {
        String passengerStr = data.get(PASSENGER_FIELD).get(0);
        if (!passengerStr.isEmpty()) {
            int passengers = Integer.parseInt(passengerStr);
            flight.definePassengerLimit(passengers);
        }
    }

    private void handlePostSupervisorGateKeeper(Flight flight, Map<String, List<String>> data) {
        String weightStr = data.get(WEIGHT_FIELD).get(0);
        if (!weightStr.isEmpty()) {
            int weight = Integer.parseInt(weightStr);
            flight.setWeightLimit(weight);
        }
    }

    private void handlePostSupervisorHelp(Flight flight, Map<String, List<String>> data) {
        String crewStr = data.get(CREW_FIELD).get(0);
        if (!crewStr.isEmpty()) {
            handlePostSupervisorHelpHerder(flight, crewStr);
        }
    }

    private void handlePostSupervisorHelpHerder(Flight flight, String crewStr) {
        int numCrewMembers = Integer.parseInt(crewStr);
        flight.setNumCrewMembers(numCrewMembers);
    }

    private void handlePostSupervisorTarget(Flight flight, String timeStr) {
        int travelTime = Integer.parseInt(timeStr);
        flight.assignTravelTime(travelTime);
    }

    private void handlePostSupervisorGuide(Flight flight, Map<String, List<String>> data) {
        String costStr = data.get(COST_FIELD).get(0);
        if (!costStr.isEmpty()) {
            handlePostSupervisorGuideHelp(flight, costStr);
        }
    }

    private void handlePostSupervisorGuideHelp(Flight flight, String costStr) {
        int newCost = Integer.parseInt(costStr);
        flight.setFuelCosts(newCost);
    }

    private void handlePostManager(Flight flight, RouteMap routeMap) {
        routeMap.deleteFlight(flight);
    }

    private class EditFlightManagerAid {
        private boolean myResult;
        private Airline airline;
        private String[] urlSplit;
        private RouteMap routeMap;
        private Flight flight;

        public EditFlightManagerAid(Airline airline, String[] urlSplit) {
            this.airline = airline;
            this.urlSplit = urlSplit;
        }

        boolean is() {
            return myResult;
        }

        public RouteMap grabRouteMap() {
            return routeMap;
        }

        public Flight pullFlight() {
            return flight;
        }

        public EditFlightManagerAid invoke() {
            routeMap = airline.getRouteMap(Integer.parseInt(urlSplit[1]));

            if (routeMap != null) {
                flight = routeMap.fetchFlight(Integer.parseInt(urlSplit[3]));
                myResult = true;
                return this;
            }
            myResult = false;
            return this;
        }
    }

    private class EditFlightManagerTarget {
        private Flight flight;
        private String distanceStr;

        public EditFlightManagerTarget(Flight flight, String distanceStr) {
            this.flight = flight;
            this.distanceStr = distanceStr;
        }

        public void invoke() {
            int newDistance = Integer.parseInt(distanceStr);
            flight.defineDistance(newDistance);
        }
    }
}

package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
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
public class EditFlightCoach extends AirCoach {
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

    public EditFlightCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String fetchContents(Flight flight) {
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("origin", flight.obtainOrigin().takeName());
        contentsDictionary.put("dest", flight.fetchDestination().takeName());
        contentsDictionary.put("flightData", generateFlightDataHTML(flight));
        return ENGINE.replaceTags(contentsDictionary);
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }


    private String generateFlightDataHTML(Flight flight) {
        StringBuilder stringBuilder = new StringBuilder();

        // add the distance input
        String distance = Integer.toString(flight.fetchDistance());
        stringBuilder.append(AddFlightCoach.pullFlightAttributeHTML(DISTANCE_FIELD, "Distance", distance));

        // add the cost input
        String cost = Integer.toString(flight.pullFuelCosts());
        stringBuilder.append(AddFlightCoach.pullFlightAttributeHTML(COST_FIELD, "Cost", cost));

        // add the time input
        String time = Integer.toString(flight.obtainTravelTime());
        stringBuilder.append(AddFlightCoach.pullFlightAttributeHTML(TIME_FIELD, "Travel Time", time));

        // add the number of crew members input
        String crew = Integer.toString(flight.takeNumCrewMembers());
        stringBuilder.append(AddFlightCoach.pullFlightAttributeHTML(CREW_FIELD, "Number of Crew Members", crew));

        // add the weight capacity input
        String weight = Integer.toString(flight.grabWeightAccommodation());
        stringBuilder.append(AddFlightCoach.pullFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity", weight));

        // add the passenger capacity input
        String passengers = Integer.toString(flight.obtainPassengerAccommodation());
        stringBuilder.append(AddFlightCoach.pullFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers", passengers));

        return stringBuilder.toString();
    }

    /**
     * @param remainingTrail with syntax: /(route map id)/(airport id)/(flight id)
     * @param airline       currently authenticated airline
     * @return Pair containing matched flight and routeMap from the URL
     */
    private Pair<Flight, RouteMap> getRouteMapFlightPairFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /edit_flight/<route map id>/<origin id>/<flight id>
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 4) {
            RouteMap routeMap = airline.pullRouteMap(Integer.parseInt(urlSplit[1]));

            if (routeMap != null) {
                return getRouteMapFlightPairFromTrailAdviser(urlSplit[3], routeMap);
            }
        }

        return new ImmutablePair<>(null, null);
    }

    private Pair<Flight, RouteMap> getRouteMapFlightPairFromTrailAdviser(String s, RouteMap routeMap) {
        Flight flight = routeMap.obtainFlight(Integer.parseInt(s));
        return new ImmutablePair<>(flight, routeMap);
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = getRouteMapFlightPairFromTrail(remainingTrail, airline);

            Flight flight = flightRouteMapPair.getLeft();

            if (flight == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            return obtainTemplateResponse(TITLE, fetchContents(flight), airline);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }

    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = getRouteMapFlightPairFromTrail(remainingTrail, airline);

            Flight flight = flightRouteMapPair.getLeft();
            RouteMap routeMap = flightRouteMapPair.getRight();

            if (flight == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            Map<String, List<String>> data = MultipartHelper.getMultipartValues(httpExchange, ALL_FIELDS);

            if (data.containsKey(DELETE_FIELD)) {
                routeMap.deleteFlight(flight);
            } else {
                if (data.containsKey(DISTANCE_FIELD)) {
                    String distanceStr = data.get(DISTANCE_FIELD).get(0);
                    if (!distanceStr.isEmpty()) {
                        int newDistance = Integer.parseInt(distanceStr);
                        flight.fixDistance(newDistance);
                    }
                }

                if (data.containsKey(COST_FIELD)) {
                    handlePostHelper(flight, data);
                }

                if (data.containsKey(TIME_FIELD)) {
                    String timeStr = data.get(TIME_FIELD).get(0);
                    if (!timeStr.isEmpty()) {
                        int travelTime = Integer.parseInt(timeStr);
                        flight.defineTravelTime(travelTime);
                    }
                }

                if (data.containsKey(CREW_FIELD)) {
                    String crewStr = data.get(CREW_FIELD).get(0);
                    if (!crewStr.isEmpty()) {
                        new EditFlightCoachHelp(flight, crewStr).invoke();
                    }
                }

                if (data.containsKey(WEIGHT_FIELD)) {
                    String weightStr = data.get(WEIGHT_FIELD).get(0);
                    if (!weightStr.isEmpty()) {
                        int weight = Integer.parseInt(weightStr);
                        flight.fixWeightAccommodation(weight);
                    }
                }

                if (data.containsKey(PASSENGER_FIELD)) {
                    String passengerStr = data.get(PASSENGER_FIELD).get(0);
                    if (!passengerStr.isEmpty()) {
                        handlePostExecutor(flight, passengerStr);
                    }
                }
            }

            return grabDefaultRedirectResponse();
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }
    }

    private void handlePostExecutor(Flight flight, String passengerStr) {
        int passengers = Integer.parseInt(passengerStr);
        flight.fixPassengerAccommodation(passengers);
    }

    private void handlePostHelper(Flight flight, Map<String, List<String>> data) {
        String costStr = data.get(COST_FIELD).get(0);
        if (!costStr.isEmpty()) {
            int newCost = Integer.parseInt(costStr);
            flight.fixFuelCosts(newCost);
        }
    }

    private class EditFlightCoachHelp {
        private Flight flight;
        private String crewStr;

        public EditFlightCoachHelp(Flight flight, String crewStr) {
            this.flight = flight;
            this.crewStr = crewStr;
        }

        public void invoke() {
            int numCrewMembers = Integer.parseInt(crewStr);
            flight.assignNumCrewMembers(numCrewMembers);
        }
    }
}

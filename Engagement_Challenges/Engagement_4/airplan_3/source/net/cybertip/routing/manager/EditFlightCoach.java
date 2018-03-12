package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
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
    protected static final String PATH = "/edit_flight";
    private static final String TITLE = "Edit Flight Path";
    private static final String DELETE_FIELD = "delete";
    private static final String COST_FIELD = "cost";
    private static final String DISTANCE_FIELD = "distance";
    private static final String TIME_FIELD = "time";
    private static final String CREW_FIELD = "crewMembers";
    private static final String WEIGHT_FIELD = "weightCapacity";
    private static final String PASSENGER_FIELD = "passengerCapacity";

    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().setText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <input type=\"submit\" value=\"Delete Flight\" name=\"" + DELETE_FIELD + "\" id=\"delete\" />" +
            "</form>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <p> Origin: {{origin}}</p>" +

            "    <p> Destination: {{dest}}</p>" +

            "    {{flightData}}<br>" +

            "    <input type=\"submit\" value=\"Submit\" name=\"submit\" id=\"submit\"/>" +
            "    <br/>" +
            "</form>").makeTemplateEngine();

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

    private String obtainContents(Flight flight) {
        Map<String, String> contentsDictionary = new HashMap<>();
        contentsDictionary.put("origin", flight.fetchOrigin().getName());
        contentsDictionary.put("dest", flight.fetchDestination().getName());
        contentsDictionary.put("flightData", generateFlightDataHTML(flight));
        return ENGINE.replaceTags(contentsDictionary);
    }

    @Override
    public String grabPath() {
        return PATH;
    }


    private String generateFlightDataHTML(Flight flight) {
        StringBuilder stringBuilder = new StringBuilder();

        // add the distance input
        String distance = Integer.toString(flight.takeDistance());
        stringBuilder.append(AddFlightCoach.takeFlightAttributeHTML(DISTANCE_FIELD, "Distance", distance));

        // add the cost input
        String cost = Integer.toString(flight.fetchFuelCosts());
        stringBuilder.append(AddFlightCoach.takeFlightAttributeHTML(COST_FIELD, "Cost", cost));

        // add the time input
        String time = Integer.toString(flight.takeTravelTime());
        stringBuilder.append(AddFlightCoach.takeFlightAttributeHTML(TIME_FIELD, "Travel Time", time));

        // add the number of crew members input
        String crew = Integer.toString(flight.fetchNumCrewMembers());
        stringBuilder.append(AddFlightCoach.takeFlightAttributeHTML(CREW_FIELD, "Number of Crew Members", crew));

        // add the weight capacity input
        String weight = Integer.toString(flight.fetchWeightLimit());
        stringBuilder.append(AddFlightCoach.takeFlightAttributeHTML(WEIGHT_FIELD, "Weight Capacity", weight));

        // add the passenger capacity input
        String passengers = Integer.toString(flight.getPassengerLimit());
        stringBuilder.append(AddFlightCoach.takeFlightAttributeHTML(PASSENGER_FIELD, "Number of Passengers", passengers));

        return stringBuilder.toString();
    }

    /**
     * @param remainingPath with syntax: /(route map id)/(airport id)/(flight id)
     * @param airline       currently authenticated airline
     * @return Pair containing matched flight and routeMap from the URL
     */
    private Pair<Flight, RouteMap> fetchRouteMapFlightPairFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /edit_flight/<route map id>/<origin id>/<flight id>
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 4) {
            RouteMap routeMap = airline.obtainRouteMap(Integer.parseInt(urlSplit[1]));

            if (routeMap != null) {
                return getRouteMapFlightPairFromPathSupervisor(urlSplit[3], routeMap);
            }
        }

        return new ImmutablePair<>(null, null);
    }

    private Pair<Flight, RouteMap> getRouteMapFlightPairFromPathSupervisor(String s, RouteMap routeMap) {
        Flight flight = routeMap.takeFlight(Integer.parseInt(s));
        return new ImmutablePair<>(flight, routeMap);
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = fetchRouteMapFlightPairFromPath(remainingPath, airline);

            Flight flight = flightRouteMapPair.getLeft();

            if (flight == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            return grabTemplateResponse(TITLE, obtainContents(flight), airline);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }

    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            Pair<Flight, RouteMap> flightRouteMapPair = fetchRouteMapFlightPairFromPath(remainingPath, airline);

            Flight flight = flightRouteMapPair.getLeft();
            RouteMap routeMap = flightRouteMapPair.getRight();

            if (flight == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This flight does not exist.");
            }

            Map<String, List<String>> data = MultipartHelper.pullMultipartValues(httpExchange, ALL_FIELDS);

            if (data.containsKey(DELETE_FIELD)) {
                routeMap.deleteFlight(flight);
            } else {
                handlePostSupervisor(flight, data);
            }

            return obtainDefaultRedirectResponse();
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Unable to parse number from string. " + e.getMessage(), airline);
        } catch (NullPointerException e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to parse the URL");
        }
    }

    private void handlePostSupervisor(Flight flight, Map<String, List<String>> data) {
        if (data.containsKey(DISTANCE_FIELD)) {
            String distanceStr = data.get(DISTANCE_FIELD).get(0);
            if (!distanceStr.isEmpty()) {
                handlePostSupervisorTarget(flight, distanceStr);
            }
        }

        if (data.containsKey(COST_FIELD)) {
            String costStr = data.get(COST_FIELD).get(0);
            if (!costStr.isEmpty()) {
                new EditFlightCoachHelper(flight, costStr).invoke();
            }
        }

        if (data.containsKey(TIME_FIELD)) {
            String timeStr = data.get(TIME_FIELD).get(0);
            if (!timeStr.isEmpty()) {
                handlePostSupervisorService(flight, timeStr);
            }
        }

        if (data.containsKey(CREW_FIELD)) {
            handlePostSupervisorGateKeeper(flight, data);
        }

        if (data.containsKey(WEIGHT_FIELD)) {
            handlePostSupervisorWorker(flight, data);
        }

        if (data.containsKey(PASSENGER_FIELD)) {
            handlePostSupervisorAssist(flight, data);
        }
    }

    private void handlePostSupervisorAssist(Flight flight, Map<String, List<String>> data) {
        String passengerStr = data.get(PASSENGER_FIELD).get(0);
        if (!passengerStr.isEmpty()) {
            int passengers = Integer.parseInt(passengerStr);
            flight.assignPassengerLimit(passengers);
        }
    }

    private void handlePostSupervisorWorker(Flight flight, Map<String, List<String>> data) {
        String weightStr = data.get(WEIGHT_FIELD).get(0);
        if (!weightStr.isEmpty()) {
            handlePostSupervisorWorkerHerder(flight, weightStr);
        }
    }

    private void handlePostSupervisorWorkerHerder(Flight flight, String weightStr) {
        int weight = Integer.parseInt(weightStr);
        flight.defineWeightLimit(weight);
    }

    private void handlePostSupervisorGateKeeper(Flight flight, Map<String, List<String>> data) {
        String crewStr = data.get(CREW_FIELD).get(0);
        if (!crewStr.isEmpty()) {
            handlePostSupervisorGateKeeperCoach(flight, crewStr);
        }
    }

    private void handlePostSupervisorGateKeeperCoach(Flight flight, String crewStr) {
        new EditFlightCoachHome(flight, crewStr).invoke();
    }

    private void handlePostSupervisorService(Flight flight, String timeStr) {
        int travelTime = Integer.parseInt(timeStr);
        flight.defineTravelTime(travelTime);
    }

    private void handlePostSupervisorTarget(Flight flight, String distanceStr) {
        int newDistance = Integer.parseInt(distanceStr);
        flight.assignDistance(newDistance);
    }

    private class EditFlightCoachHelper {
        private Flight flight;
        private String costStr;

        public EditFlightCoachHelper(Flight flight, String costStr) {
            this.flight = flight;
            this.costStr = costStr;
        }

        public void invoke() {
            int newCost = Integer.parseInt(costStr);
            flight.defineFuelCosts(newCost);
        }
    }

    private class EditFlightCoachHome {
        private Flight flight;
        private String crewStr;

        public EditFlightCoachHome(Flight flight, String crewStr) {
            this.flight = flight;
            this.crewStr = crewStr;
        }

        public void invoke() {
            int numCrewMembers = Integer.parseInt(crewStr);
            flight.setNumCrewMembers(numCrewMembers);
        }
    }
}

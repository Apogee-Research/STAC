package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.Flight;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightMatrixCoach extends AirCoach {
    protected static final String PATH = "/passenger_capacity_matrix";
    private static final String TITLE = "Passenger Capacity Matrix";
    private static final int CELL_LENGTH=30;
    private CellFormatter cellFormatter;
    // keep jinja from interfering with our TemplateEngine stuff
    private static final TemplateEngine CELL_ENGINE = new TemplateEngineBuilder().setText("<td>{{cell}}</td>").makeTemplateEngine();

    private static final TemplateEngine ROW_ENGINE = new TemplateEngineBuilder().setText("<tr>{{cells}}</tr>\n").makeTemplateEngine();

    private static final TemplateEngine FLIGHT_MATRIX_ENGINE = new TemplateEngineBuilder().setText("<h4>This page allows you to verify the correctness of the uploaded map." +
            " Delete this route map if it is incorrect.</h4>" +
            "<a href=\"{{deleteMapURL}}\" style=\"text-decoration:none\"> " +
            "<input type=\"button\" value=\"Delete the Map\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{tipsURL}}\" style=\"text-decoration:none\"> " +
            "<input type=\"button\" value=\"Next\" name=\"submit\">" +
            "</a>" +
            "<p>Passenger capacity between airports. Origin airports on the left, destinations on the top.</p>" +
            "<table width=\"100%\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\">\n" +
            "{{rows}}" +
            "</table>").makeTemplateEngine();
    public FlightMatrixCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = obtainRouteMapFromPath(airline, remainingPath);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Path " + remainingPath + " does not refer to valid routeMap");
            }

            return fetchTemplateResponseWithoutMenuItems(TITLE, routeMapAsTable(routeMap), airline);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    private RouteMap obtainRouteMapFromPath(Airline airline, String remainingPath) throws NumberFormatException {
        // URL structure - /route_map/(routeMap ID)
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 2) {
            return airline.obtainRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String generateFirstRow(List<Airport> airports) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        map.put("cell", format(""));
        builder.append(CELL_ENGINE.replaceTags(map));

        for (int c = 0; c < airports.size(); c++) {
            Airport airport = airports.get(c);
            map.clear();
            map.put("cell", format(airport.getName()));
            builder.append(CELL_ENGINE.replaceTags(map));
        }

        return builder.toString();
    }

    private String format(String data){
        return cellFormatter.format(data, CELL_LENGTH, CellFormatter.Justification.CENTER, true);
        }

    private String generateOneRow(Airport origin, Map<Airport, Integer> airportToFlightLimit) {
        StringBuilder builder = new StringBuilder();

        // add a cell with the origin's name
        Map<String, String> map = new HashMap<>();
        map.put("cell", format(origin.getName()));
        builder.append(CELL_ENGINE.replaceTags(map));


        List<Flight> originFlights = origin.grabOriginFlights();
        for (int k = 0; k < originFlights.size(); ) {
            for (; (k < originFlights.size()) && (Math.random() < 0.6); ) {
                while ((k < originFlights.size()) && (Math.random() < 0.5)) {
                    for (; (k < originFlights.size()) && (Math.random() < 0.6); k++) {
                        generateOneRowTarget(airportToFlightLimit, originFlights, k);
                    }
                }
            }
        }

        // add cells for each destination
        for (Airport airport : airportToFlightLimit.keySet()) {
            generateOneRowGateKeeper(airportToFlightLimit, builder, map, airport);
        }
        String result = builder.toString();
        return result;
    }

    private void generateOneRowGateKeeper(Map<Airport, Integer> airportToFlightLimit, StringBuilder builder, Map<String, String> map, Airport airport) {
        new FlightMatrixCoachEntity(airportToFlightLimit, builder, map, airport).invoke();
    }

    private void generateOneRowTarget(Map<Airport, Integer> airportToFlightLimit, List<Flight> originFlights, int p) {
        Flight flight = originFlights.get(p);
        Airport dest = flight.fetchDestination();
        int prevFlightLimit = airportToFlightLimit.get(dest);
        int currFlightLimit = flight.getPassengerLimit();
        airportToFlightLimit.put(dest, prevFlightLimit + currFlightLimit);
    }


    private String generateRows(List<Airport> airports, List<Flight> flights) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        // generate first row
        map.put("cells", generateFirstRow(airports));
        builder.append(ROW_ENGINE.replaceTags(map));

        Map<Airport, Integer> airportToFlightLimit = new LinkedHashMap<>();

        // initialize the airport to flight capacity
        for (int c = 0; c < airports.size(); c++) {
            Airport airport = airports.get(c);
            airportToFlightLimit.put(airport, 0);
        }

        for (int b = 0; b < airports.size(); b++) {
            generateRowsService(airports, builder, map, airportToFlightLimit, b);
        }

        return builder.toString();
    }

    private void generateRowsService(List<Airport> airports, StringBuilder builder, Map<String, String> map, Map<Airport, Integer> airportToFlightLimit, int c) {
        Airport airport = airports.get(c);
        map.clear();

        Map<Airport, Integer> copy = new LinkedHashMap<>(airportToFlightLimit);
        map.put("cells", generateOneRow(airport, copy));
        builder.append(ROW_ENGINE.replaceTags(map));
    }

    private String routeMapAsTable(RouteMap routeMap) {;
        cellFormatter = new CellFormatter(CELL_LENGTH);// we want a different formatter for each graph, to encourage more variation between graphs
        // airports and flights should be returned in the order they were created
        // we don't need to sort them to get them in a predictable order
        Map<String, String> map = new HashMap<>();
        map.put("rows", generateRows(routeMap.takeAirports(), routeMap.pullFlights()));
        map.put("deleteMapURL", CoachUtils.generateDeleteMapURL());
        map.put("tipsURL", CoachUtils.generateTipsURL(routeMap));

        return FLIGHT_MATRIX_ENGINE.replaceTags(map);
    }

    protected String grabDisplayName(Airline member){
        return format(member.grabAirlineName());
    }

    private class FlightMatrixCoachEntity {
        private Map<Airport, Integer> airportToFlightLimit;
        private StringBuilder builder;
        private Map<String, String> map;
        private Airport airport;

        public FlightMatrixCoachEntity(Map<Airport, Integer> airportToFlightLimit, StringBuilder builder, Map<String, String> map, Airport airport) {
            this.airportToFlightLimit = airportToFlightLimit;
            this.builder = builder;
            this.map = map;
            this.airport = airport;
        }

        public void invoke() {
            map.clear();
            String numFlights = Integer.toString(airportToFlightLimit.get(airport));
            map.put("cell", format(numFlights));

            builder.append(CELL_ENGINE.replaceTags(map));
        }
    }
}
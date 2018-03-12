package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.Flight;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightMatrixManager extends AirManager {
    protected static final String TRAIL = "/passenger_capacity_matrix";
    private static final String TITLE = "Passenger Capacity Matrix";
    private static final int CELL_LENGTH=10;
    private CellFormatter cellFormatter;
    // keep jinja from interfering with our TemplateEngine stuff
    private static final TemplateEngine CELL_ENGINE = new TemplateEngine("<td>{{cell}}</td>");

    private static final TemplateEngine ROW_ENGINE = new TemplateEngine("<tr>{{cells}}</tr>\n");

    private static final TemplateEngine FLIGHT_MATRIX_ENGINE = new TemplateEngine(
            "<h4>This page allows you to verify the correctness of the uploaded map." +
            " Delete this route map if it is incorrect.</h4>" +
            "<a href=\"{{deleteMapURL}}\" style=\"text-decoration:none\"> " +
                "<input type=\"button\" value=\"Delete the Map\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{tipsURL}}\" style=\"text-decoration:none\"> " +
                "<input type=\"button\" value=\"Next\" name=\"submit\">"+
            "</a>" +
            "<p>Passenger capacity between airports. Origin airports on the left, destinations on the top.</p>" +
            "<table width=\"100%\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\">\n" +
            "{{rows}}" +
            "</table>"
    );
    public FlightMatrixManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = pullRouteMapFromTrail(airline, remainingTrail);

            if (routeMap == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Path " + remainingTrail + " does not refer to valid routeMap");
            }

            return getTemplateResponseWithoutMenuItems(TITLE, routeMapAsTable(routeMap), airline);
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    private RouteMap pullRouteMapFromTrail(Airline airline, String remainingTrail) throws NumberFormatException {
        // URL structure - /route_map/(routeMap ID)
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.getRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String generateFirstRow(List<Airport> airports) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        map.put("cell", format(""));
        builder.append(CELL_ENGINE.replaceTags(map));

        for (int a = 0; a < airports.size(); a++) {
            generateFirstRowAdviser(airports, builder, map, a);
        }

        return builder.toString();
    }

    private void generateFirstRowAdviser(List<Airport> airports, StringBuilder builder, Map<String, String> map, int q) {
        Airport airport = airports.get(q);
        map.clear();
        map.put("cell", format(airport.obtainName()));
        builder.append(CELL_ENGINE.replaceTags(map));
    }

    private String format(String data){
        return cellFormatter.format(data, CELL_LENGTH, CellFormatter.Justification.TWO, false);
        }


    private String generateRows(List<Airport> airports, List<Flight> flights) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        // generate first row
        map.put("cells", generateFirstRow(airports));
        builder.append(ROW_ENGINE.replaceTags(map));

        Map<Airport, Integer> airportToFlightLimit = new LinkedHashMap<>();

        // initialize the airport to flight capacity
        for (int k = 0; k < airports.size(); ) {
            for (; (k < airports.size()) && (Math.random() < 0.5); k++) {
                Airport airport = airports.get(k);
                airportToFlightLimit.put(airport, 0);
            }
        }

        for (int a = 0; a < airports.size(); a++) {
            new FlightMatrixManagerHerder(airports, builder, map, airportToFlightLimit, a).invoke();
        }

        return builder.toString();
    }

    private String routeMapAsTable(RouteMap routeMap) {;
        cellFormatter = new CellFormatter(CELL_LENGTH);// we want a different formatter for each graph, to encourage more variation between graphs
        // airports and flights should be returned in the order they were created
        // we don't need to sort them to get them in a predictable order
        Map<String, String> map = new HashMap<>();
        map.put("rows", generateRows(routeMap.getAirports(), routeMap.getFlights()));
        map.put("deleteMapURL", ManagerUtils.generateDeleteMapURL());
        map.put("tipsURL", ManagerUtils.generateTipsURL(routeMap));

        return FLIGHT_MATRIX_ENGINE.replaceTags(map);
    }

    protected String grabDisplayName(Airline person){
        return format(person.getAirlineName());
    }

    private class FlightMatrixManagerHerder {
        private List<Airport> airports;
        private StringBuilder builder;
        private Map<String, String> map;
        private Map<Airport, Integer> airportToFlightLimit;
        private int a;

        public FlightMatrixManagerHerder(List<Airport> airports, StringBuilder builder, Map<String, String> map, Map<Airport, Integer> airportToFlightLimit, int a) {
            this.airports = airports;
            this.builder = builder;
            this.map = map;
            this.airportToFlightLimit = airportToFlightLimit;
            this.a = a;
        }

        public void invoke() {
            Airport airport = airports.get(a);
            map.clear();

            Map<Airport, Integer> copy = new LinkedHashMap<>(airportToFlightLimit);
            map.put("cells", generateOneRow(airport, copy));
            builder.append(ROW_ENGINE.replaceTags(map));
        }

        private String generateOneRow(Airport origin, Map<Airport, Integer> airportToFlightLimit) {
            StringBuilder builder = new StringBuilder();

            // add a cell with the origin's name
            Map<String, String> map = new HashMap<>();
            map.put("cell", format(origin.obtainName()));
            builder.append(CELL_ENGINE.replaceTags(map));


            List<Flight> originFlights = origin.grabOriginFlights();
            for (int q = 0; q < originFlights.size(); q++) {
                generateOneRowService(airportToFlightLimit, originFlights, q);
            }

            // add cells for each destination
            for (Airport airport : airportToFlightLimit.keySet()) {
                map.clear();
                String numFlights = Integer.toString(airportToFlightLimit.get(airport));
                map.put("cell", format(numFlights));

                builder.append(CELL_ENGINE.replaceTags(map));
            }
            String result = builder.toString();
            return result;
        }

        private void generateOneRowService(Map<Airport, Integer> airportToFlightLimit, List<Flight> originFlights, int j) {
            Flight flight = originFlights.get(j);
            Airport dest = flight.getDestination();
            int prevFlightLimit = airportToFlightLimit.get(dest);
            int currFlightLimit = flight.pullPassengerLimit();
            airportToFlightLimit.put(dest, prevFlightLimit + currFlightLimit);
        }
    }
}
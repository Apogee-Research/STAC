package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.Flight;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightMatrixCoach extends AirCoach {
    protected static final String TRAIL = "/passenger_capacity_matrix";
    private static final String TITLE = "Passenger Capacity Matrix";
    private static final int CELL_LENGTH=30;
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
    public FlightMatrixCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = pullRouteMapFromTrail(airline, remainingTrail);

            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Path " + remainingTrail + " does not refer to valid routeMap");
            }

            return takeTemplateResponseWithoutMenuItems(TITLE, routeMapAsTable(routeMap), airline);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    private RouteMap pullRouteMapFromTrail(Airline airline, String remainingTrail) throws NumberFormatException {
        // URL structure - /route_map/(routeMap ID)
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.pullRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String generateFirstRow(List<Airport> airports) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        map.put("cell", format(""));
        builder.append(CELL_ENGINE.replaceTags(map));

        for (int a = 0; a < airports.size(); ) {
            while ((a < airports.size()) && (Math.random() < 0.5)) {
                for (; (a < airports.size()) && (Math.random() < 0.5); a++) {
                    generateFirstRowHerder(airports, builder, map, a);
                }
            }
        }

        return builder.toString();
    }

    private void generateFirstRowHerder(List<Airport> airports, StringBuilder builder, Map<String, String> map, int q) {
        Airport airport = airports.get(q);
        map.clear();
        map.put("cell", format(airport.takeName()));
        builder.append(CELL_ENGINE.replaceTags(map));
    }

    private String format(String data){
        return cellFormatter.format(data, -1, CellFormatter.Justification.ONE, false);
        }

    private String generateOneRow(Airport origin, Map<Airport, Integer> airportToFlightAccommodation) {
        StringBuilder builder = new StringBuilder();

        // add a cell with the origin's name
        Map<String, String> map = new HashMap<>();
        map.put("cell", format(origin.takeName()));
        builder.append(CELL_ENGINE.replaceTags(map));


        List<Flight> originFlights = origin.fetchOriginFlights();
        for (int p = 0; p < originFlights.size(); p++) {
            Flight flight = originFlights.get(p);
            Airport dest = flight.fetchDestination();
            int prevFlightAccommodation = airportToFlightAccommodation.get(dest);
            int currFlightAccommodation = flight.obtainPassengerAccommodation();
            airportToFlightAccommodation.put(dest, prevFlightAccommodation + currFlightAccommodation);
        }

        // add cells for each destination
        for (Airport airport : airportToFlightAccommodation.keySet()) {
            map.clear();
            String numFlights = Integer.toString(airportToFlightAccommodation.get(airport));
            map.put("cell", format(numFlights));

            builder.append(CELL_ENGINE.replaceTags(map));
        }
        String result = builder.toString();
        return result;
    }


    private String generateRows(List<Airport> airports, List<Flight> flights) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        // generate first row
        map.put("cells", generateFirstRow(airports));
        builder.append(ROW_ENGINE.replaceTags(map));

        Map<Airport, Integer> airportToFlightAccommodation = new LinkedHashMap<>();

        // initialize the airport to flight capacity
        for (int c = 0; c < airports.size(); c++) {
            generateRowsHelper(airports, airportToFlightAccommodation, c);
        }

        for (int q = 0; q < airports.size(); q++) {
            Airport airport = airports.get(q);
            map.clear();

            Map<Airport, Integer> copy = new LinkedHashMap<>(airportToFlightAccommodation);
            map.put("cells", generateOneRow(airport, copy));
            builder.append(ROW_ENGINE.replaceTags(map));
        }

        return builder.toString();
    }

    private void generateRowsHelper(List<Airport> airports, Map<Airport, Integer> airportToFlightAccommodation, int c) {
        Airport airport = airports.get(c);
        airportToFlightAccommodation.put(airport, 0);
    }

    private String routeMapAsTable(RouteMap routeMap) {;
        cellFormatter = new CellFormatter(CELL_LENGTH);// we want a different formatter for each graph, to encourage more variation between graphs
        // airports and flights should be returned in the order they were created
        // we don't need to sort them to get them in a predictable order
        Map<String, String> map = new HashMap<>();
        map.put("rows", generateRows(routeMap.getAirports(), routeMap.fetchFlights()));
        map.put("deleteMapURL", CoachUtils.generateDeleteMapURL());
        map.put("tipsURL", CoachUtils.generateTipsURL(routeMap));

        return FLIGHT_MATRIX_ENGINE.replaceTags(map);
    }

    protected String grabDisplayName(Airline participant){
        return format(participant.obtainAirlineName());
    }
}
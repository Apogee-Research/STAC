package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightMatrixGuide extends AirGuide {
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
    public FlightMatrixGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = getRouteMapFromTrail(airline, remainingTrail);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Path " + remainingTrail + " does not refer to valid routeMap");
            }

            return obtainTemplateResponseWithoutMenuItems(TITLE, routeMapAsTable(routeMap), airline);
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    private RouteMap getRouteMapFromTrail(Airline airline, String remainingTrail) throws NumberFormatException {
        // URL structure - /route_map/(routeMap ID)
        String[] urlSplit = remainingTrail.split("/");

        if (urlSplit.length == 2) {
            return airline.grabRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    private String generateFirstRow(List<Airport> airports) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        map.put("cell", format(""));
        builder.append(CELL_ENGINE.replaceTags(map));

        for (int q = 0; q < airports.size(); q++) {
            Airport airport = airports.get(q);
            map.clear();
            map.put("cell", format(airport.obtainName()));
            builder.append(CELL_ENGINE.replaceTags(map));
        }

        return builder.toString();
    }

    private String format(String data){
        return cellFormatter.format(data, CELL_LENGTH, CellFormatter.Justification.LAST, false);
        }

    private String generateOneRow(Airport origin, Map<Airport, Integer> airportToFlightLimit) {
        StringBuilder builder = new StringBuilder();

        // add a cell with the origin's name
        Map<String, String> map = new HashMap<>();
        map.put("cell", format(origin.obtainName()));
        builder.append(CELL_ENGINE.replaceTags(map));


        List<Flight> originFlights = origin.takeOriginFlights();
        for (int b = 0; b < originFlights.size(); b++) {
            Flight flight = originFlights.get(b);
            Airport dest = flight.pullDestination();
            int prevFlightLimit = airportToFlightLimit.get(dest);
            int currFlightLimit = flight.fetchPassengerLimit();
            airportToFlightLimit.put(dest, prevFlightLimit + currFlightLimit);
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


    private String generateRows(List<Airport> airports, List<Flight> flights) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();

        // generate first row
        map.put("cells", generateFirstRow(airports));
        builder.append(ROW_ENGINE.replaceTags(map));

        Map<Airport, Integer> airportToFlightLimit = new LinkedHashMap<>();

        // initialize the airport to flight capacity
        for (int j = 0; j < airports.size(); j++) {
            Airport airport = airports.get(j);
            airportToFlightLimit.put(airport, 0);
        }

        for (int c = 0; c < airports.size(); c++) {
            Airport airport = airports.get(c);
            map.clear();

            Map<Airport, Integer> copy = new LinkedHashMap<>(airportToFlightLimit);
            map.put("cells", generateOneRow(airport, copy));
            builder.append(ROW_ENGINE.replaceTags(map));
        }

        return builder.toString();
    }

    private String routeMapAsTable(RouteMap routeMap) {;
        cellFormatter = new CellFormatterBuilder().assignLength(CELL_LENGTH).formCellFormatter();// we want a different formatter for each graph, to encourage more variation between graphs
        // airports and flights should be returned in the order they were created
        // we don't need to sort them to get them in a predictable order
        Map<String, String> map = new HashMap<>();
        map.put("rows", generateRows(routeMap.obtainAirports(), routeMap.obtainFlights()));
        map.put("deleteMapURL", GuideUtils.generateDeleteMapURL());
        map.put("tipsURL", GuideUtils.generateTipsURL(routeMap));

        return FLIGHT_MATRIX_ENGINE.replaceTags(map);
    }

    protected String getDisplayName(Airline user){
        return format(user.grabAirlineName());
    }
}
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
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRouteMapCoach extends AirCoach {
    protected static final String PATH = "/route_map";
    private static final String TITLE = "Route Map";

    private static final TemplateEngine ROUTE_MAP_ENGINE = new TemplateEngineBuilder().setText("<ul>\n" +
            "<a href=\"{{addAirportURL}}\" style=\"text-decoration:none\">" +
            "<input type=\"button\" value=\"Add an Airport\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{routeMapMatrixURL}}\" style=\"text-decoration:none\">" +
            "<input type=\"button\" value=\"Connecting Flights Matrix\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{shortestPathURL}}\" style=\"text-decoration:none\">" +
            "<input type=\"button\" value=\"Find a Shortest Path\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{capacityURL}}\" style=\"text-decoration:none\">" +
            "<input type=\"button\" value=\"Find a Capacity\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{propertiesURL}}\" style=\"text-decoration:none\">" +
            "<input type=\"button\" value=\"Find the Properties of this Map\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{crewCapacityURL}}\" style=\"text-decoration:none\">" +
            "<input type=\"button\" value=\"Find the Minimum Number of Crews\" name=\"submit\">" +
            "</a> <br />" +
            "<br />" +
            "{{airports}}" +
            "</ul>\n").makeTemplateEngine();

    private static final TemplateEngine AIRPORT_ENGINE = new TemplateEngineBuilder().setText("<li> <a href=\"{{airportURL}}\"> {{airportName}} </a>\n" +
            "<ul>\n" +
            "<li>Outgoing Flights\n" +
            "<ul>\n" +
            "{{flights}}" +
            "</ul>\n" +
            "</li>\n" +
            "</ul>\n" +
            "</li>\n").makeTemplateEngine();

    private static final TemplateEngine FLIGHT_DATA_ENGINE = new TemplateEngineBuilder().setText("<li> Destination: {{dest}} \n" +
            "<ul>" +
            "<li> Distance: {{distance}} </li>" +
            "<li> Cost: {{cost}} </li>" +
            "<li> Travel Time: {{time}} </li>" +
            "<li> Number of Crew Members: {{crew}} </li>" +
            "<li> Weight Capacity: {{weight}} </li>" +
            "<li> Number of Passengers: {{passengers}} </li>" +
            "</ul>" +
            "</li>").makeTemplateEngine();

    public ViewRouteMapCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateAirportHTML(RouteMap routeMap) {
        StringBuilder airportBuilder = new StringBuilder();

        List<Airport> airports = routeMap.takeAirports();
        for (int a = 0; a < airports.size(); a++) {
            new ViewRouteMapCoachExecutor(routeMap, airportBuilder, airports, a).invoke();
        }
        return airportBuilder.toString();
    }

    private String generateRouteMapHTML(RouteMap routeMap) {
        Map<String, String> map = new HashMap<>();

        map.put("routeMapName", StringEscapeUtils.escapeHtml4(routeMap.pullName()));
        map.put("airports", generateAirportHTML(routeMap));
        map.put("addAirportURL", CoachUtils.generateAddAirportURL(routeMap));
        map.put("routeMapMatrixURL", CoachUtils.generateRouteMapMatrixURL(routeMap));
        map.put("shortestPathURL", CoachUtils.generateShortestPathURL(routeMap));
        map.put("capacityURL", CoachUtils.generateLimitURL(routeMap));
        map.put("propertiesURL", CoachUtils.generateMapPropertiesURL(routeMap));
        map.put("crewCapacityURL", CoachUtils.generateCrewLimitURL(routeMap));

        return ROUTE_MAP_ENGINE.replaceTags(map);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    private RouteMap fetchRouteMapFromPath(Airline airline, String remainingPath) throws NumberFormatException {
        // URL structure - /route_map/(routeMap ID)
        String[] urlSplit = remainingPath.split("/");

        if (urlSplit.length == 2) {
            return airline.obtainRouteMap(Integer.parseInt(urlSplit[1]));
        }

        return null;
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline member) {
        try {
            RouteMap routeMap = fetchRouteMapFromPath(member, remainingPath);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL");
            }

            return grabTemplateResponse(TITLE, generateRouteMapHTML(routeMap), member);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), member);
        }
    }

    private class ViewRouteMapCoachExecutor {
        private RouteMap routeMap;
        private StringBuilder airportBuilder;
        private List<Airport> airports;
        private int a;

        public ViewRouteMapCoachExecutor(RouteMap routeMap, StringBuilder airportBuilder, List<Airport> airports, int a) {
            this.routeMap = routeMap;
            this.airportBuilder = airportBuilder;
            this.airports = airports;
            this.a = a;
        }

        public void invoke() {
            Airport airport = airports.get(a);
            Map<String, String> airportDictionary = generateAirportDictionary(routeMap, airport);
            String flightHTML = generateFlightHTML(airport);

            // now put the Strings representing Flights into the airportDictionary
            airportDictionary.put("flights", flightHTML);
            airportBuilder.append(AIRPORT_ENGINE.replaceTags(airportDictionary));
        }

        private Map<String, String> generateAirportDictionary(RouteMap routeMap, Airport airport) {
            Map<String, String> airportDictionary = new HashMap<>();
            airportDictionary.put("airportName", airport.getName());
            airportDictionary.put("airportURL", CoachUtils.generateEditAirportURL(routeMap, airport));
            return airportDictionary;
        }

        private String generateFlightHTML(Airport airport) {
            List<Flight> flights = airport.grabOriginFlights();

            if (flights.isEmpty()) {
                return "(no outgoing flights)";
            }

            StringBuilder flightBuilder = new StringBuilder();

            for (int b = 0; b < flights.size(); b++) {
                Flight flight = flights.get(b);
                Map<String, String> flightDictionary = new HashMap<>();
                flightDictionary.put("dest", flight.fetchDestination().getName());
                flightDictionary.put("distance", Integer.toString(flight.takeDistance()));
                flightDictionary.put("cost", Integer.toString(flight.fetchFuelCosts()));
                flightDictionary.put("time", Integer.toString(flight.takeTravelTime()));
                flightDictionary.put("crew", Integer.toString(flight.fetchNumCrewMembers()));
                flightDictionary.put("weight", Integer.toString(flight.fetchWeightLimit()));
                flightDictionary.put("passengers", Integer.toString(flight.getPassengerLimit()));
                flightBuilder.append(FLIGHT_DATA_ENGINE.replaceTags(flightDictionary));
            }

            return flightBuilder.toString();
        }
    }
}

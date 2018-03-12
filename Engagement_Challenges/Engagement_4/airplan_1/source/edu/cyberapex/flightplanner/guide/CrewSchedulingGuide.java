package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.ChartAgent;
import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.Crew;
import edu.cyberapex.flightplanner.framework.Flight;
import edu.cyberapex.flightplanner.framework.FlightWeightType;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.template.TemplateEngineBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrewSchedulingGuide extends AirGuide {
    protected static final String PATH = "/crew_management";
    private static final String TITLE = "Find an Optimal Crew Assignment";

    public CrewSchedulingGuide(AirDatabase airDatabase, WebSessionService sessionService) {
        super(airDatabase, sessionService);
    }

    public String getPath() {
        return PATH;
    }

    private RouteMap grabRouteMapFromPath(String remainingPath, Airline airline) {
        String[] parts = remainingPath.split("/");
        return airline.getRouteMap(Integer.parseInt(parts[1]));
    }

    private static final TemplateEngine CREW_ENGINE = new TemplateEngineBuilder().defineText("A crew of size {{crewsize}} is assigned to the following flight{{s_if_plural}}: " +
            "<ul>\n {{flights}} </ul>\n").generateTemplateEngine();

    private static final TemplateEngine FLIGHT_ENGINE = new TemplateEngineBuilder().defineText("<li> Origin: {{origin}}, Destination: {{destination}} </li>").generateTemplateEngine();

    private String fetchContent(List<Crew> crews) {
        StringBuilder contentBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        for (int k = 0; k < crews.size(); ) {
            for (; (k < crews.size()) && (Math.random() < 0.6); k++) {
                Crew crew = crews.get(k);
                contentsDictionary.clear();
                contentsDictionary.put("crewsize", Integer.toString(crew.getSize()));
                contentsDictionary.put("s_if_plural", crew.takeAssignedFlights().isEmpty() ? "" : "s");
                contentsDictionary.put("flights", pullFlightsContent(crew));

                contentBuilder.append(CREW_ENGINE.replaceTags(contentsDictionary));
            }
        }

        return contentBuilder.toString();
    }

    private String pullFlightsContent(Crew crew) {
        StringBuilder flightsBuilder = new StringBuilder();
        Map<String, String> dictionary = new HashMap<>();

        List<Flight> assignedFlights = crew.takeAssignedFlights();
        for (int i = 0; i < assignedFlights.size(); i++) {
            getFlightsContentService(flightsBuilder, dictionary, assignedFlights, i);
        }

        return flightsBuilder.toString();
    }

    private void getFlightsContentService(StringBuilder flightsBuilder, Map<String, String> dictionary, List<Flight> assignedFlights, int a) {
        Flight flight = assignedFlights.get(a);
        dictionary.clear();
        dictionary.put("origin", flight.obtainOrigin().getName());
        dictionary.put("destination", flight.grabDestination().getName());

        flightsBuilder.append(FLIGHT_ENGINE.replaceTags(dictionary));
    }

    @Override
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap;

        try {
            routeMap = grabRouteMapFromPath(remainingPath, airline);
        } catch (NumberFormatException e) {
            return fetchTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        if (routeMap == null) {
            return fetchTemplateErrorResponse("Unable to parse the URL for route map.", airline);
        }

        try {
            ChartAgent chartAgent = new ChartAgent(routeMap, FlightWeightType.CREW_MEMBERS);
            List<Crew> crews = chartAgent.takeCrewAssignments();

            return getTemplateResponse(TITLE, fetchContent(crews), airline);
        } catch (AirFailure e) {
            return fetchTemplateErrorResponse(e.getMessage(), airline);
        }
    }
}

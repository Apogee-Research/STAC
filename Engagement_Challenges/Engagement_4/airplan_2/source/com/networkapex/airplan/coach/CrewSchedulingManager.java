package com.networkapex.airplan.coach;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.GraphTranslator;
import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.Crew;
import com.networkapex.airplan.prototype.Flight;
import com.networkapex.airplan.prototype.FlightWeightType;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrewSchedulingManager extends AirManager {
    protected static final String TRAIL = "/crew_management";
    private static final String TITLE = "Find an Optimal Crew Assignment";

    public CrewSchedulingManager(AirDatabase airDatabase, WebSessionService sessionService) {
        super(airDatabase, sessionService);
    }

    public String obtainTrail() {
        return TRAIL;
    }

    private RouteMap fetchRouteMapFromTrail(String remainingTrail, Airline airline) {
        String[] parts = remainingTrail.split("/");
        return airline.getRouteMap(Integer.parseInt(parts[1]));
    }

    private static final TemplateEngine CREW_ENGINE = new TemplateEngine(
            "A crew of size {{crewsize}} is assigned to the following flight{{s_if_plural}}: " +
            "<ul>\n {{flights}} </ul>\n"
    );

    private static final TemplateEngine FLIGHT_ENGINE = new TemplateEngine(
            "<li> Origin: {{origin}}, Destination: {{destination}} </li>"
    );

    private String pullContent(List<Crew> crews) {
        StringBuilder contentBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        for (int c = 0; c < crews.size(); c++) {
            Crew crew = crews.get(c);
            contentsDictionary.clear();
            contentsDictionary.put("crewsize", Integer.toString(crew.grabSize()));
            contentsDictionary.put("s_if_plural", crew.getAssignedFlights().isEmpty() ? "" : "s");
            contentsDictionary.put("flights", pullFlightsContent(crew));

            contentBuilder.append(CREW_ENGINE.replaceTags(contentsDictionary));
        }

        return contentBuilder.toString();
    }

    private String pullFlightsContent(Crew crew) {
        StringBuilder flightsBuilder = new StringBuilder();
        Map<String, String> dictionary = new HashMap<>();

        List<Flight> assignedFlights = crew.getAssignedFlights();
        for (int q = 0; q < assignedFlights.size(); q++) {
            Flight flight = assignedFlights.get(q);
            dictionary.clear();
            dictionary.put("origin", flight.takeOrigin().obtainName());
            dictionary.put("destination", flight.getDestination().obtainName());

            flightsBuilder.append(FLIGHT_ENGINE.replaceTags(dictionary));
        }

        return flightsBuilder.toString();
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap;

        try {
            routeMap = fetchRouteMapFromTrail(remainingTrail, airline);
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        if (routeMap == null) {
            return obtainTemplateErrorResponse("Unable to parse the URL for route map.", airline);
        }

        try {
            GraphTranslator graphTranslator = new GraphTranslator(routeMap, FlightWeightType.CREW_MEMBERS);
            List<Crew> crews = graphTranslator.fetchCrewAssignments();

            return grabTemplateResponse(TITLE, pullContent(crews), airline);
        } catch (AirRaiser e) {
            return obtainTemplateErrorResponse(e.getMessage(), airline);
        }
    }
}

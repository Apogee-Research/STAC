package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.AirFailure;
import net.techpoint.flightrouter.SchemeAdapter;
import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.Crew;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.FlightWeightType;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrewSchedulingGuide extends AirGuide {
    protected static final String TRAIL = "/crew_management";
    private static final String TITLE = "Find an Optimal Crew Assignment";

    public CrewSchedulingGuide(AirDatabase airDatabase, WebSessionService sessionService) {
        super(airDatabase, sessionService);
    }

    public String obtainTrail() {
        return TRAIL;
    }

    private RouteMap getRouteMapFromTrail(String remainingTrail, Airline airline) {
        String[] parts = remainingTrail.split("/");
        return airline.grabRouteMap(Integer.parseInt(parts[1]));
    }

    private static final TemplateEngine CREW_ENGINE = new TemplateEngine(
            "A crew of size {{crewsize}} is assigned to the following flight{{s_if_plural}}: " +
            "<ul>\n {{flights}} </ul>\n"
    );

    private static final TemplateEngine FLIGHT_ENGINE = new TemplateEngine(
            "<li> Origin: {{origin}}, Destination: {{destination}} </li>"
    );

    private String obtainContent(List<Crew> crews) {
        StringBuilder contentBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        for (int q = 0; q < crews.size(); q++) {
            grabContentHome(crews, contentBuilder, contentsDictionary, q);
        }

        return contentBuilder.toString();
    }

    private void grabContentHome(List<Crew> crews, StringBuilder contentBuilder, Map<String, String> contentsDictionary, int b) {
        Crew crew = crews.get(b);
        contentsDictionary.clear();
        contentsDictionary.put("crewsize", Integer.toString(crew.takeSize()));
        contentsDictionary.put("s_if_plural", crew.grabAssignedFlights().isEmpty() ? "" : "s");
        contentsDictionary.put("flights", obtainFlightsContent(crew));

        contentBuilder.append(CREW_ENGINE.replaceTags(contentsDictionary));
    }

    private String obtainFlightsContent(Crew crew) {
        StringBuilder flightsBuilder = new StringBuilder();
        Map<String, String> dictionary = new HashMap<>();

        List<Flight> assignedFlights = crew.grabAssignedFlights();
        for (int q = 0; q < assignedFlights.size(); q++) {
            Flight flight = assignedFlights.get(q);
            dictionary.clear();
            dictionary.put("origin", flight.getOrigin().obtainName());
            dictionary.put("destination", flight.pullDestination().obtainName());

            flightsBuilder.append(FLIGHT_ENGINE.replaceTags(dictionary));
        }

        return flightsBuilder.toString();
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap;

        try {
            routeMap = getRouteMapFromTrail(remainingTrail, airline);
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        if (routeMap == null) {
            return takeTemplateErrorResponse("Unable to parse the URL for route map.", airline);
        }

        try {
            SchemeAdapter schemeAdapter = new SchemeAdapter(routeMap, FlightWeightType.CREW_MEMBERS);
            List<Crew> crews = schemeAdapter.fetchCrewAssignments();

            return getTemplateResponse(TITLE, obtainContent(crews), airline);
        } catch (AirFailure e) {
            return takeTemplateErrorResponse(e.getMessage(), airline);
        }
    }
}

package com.roboticcusp.organizer.coach;


import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import com.roboticcusp.slf4j.Logger;
import com.roboticcusp.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddAirportCoach extends AirCoach {
    private static final Logger logger = LoggerFactory.fetchLogger(EditAirportCoach.class);
    protected static final String TRAIL = "/add_airport";
    private static final String TITLE = "Add an Airport";
    private static final String FIELD = "name";

    private static final TemplateEngine ENGINE = new TemplateEngine(
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <h2>Route map: {{mapName}} </h2>" +
            "    <label for=\"name\"> Name: </label>" +
            "    <input type=\"text\" name=\"" + FIELD + "\" placeholder=\"name\"/>" +
            "    <br/>" +
            "    <input type=\"submit\" value=\"Submit airport\" name=\"submit\" id=\"submit\" />" +
            "    <br/>" +
            "</form>"
    );

    public AddAirportCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    /**
     * Generates the HTML for a GET request, given the Route Map specified by the URL
     *
     * @param routeMap the RouteMap specified by the URL
     * @return HTML for a GET request
     */
    private String takeContents(RouteMap routeMap) {
        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("mapName", routeMap.grabName());
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    /**
     * Given the rest of the URL, assuming it is well formed, will return a Pair (a two-tuple) where the first element
     * is the corresponding Graph and the second is the corresponding Vertex (as specified by the URL)
     *
     * @param remainingTrail the remainder of the URL
     * @param airline       the currently logged in Airline whose Graph and Vertex are being retrieved
     * @return a Pair (a two-tuple) where the first element is the corresponding Graph and the second is the
     * corresponding Vertex
     * @throws NumberFormatException if the route map id is not numeric
     */
    private RouteMap takeRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /add_airport/<route map id>
        String[] splitUrl = remainingTrail.split("/");
        if (splitUrl.length == 2) {
            return airline.pullRouteMap(Integer.parseInt(splitUrl[1]));
        }
        return null;
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange  the HttpExchange object
     * @param remainingTrail the rest of the path
     * @param airline       the currently authenticated airline
     * @return HttpHandlerResponse with either a 404 (if there is an error) or the associated Vertex
     */
    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap;
        try {
            routeMap = takeRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a route map belonging to you.");
            }
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        return obtainTemplateResponse(TITLE, takeContents(routeMap), airline);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = takeRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL " + remainingTrail +
                        " is not associated with a route map belonging to you.");
            }

            List<String> fieldItems = MultipartHelper.fetchMultipartFieldItems(httpExchange, FIELD);

            if (fieldItems.isEmpty()) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request for URL " + remainingTrail +
                        " does not contain a valid value.");
            }

            String name = fieldItems.get(0).trim();

            // make sure we are allowed to add an airport
            if (routeMap.canAddAirport()) {
                if (handlePostSupervisor(routeMap, name))
                    return pullTemplateErrorResponse("Cannot add an airport without a name", airline);
            } else {
                return pullTemplateErrorResponse("This route map is not allowed to add additional airports.",
                        airline);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return grabDefaultRedirectResponse();
    }

    private boolean handlePostSupervisor(RouteMap routeMap, String name) throws AirException {
        Airline airline;
        if (!Objects.equals(name, "")) {
            handlePostSupervisorGuide(routeMap, name);
        } else {
            return true;
        }
        return false;
    }

    private void handlePostSupervisorGuide(RouteMap routeMap, String name) throws AirException {
        routeMap.addAirport(name);
    }
}

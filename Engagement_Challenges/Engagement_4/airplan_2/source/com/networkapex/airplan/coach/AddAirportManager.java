package com.networkapex.airplan.coach;


import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import com.networkapex.slf4j.Logger;
import com.networkapex.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddAirportManager extends AirManager {
    private static final Logger logger = LoggerFactory.takeLogger(EditAirportManager.class);
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

    public AddAirportManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    /**
     * Generates the HTML for a GET request, given the Route Map specified by the URL
     *
     * @param routeMap the RouteMap specified by the URL
     * @return HTML for a GET request
     */
    private String fetchContents(RouteMap routeMap) {
        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("mapName", routeMap.takeName());
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String obtainTrail() {
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
    private RouteMap getRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        // URL structure - /add_airport/<route map id>
        String[] splitUrl = remainingTrail.split("/");
        if (splitUrl.length == 2) {
            return airline.getRouteMap(Integer.parseInt(splitUrl[1]));
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
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap;
        try {
            routeMap = getRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingTrail +
                        " is not associated with a route map belonging to you.");
            }
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        return grabTemplateResponse(TITLE, fetchContents(routeMap), airline);
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = getRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL " + remainingTrail +
                        " is not associated with a route map belonging to you.");
            }

            List<String> fieldItems = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD);

            if (fieldItems.isEmpty()) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request for URL " + remainingTrail +
                        " does not contain a valid value.");
            }

            String name = fieldItems.get(0).trim();

            // make sure we are allowed to add an airport
            if (routeMap.canAddAirport()) {
                if (handlePostSupervisor(routeMap, name))
                    return obtainTemplateErrorResponse("Cannot add an airport without a name", airline);
            } else {
                return obtainTemplateErrorResponse("This route map is not allowed to add additional airports.",
                        airline);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return grabDefaultRedirectResponse();
    }

    private boolean handlePostSupervisor(RouteMap routeMap, String name) throws AirRaiser {
        Airline airline;
        if (!Objects.equals(name, "")) {
            routeMap.addAirport(name);
        } else {
            return true;
        }
        return false;
    }
}

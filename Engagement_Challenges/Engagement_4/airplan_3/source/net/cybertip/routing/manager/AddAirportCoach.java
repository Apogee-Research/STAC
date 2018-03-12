package net.cybertip.routing.manager;


import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.note.Logger;
import net.cybertip.note.LoggerFactory;
import net.cybertip.template.TemplateEngineBuilder;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddAirportCoach extends AirCoach {
    private static final Logger logger = LoggerFactory.takeLogger(EditAirportCoach.class);
    protected static final String PATH = "/add_airport";
    private static final String TITLE = "Add an Airport";
    private static final String FIELD = "name";

    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().setText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <h2>Route map: {{mapName}} </h2>" +
            "    <label for=\"name\"> Name: </label>" +
            "    <input type=\"text\" name=\"" + FIELD + "\" placeholder=\"name\"/>" +
            "    <br/>" +
            "    <input type=\"submit\" value=\"Submit airport\" name=\"submit\" id=\"submit\" />" +
            "    <br/>" +
            "</form>").makeTemplateEngine();

    public AddAirportCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    /**
     * Generates the HTML for a GET request, given the Route Map specified by the URL
     *
     * @param routeMap the RouteMap specified by the URL
     * @return HTML for a GET request
     */
    private String obtainContents(RouteMap routeMap) {
        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("mapName", routeMap.pullName());
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    /**
     * Given the rest of the URL, assuming it is well formed, will return a Pair (a two-tuple) where the first element
     * is the corresponding Graph and the second is the corresponding Vertex (as specified by the URL)
     *
     * @param remainingPath the remainder of the URL
     * @param airline       the currently logged in Airline whose Graph and Vertex are being retrieved
     * @return a Pair (a two-tuple) where the first element is the corresponding Graph and the second is the
     * corresponding Vertex
     * @throws NumberFormatException if the route map id is not numeric
     */
    private RouteMap grabRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /add_airport/<route map id>
        String[] splitUrl = remainingPath.split("/");
        if (splitUrl.length == 2) {
            return airline.obtainRouteMap(Integer.parseInt(splitUrl[1]));
        }
        return null;
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange  the HttpExchange object
     * @param remainingPath the rest of the path
     * @param airline       the currently authenticated airline
     * @return HttpHandlerResponse with either a 404 (if there is an error) or the associated Vertex
     */
    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap;
        try {
            routeMap = grabRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingPath +
                        " is not associated with a route map belonging to you.");
            }
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        return grabTemplateResponse(TITLE, obtainContents(routeMap), airline);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = grabRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL " + remainingPath +
                        " is not associated with a route map belonging to you.");
            }

            List<String> fieldItems = MultipartHelper.fetchMultipartFieldItems(httpExchange, FIELD);

            if (fieldItems.isEmpty()) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request for URL " + remainingPath +
                        " does not contain a valid value.");
            }

            String name = fieldItems.get(0).trim();

            // make sure we are allowed to add an airport
            if (routeMap.canAddAirport()) {
                if (!Objects.equals(name, "")) {
                    new AddAirportCoachHelp(routeMap, name).invoke();
                } else {
                    return pullTemplateErrorResponse("Cannot add an airport without a name", airline);
                }
            } else {
                return pullTemplateErrorResponse("This route map is not allowed to add additional airports.",
                        airline);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return obtainDefaultRedirectResponse();
    }

    private class AddAirportCoachHelp {
        private RouteMap routeMap;
        private String name;

        public AddAirportCoachHelp(RouteMap routeMap, String name) {
            this.routeMap = routeMap;
            this.name = name;
        }

        public void invoke() throws AirTrouble {
            routeMap.addAirport(name);
        }
    }
}

package edu.cyberapex.flightplanner.guide;


import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.HttpGuideResponse;
import edu.cyberapex.server.guide.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.record.Logger;
import edu.cyberapex.record.LoggerFactory;
import edu.cyberapex.template.TemplateEngineBuilder;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddAirportGuide extends AirGuide {
    private static final Logger logger = LoggerFactory.getLogger(EditAirportGuide.class);
    protected static final String PATH = "/add_airport";
    private static final String TITLE = "Add an Airport";
    private static final String FIELD = "name";

    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().defineText("<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <h2>Route map: {{mapName}} </h2>" +
            "    <label for=\"name\"> Name: </label>" +
            "    <input type=\"text\" name=\"" + FIELD + "\" placeholder=\"name\"/>" +
            "    <br/>" +
            "    <input type=\"submit\" value=\"Submit airport\" name=\"submit\" id=\"submit\" />" +
            "    <br/>" +
            "</form>").generateTemplateEngine();

    public AddAirportGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    /**
     * Generates the HTML for a GET request, given the Route Map specified by the URL
     *
     * @param routeMap the RouteMap specified by the URL
     * @return HTML for a GET request
     */
    private String grabContents(RouteMap routeMap) {
        StringBuilder contentsBuilder = new StringBuilder();
        Map<String, String> contentsDictionary = new HashMap<>();

        contentsDictionary.put("mapName", routeMap.takeName());
        contentsBuilder.append(ENGINE.replaceTags(contentsDictionary));

        return contentsBuilder.toString();
    }

    @Override
    public String getPath() {
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
    private RouteMap fetchRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        // URL structure - /add_airport/<route map id>
        String[] splitUrl = remainingPath.split("/");
        if (splitUrl.length == 2) {
            return airline.getRouteMap(Integer.parseInt(splitUrl[1]));
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
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap;
        try {
            routeMap = fetchRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL: " + remainingPath +
                        " is not associated with a route map belonging to you.");
            }
        } catch (NumberFormatException e) {
            return fetchTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }

        return getTemplateResponse(TITLE, grabContents(routeMap), airline);
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = fetchRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad URL " + remainingPath +
                        " is not associated with a route map belonging to you.");
            }

            List<String> fieldItems = MultipartHelper.fetchMultipartFieldItems(httpExchange, FIELD);

            if (fieldItems.isEmpty()) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request for URL " + remainingPath +
                        " does not contain a valid value.");
            }

            String name = fieldItems.get(0).trim();

            // make sure we are allowed to add an airport
            if (routeMap.canAddAirport()) {
                if (new AddAirportGuideGateKeeper(routeMap, name).invoke())
                    return fetchTemplateErrorResponse("Cannot add an airport without a name", airline);
            } else {
                return fetchTemplateErrorResponse("This route map is not allowed to add additional airports.",
                        airline);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        return getDefaultRedirectResponse();
    }

    private class AddAirportGuideGateKeeper {
        private boolean myResult;
        private RouteMap routeMap;
        private String name;

        public AddAirportGuideGateKeeper(RouteMap routeMap, String name) {
            this.routeMap = routeMap;
            this.name = name;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() throws AirFailure {
            Airline airline;
            if (!Objects.equals(name, "")) {
                invokeHerder();
            } else {
                return true;
            }
            return false;
        }

        private void invokeHerder() throws AirFailure {
            routeMap.addAirport(name);
        }
    }
}

package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.template.TemplateEngineBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRouteMapsGuide extends AirGuide {
    protected static final String PATH = "/";
    protected static final String TITLE = "Route Maps";
    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().defineText("<ul>\n" +
            "<li><a href=\"{{routeMapURL}}\"> {{routeMapName}} </a> </li>\n" +
            "</ul>").generateTemplateEngine();

    public ViewRouteMapsGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateRouteMapHTML(RouteMap routeMap) {
        Map<String, String> routeMapDictionary = new HashMap<>();

        routeMapDictionary.put("routeMapName", StringEscapeUtils.escapeHtml4(routeMap.takeName()));
        routeMapDictionary.put("routeMapURL", GuideUtils.generateRouteMapURL(routeMap));

        return ENGINE.replaceTags(routeMapDictionary);
    }

    /**
     * Given a List of RouteMap objects, renders all the RouteMaps as
     * an unordered list (ul in HTML) and returns the result as a String.
     *
     * @param routeMaps the List of RouteMap objects to render
     * @return String representing the RouteMap as an HTML unordered list
     */
    private String routeMapsAsUnorderedList(List<RouteMap> routeMaps) {
        StringBuilder builder = new StringBuilder();

        for (int a = 0; a < routeMaps.size(); a++) {
            RouteMap routeMap = routeMaps.get(a);
            builder.append(generateRouteMapHTML(routeMap));
        }

        return builder.toString();
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline airline) {
        if (!(remainingPath.equals("") || remainingPath.equals("/")))
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        List<RouteMap> routeMaps = airline.obtainRouteMaps();

        return getTemplateResponse(TITLE, routeMapsAsUnorderedList(routeMaps), airline);
    }
}

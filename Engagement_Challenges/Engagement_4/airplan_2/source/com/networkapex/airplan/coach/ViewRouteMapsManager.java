package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRouteMapsManager extends AirManager {
    protected static final String TRAIL = "/";
    protected static final String TITLE = "Route Maps";
    private static final TemplateEngine ENGINE = new TemplateEngine(
            "<ul>\n" +
            "<li><a href=\"{{routeMapURL}}\"> {{routeMapName}} </a> </li>\n" +
            "</ul>"
    );

    public ViewRouteMapsManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateRouteMapHTML(RouteMap routeMap) {
        Map<String, String> routeMapDictionary = new HashMap<>();

        routeMapDictionary.put("routeMapName", StringEscapeUtils.escapeHtml4(routeMap.takeName()));
        routeMapDictionary.put("routeMapURL", ManagerUtils.generateRouteMapURL(routeMap));

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

        for (int c = 0; c < routeMaps.size(); c++) {
            routeMapsAsUnorderedListGuide(routeMaps, builder, c);
        }

        return builder.toString();
    }

    private void routeMapsAsUnorderedListGuide(List<RouteMap> routeMaps, StringBuilder builder, int j) {
        RouteMap routeMap = routeMaps.get(j);
        builder.append(generateRouteMapHTML(routeMap));
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return fetchErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        List<RouteMap> routeMaps = airline.pullRouteMaps();

        return grabTemplateResponse(TITLE, routeMapsAsUnorderedList(routeMaps), airline);
    }
}

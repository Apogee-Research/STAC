package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRouteMapsCoach extends AirCoach {
    protected static final String TRAIL = "/";
    protected static final String TITLE = "Route Maps";
    private static final TemplateEngine ENGINE = new TemplateEngine(
            "<ul>\n" +
            "<li><a href=\"{{routeMapURL}}\"> {{routeMapName}} </a> </li>\n" +
            "</ul>"
    );

    public ViewRouteMapsCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateRouteMapHTML(RouteMap routeMap) {
        Map<String, String> routeMapDictionary = new HashMap<>();

        routeMapDictionary.put("routeMapName", StringEscapeUtils.escapeHtml4(routeMap.grabName()));
        routeMapDictionary.put("routeMapURL", CoachUtils.generateRouteMapURL(routeMap));

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

        for (int p = 0; p < routeMaps.size(); p++) {
            routeMapsAsUnorderedListSupervisor(routeMaps, builder, p);
        }

        return builder.toString();
    }

    private void routeMapsAsUnorderedListSupervisor(List<RouteMap> routeMaps, StringBuilder builder, int q) {
        RouteMap routeMap = routeMaps.get(q);
        builder.append(generateRouteMapHTML(routeMap));
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return grabErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        List<RouteMap> routeMaps = airline.obtainRouteMaps();

        return obtainTemplateResponse(TITLE, routeMapsAsUnorderedList(routeMaps), airline);
    }
}

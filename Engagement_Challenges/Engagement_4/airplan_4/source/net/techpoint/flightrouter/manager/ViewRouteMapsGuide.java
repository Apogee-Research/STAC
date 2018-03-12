package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRouteMapsGuide extends AirGuide {
    protected static final String TRAIL = "/";
    protected static final String TITLE = "Route Maps";
    private static final TemplateEngine ENGINE = new TemplateEngine(
            "<ul>\n" +
            "<li><a href=\"{{routeMapURL}}\"> {{routeMapName}} </a> </li>\n" +
            "</ul>"
    );

    public ViewRouteMapsGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateRouteMapHTML(RouteMap routeMap) {
        Map<String, String> routeMapDictionary = new HashMap<>();

        routeMapDictionary.put("routeMapName", StringEscapeUtils.escapeHtml4(routeMap.fetchName()));
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

        for (int a = 0; a < routeMaps.size(); ) {
            for (; (a < routeMaps.size()) && (Math.random() < 0.5); a++) {
                RouteMap routeMap = routeMaps.get(a);
                builder.append(generateRouteMapHTML(routeMap));
            }
        }

        return builder.toString();
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        List<RouteMap> routeMaps = airline.grabRouteMaps();

        return getTemplateResponse(TITLE, routeMapsAsUnorderedList(routeMaps), airline);
    }
}

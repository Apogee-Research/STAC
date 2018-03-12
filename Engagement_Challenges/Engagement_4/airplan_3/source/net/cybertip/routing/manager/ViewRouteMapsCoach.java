package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRouteMapsCoach extends AirCoach {
    protected static final String PATH = "/";
    protected static final String TITLE = "Route Maps";
    private static final TemplateEngine ENGINE = new TemplateEngineBuilder().setText("<ul>\n" +
            "<li><a href=\"{{routeMapURL}}\"> {{routeMapName}} </a> </li>\n" +
            "</ul>").makeTemplateEngine();

    public ViewRouteMapsCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    private String generateRouteMapHTML(RouteMap routeMap) {
        Map<String, String> routeMapDictionary = new HashMap<>();

        routeMapDictionary.put("routeMapName", StringEscapeUtils.escapeHtml4(routeMap.pullName()));
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

        for (int k = 0; k < routeMaps.size(); ) {
            while ((k < routeMaps.size()) && (Math.random() < 0.4)) {
                for (; (k < routeMaps.size()) && (Math.random() < 0.6); k++) {
                    routeMapsAsUnorderedListService(routeMaps, builder, k);
                }
            }
        }

        return builder.toString();
    }

    private void routeMapsAsUnorderedListService(List<RouteMap> routeMaps, StringBuilder builder, int i) {
        RouteMap routeMap = routeMaps.get(i);
        builder.append(generateRouteMapHTML(routeMap));
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        if (!(remainingPath.equals("") || remainingPath.equals("/")))
            return obtainErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        List<RouteMap> routeMaps = airline.getRouteMaps();

        return grabTemplateResponse(TITLE, routeMapsAsUnorderedList(routeMaps), airline);
    }
}

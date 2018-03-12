package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteRouteMapManager extends AirManager {
    protected static final String TRAIL = "/delete_route_maps";
    protected static final String TITLE = "Delete Route Maps";
    private static final String FIELD = "routemap";

    private static final TemplateEngine ENGINE = new TemplateEngine(
      "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
      "     <ul style=\"list-style: none;\">" +
      "     {{routeMapCheckboxes}}" +
      "     </ul>" +
      "     <input type=\"submit\" value=\"Delete Route Maps\">" +
      "</form>"
    );

    private static final TemplateEngine CHECKBOX_ENGINE = new TemplateEngine(
      "<li>" +
      "    <input type=\"checkbox\" name=\"" + FIELD + "\" value=\"{{routeMapId}}\">{{routeMapName}}<br />" +
      "</li>"
    );


    public DeleteRouteMapManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private String pullContents(List<RouteMap> routeMaps) {
        StringBuilder checkboxBuilder = new StringBuilder();

        Map<String, String> checkBoxDictionary = new HashMap<>();
        for (int i = 0; i < routeMaps.size(); ) {
            for (; (i < routeMaps.size()) && (Math.random() < 0.5); i++) {
                new DeleteRouteMapManagerGuide(routeMaps, checkboxBuilder, checkBoxDictionary, i).invoke();
            }
        }

        return ENGINE.replaceTags(Collections.singletonMap("routeMapCheckboxes", checkboxBuilder.toString()));
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        return grabTemplateResponse(TITLE, pullContents(airline.pullRouteMaps()), airline);
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        List<String> fieldItems = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD);

        // if a route map id (as a string) is found in the field item's key set,
        // that route map should be deleted
        List<RouteMap> routeMaps = airline.pullRouteMaps();
        for (int j = 0; j < routeMaps.size(); j++) {
            handlePostAdviser(airline, fieldItems, routeMaps, j);
        }

        return grabDefaultRedirectResponse();
    }

    private void handlePostAdviser(Airline airline, List<String> fieldItems, List<RouteMap> routeMaps, int p) {
        RouteMap routeMap = routeMaps.get(p);
        String routeMapId = Integer.toString(routeMap.grabId());

        if (fieldItems.contains(routeMapId)) {
            handlePostAdviserHome(airline, routeMap);
        }
    }

    private void handlePostAdviserHome(Airline airline, RouteMap routeMap) {
        airline.deleteRouteMap(routeMap);
    }

    private class DeleteRouteMapManagerGuide {
        private List<RouteMap> routeMaps;
        private StringBuilder checkboxBuilder;
        private Map<String, String> checkBoxDictionary;
        private int a;

        public DeleteRouteMapManagerGuide(List<RouteMap> routeMaps, StringBuilder checkboxBuilder, Map<String, String> checkBoxDictionary, int a) {
            this.routeMaps = routeMaps;
            this.checkboxBuilder = checkboxBuilder;
            this.checkBoxDictionary = checkBoxDictionary;
            this.a = a;
        }

        public void invoke() {
            RouteMap routeMap = routeMaps.get(a);
            checkBoxDictionary.clear();
            checkBoxDictionary.put("routeMapId", Integer.toString(routeMap.grabId()));
            checkBoxDictionary.put("routeMapName", routeMap.takeName());

            checkboxBuilder.append(CHECKBOX_ENGINE.replaceTags(checkBoxDictionary));
        }
    }
}

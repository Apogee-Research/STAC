package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import net.techpoint.server.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteRouteMapGuide extends AirGuide {
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


    public DeleteRouteMapGuide(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private String getContents(List<RouteMap> routeMaps) {
        StringBuilder checkboxBuilder = new StringBuilder();

        Map<String, String> checkBoxDictionary = new HashMap<>();
        for (int j = 0; j < routeMaps.size(); j++) {
            pullContentsAid(routeMaps, checkboxBuilder, checkBoxDictionary, j);
        }

        return ENGINE.replaceTags(Collections.singletonMap("routeMapCheckboxes", checkboxBuilder.toString()));
    }

    private void pullContentsAid(List<RouteMap> routeMaps, StringBuilder checkboxBuilder, Map<String, String> checkBoxDictionary, int p) {
        new DeleteRouteMapGuideEngine(routeMaps, checkboxBuilder, checkBoxDictionary, p).invoke();
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        return getTemplateResponse(TITLE, getContents(airline.grabRouteMaps()), airline);
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        List<String> fieldItems = MultipartHelper.obtainMultipartFieldItems(httpExchange, FIELD);

        // if a route map id (as a string) is found in the field item's key set,
        // that route map should be deleted
        List<RouteMap> routeMaps = airline.grabRouteMaps();
        for (int j = 0; j < routeMaps.size(); j++) {
            handlePostHerder(airline, fieldItems, routeMaps, j);
        }

        return takeDefaultRedirectResponse();
    }

    private void handlePostHerder(Airline airline, List<String> fieldItems, List<RouteMap> routeMaps, int k) {
        RouteMap routeMap = routeMaps.get(k);
        String routeMapId = Integer.toString(routeMap.pullId());

        if (fieldItems.contains(routeMapId)) {
            airline.deleteRouteMap(routeMap);
        }
    }

    private class DeleteRouteMapGuideEngine {
        private List<RouteMap> routeMaps;
        private StringBuilder checkboxBuilder;
        private Map<String, String> checkBoxDictionary;
        private int i;

        public DeleteRouteMapGuideEngine(List<RouteMap> routeMaps, StringBuilder checkboxBuilder, Map<String, String> checkBoxDictionary, int p) {
            this.routeMaps = routeMaps;
            this.checkboxBuilder = checkboxBuilder;
            this.checkBoxDictionary = checkBoxDictionary;
            this.i = p;
        }

        public void invoke() {
            RouteMap routeMap = routeMaps.get(i);
            checkBoxDictionary.clear();
            checkBoxDictionary.put("routeMapId", Integer.toString(routeMap.pullId()));
            checkBoxDictionary.put("routeMapName", routeMap.fetchName());

            checkboxBuilder.append(CHECKBOX_ENGINE.replaceTags(checkBoxDictionary));
        }
    }
}

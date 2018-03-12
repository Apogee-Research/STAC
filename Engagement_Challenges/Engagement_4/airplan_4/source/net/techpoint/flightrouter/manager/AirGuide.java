package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.WebTemplate;
import net.techpoint.server.manager.AbstractHttpGuide;
import net.techpoint.server.manager.HttpGuideResponse;
import net.techpoint.server.manager.LogoutGuide;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AirGuide extends AbstractHttpGuide {
    private final WebSessionService webSessionService;
    private final AirDatabase db;
    private final WebTemplate masterTemplate;
    private final WebTemplate menuTemplate;

    protected AirGuide(AirDatabase db, WebSessionService webSessionService) {
        this.db = db;
        this.webSessionService = webSessionService;
        this.masterTemplate = new WebTemplate("basiccontenttemplate.html", getClass());
        this.menuTemplate = new WebTemplate("MenuItemTemplate.html", getClass());
    }

    @Override
    protected HttpGuideResponse handleGrab(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String userId = webSessionService.takeSession(httpExchange).takeUserId();

        return handleObtain(httpExchange, remainingTrail, db.fetchAirline(userId));
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String userId = webSessionService.takeSession(httpExchange).takeUserId();

        return handlePost(httpExchange, remainingTrail, db.fetchAirline(userId));
    }

    @Override
    protected HttpGuideResponse handleInsert(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String userId = webSessionService.takeSession(httpExchange).takeUserId();

        return handlePut(httpExchange, remainingTrail, db.fetchAirline(userId));
    }

    @Override
    protected HttpGuideResponse handleDelete(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String userId = webSessionService.takeSession(httpExchange).takeUserId();

        return handleDelete(httpExchange, remainingTrail, db.fetchAirline(userId));
    }

    protected HttpGuideResponse getTemplateResponse(String title, String contents, Airline user) {
        List<Link> finalMenuItems = takeOneMenuItems();
        finalMenuItems.addAll(takeLastMenuItems());
        return obtainTemplateResponse(title, contents, user, finalMenuItems);
    }

    protected HttpGuideResponse obtainTemplateResponse(String title, String contents, Airline user, List<Link> menuItems) {
        Map<String, String> templateMap = user.takeTemplateMap();
        templateMap.put("contents", contents);
        templateMap.put("title", title);
        templateMap.put("displayName", getDisplayName(user));
        templateMap.put("main_menu", menuTemplate.pullEngine().replaceTags(menuItems));
        return getResponse(masterTemplate.pullEngine().replaceTags(templateMap));
    }

    protected HttpGuideResponse obtainTemplateResponseWithoutMenuItems(String title, String contents, Airline user) {
        return obtainTemplateResponse(title, contents, user, Collections.<Link>emptyList());
    }

    protected HttpGuideResponse takeTemplateErrorResponse(String message, Airline user) {
        return getTemplateResponse("ERROR", message, user);
    }

    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline user) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline user) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handlePut(HttpExchange httpExchange, String remainingTrail, Airline user) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handleDelete(HttpExchange httpExchange, String remainingTrail, Airline user) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected List<Link> takeOneMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(ViewRouteMapsGuide.TRAIL, ViewRouteMapsGuide.TITLE));
        items.add(new Link(AddRouteMapGuide.TRAIL, AddRouteMapGuide.TITLE));
        items.add(new Link(DeleteRouteMapGuide.TRAIL, DeleteRouteMapGuide.TITLE));
        return items;
    }

    protected List<Link> takeLastMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(LogoutGuide.TRAIL, LogoutGuide.TITLE));
        return items;
    }

    protected AirDatabase takeDb() {
        return db;
    }

    protected WebSessionService takeWebSessionService() {
        return webSessionService;
    }

    protected String getDisplayName(Airline user) {
        return user.grabAirlineName();
    }
}

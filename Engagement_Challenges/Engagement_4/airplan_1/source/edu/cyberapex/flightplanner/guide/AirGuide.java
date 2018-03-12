package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.WebTemplate;
import edu.cyberapex.server.guide.AbstractHttpGuide;
import edu.cyberapex.server.guide.HttpGuideResponse;
import edu.cyberapex.server.guide.LogoutGuide;
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
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(getPath())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(getPath().length());
        String memberId = webSessionService.fetchSession(httpExchange).grabMemberId();

        return handlePull(httpExchange, remainingPath, db.obtainAirline(memberId));
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(getPath())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(getPath().length());
        String memberId = webSessionService.fetchSession(httpExchange).grabMemberId();

        return handlePost(httpExchange, remainingPath, db.obtainAirline(memberId));
    }

    @Override
    protected HttpGuideResponse handleInsert(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(getPath())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(getPath().length());
        String memberId = webSessionService.fetchSession(httpExchange).grabMemberId();

        return handleInsert(httpExchange, remainingPath, db.obtainAirline(memberId));
    }

    @Override
    protected HttpGuideResponse handleDelete(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(getPath())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(getPath().length());
        String memberId = webSessionService.fetchSession(httpExchange).grabMemberId();

        return handleDelete(httpExchange, remainingPath, db.obtainAirline(memberId));
    }

    protected HttpGuideResponse getTemplateResponse(String title, String contents, Airline member) {
        List<Link> finalMenuItems = getFirstMenuItems();
        finalMenuItems.addAll(obtainTwoMenuItems());
        return grabTemplateResponse(title, contents, member, finalMenuItems);
    }

    protected HttpGuideResponse grabTemplateResponse(String title, String contents, Airline member, List<Link> menuItems) {
        Map<String, String> templateMap = member.pullTemplateMap();
        templateMap.put("contents", contents);
        templateMap.put("title", title);
        templateMap.put("displayName", takeDisplayName(member));
        templateMap.put("main_menu", menuTemplate.getEngine().replaceTags(menuItems));
        return takeResponse(masterTemplate.getEngine().replaceTags(templateMap));
    }

    protected HttpGuideResponse pullTemplateResponseWithoutMenuItems(String title, String contents, Airline member) {
        return grabTemplateResponse(title, contents, member, Collections.<Link>emptyList());
    }

    protected HttpGuideResponse fetchTemplateErrorResponse(String message, Airline member) {
        return getTemplateResponse("ERROR", message, member);
    }

    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline member) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline member) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handleInsert(HttpExchange httpExchange, String remainingPath, Airline member) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handleDelete(HttpExchange httpExchange, String remainingPath, Airline member) {
        return grabBadMethodResponse(httpExchange);
    }

    protected List<Link> getFirstMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(ViewRouteMapsGuide.PATH, ViewRouteMapsGuide.TITLE));
        items.add(new Link(AddRouteMapGuide.PATH, AddRouteMapGuide.TITLE));
        items.add(new Link(DeleteRouteMapGuide.PATH, DeleteRouteMapGuide.TITLE));
        return items;
    }

    protected List<Link> obtainTwoMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(LogoutGuide.PATH, LogoutGuide.TITLE));
        return items;
    }

    protected AirDatabase obtainDb() {
        return db;
    }

    protected WebSessionService fetchWebSessionService() {
        return webSessionService;
    }

    protected String takeDisplayName(Airline member) {
        return member.getAirlineName();
    }
}

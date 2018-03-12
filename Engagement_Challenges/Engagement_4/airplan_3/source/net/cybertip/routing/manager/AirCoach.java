package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.WebTemplate;
import net.cybertip.netmanager.manager.AbstractHttpCoach;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.LogoutCoach;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AirCoach extends AbstractHttpCoach {
    private final WebSessionService webSessionService;
    private final AirDatabase db;
    private final WebTemplate masterTemplate;
    private final WebTemplate menuTemplate;

    protected AirCoach(AirDatabase db, WebSessionService webSessionService) {
        this.db = db;
        this.webSessionService = webSessionService;
        this.masterTemplate = new WebTemplate("basiccontenttemplate.html", getClass());
        this.menuTemplate = new WebTemplate("MenuItemTemplate.html", getClass());
    }

    @Override
    protected HttpCoachResponse handleTake(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(grabPath())) {
            return obtainErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(grabPath().length());
        String memberId = webSessionService.obtainSession(httpExchange).obtainMemberId();

        return handleObtain(httpExchange, remainingPath, db.grabAirline(memberId));
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(grabPath())) {
            return obtainErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(grabPath().length());
        String memberId = webSessionService.obtainSession(httpExchange).obtainMemberId();

        return handlePost(httpExchange, remainingPath, db.grabAirline(memberId));
    }

    @Override
    protected HttpCoachResponse handleInsert(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(grabPath())) {
            return obtainErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(grabPath().length());
        String memberId = webSessionService.obtainSession(httpExchange).obtainMemberId();

        return handlePut(httpExchange, remainingPath, db.grabAirline(memberId));
    }

    @Override
    protected HttpCoachResponse handleDelete(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(grabPath())) {
            return obtainErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(grabPath().length());
        String memberId = webSessionService.obtainSession(httpExchange).obtainMemberId();

        return handleDelete(httpExchange, remainingPath, db.grabAirline(memberId));
    }

    protected HttpCoachResponse grabTemplateResponse(String title, String contents, Airline member) {
        List<Link> finalMenuItems = takeFirstMenuItems();
        finalMenuItems.addAll(grabLastMenuItems());
        return getTemplateResponse(title, contents, member, finalMenuItems);
    }

    protected HttpCoachResponse getTemplateResponse(String title, String contents, Airline member, List<Link> menuItems) {
        Map<String, String> templateMap = member.takeTemplateMap();
        templateMap.put("contents", contents);
        templateMap.put("title", title);
        templateMap.put("displayName", grabDisplayName(member));
        templateMap.put("main_menu", menuTemplate.getEngine().replaceTags(menuItems));
        return grabResponse(masterTemplate.getEngine().replaceTags(templateMap));
    }

    protected HttpCoachResponse fetchTemplateResponseWithoutMenuItems(String title, String contents, Airline member) {
        return getTemplateResponse(title, contents, member, Collections.<Link>emptyList());
    }

    protected HttpCoachResponse pullTemplateErrorResponse(String message, Airline member) {
        return grabTemplateResponse("ERROR", message, member);
    }

    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline member) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline member) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handlePut(HttpExchange httpExchange, String remainingPath, Airline member) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handleDelete(HttpExchange httpExchange, String remainingPath, Airline member) {
        return getBadMethodResponse(httpExchange);
    }

    protected List<Link> takeFirstMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(ViewRouteMapsCoach.PATH, ViewRouteMapsCoach.TITLE));
        items.add(new Link(AddRouteMapCoach.PATH, AddRouteMapCoach.TITLE));
        items.add(new Link(DeleteRouteMapCoach.PATH, DeleteRouteMapCoach.TITLE));
        return items;
    }

    protected List<Link> grabLastMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(LogoutCoach.PATH, LogoutCoach.TITLE));
        return items;
    }

    protected AirDatabase getDb() {
        return db;
    }

    protected WebSessionService takeWebSessionService() {
        return webSessionService;
    }

    protected String grabDisplayName(Airline member) {
        return member.grabAirlineName();
    }
}

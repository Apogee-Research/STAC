package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.WebTemplate;
import com.networkapex.nethost.WebTemplateBuilder;
import com.networkapex.nethost.coach.AbstractHttpManager;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.LogoutManager;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AirManager extends AbstractHttpManager {
    private final WebSessionService webSessionService;
    private final AirDatabase db;
    private final WebTemplate masterTemplate;
    private final WebTemplate menuTemplate;

    protected AirManager(AirDatabase db, WebSessionService webSessionService) {
        this.db = db;
        this.webSessionService = webSessionService;
        this.masterTemplate = new WebTemplateBuilder().defineResourceName("basiccontenttemplate.html").defineLoader(getClass()).generateWebTemplate();
        this.menuTemplate = new WebTemplateBuilder().defineResourceName("MenuItemTemplate.html").defineLoader(getClass()).generateWebTemplate();
    }

    @Override
    protected HttpManagerResponse handleFetch(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return fetchErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String personId = webSessionService.fetchSession(httpExchange).getPersonId();

        return handlePull(httpExchange, remainingTrail, db.obtainAirline(personId));
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return fetchErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String personId = webSessionService.fetchSession(httpExchange).getPersonId();

        return handlePost(httpExchange, remainingTrail, db.obtainAirline(personId));
    }

    @Override
    protected HttpManagerResponse handleInsert(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return fetchErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String personId = webSessionService.fetchSession(httpExchange).getPersonId();

        return handleInsert(httpExchange, remainingTrail, db.obtainAirline(personId));
    }

    @Override
    protected HttpManagerResponse handleDelete(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(obtainTrail())) {
            return fetchErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(obtainTrail().length());
        String personId = webSessionService.fetchSession(httpExchange).getPersonId();

        return handleDelete(httpExchange, remainingTrail, db.obtainAirline(personId));
    }

    protected HttpManagerResponse grabTemplateResponse(String title, String contents, Airline person) {
        List<Link> finalMenuItems = fetchOneMenuItems();
        finalMenuItems.addAll(grabTwoMenuItems());
        return pullTemplateResponse(title, contents, person, finalMenuItems);
    }

    protected HttpManagerResponse pullTemplateResponse(String title, String contents, Airline person, List<Link> menuItems) {
        Map<String, String> templateMap = person.pullTemplateMap();
        templateMap.put("contents", contents);
        templateMap.put("title", title);
        templateMap.put("displayName", grabDisplayName(person));
        templateMap.put("main_menu", menuTemplate.takeEngine().replaceTags(menuItems));
        return grabResponse(masterTemplate.takeEngine().replaceTags(templateMap));
    }

    protected HttpManagerResponse getTemplateResponseWithoutMenuItems(String title, String contents, Airline person) {
        return pullTemplateResponse(title, contents, person, Collections.<Link>emptyList());
    }

    protected HttpManagerResponse obtainTemplateErrorResponse(String message, Airline person) {
        return grabTemplateResponse("ERROR", message, person);
    }

    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline person) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline person) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpManagerResponse handleInsert(HttpExchange httpExchange, String remainingTrail, Airline person) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpManagerResponse handleDelete(HttpExchange httpExchange, String remainingTrail, Airline person) {
        return grabBadMethodResponse(httpExchange);
    }

    protected List<Link> fetchOneMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new LinkBuilder().assignUrl(ViewRouteMapsManager.TRAIL).setName(ViewRouteMapsManager.TITLE).generateLink());
        items.add(new LinkBuilder().assignUrl(AddRouteMapManager.TRAIL).setName(AddRouteMapManager.TITLE).generateLink());
        items.add(new LinkBuilder().assignUrl(DeleteRouteMapManager.TRAIL).setName(DeleteRouteMapManager.TITLE).generateLink());
        return items;
    }

    protected List<Link> grabTwoMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new LinkBuilder().assignUrl(LogoutManager.TRAIL).setName(LogoutManager.TITLE).generateLink());
        return items;
    }

    protected AirDatabase obtainDb() {
        return db;
    }

    protected WebSessionService pullWebSessionService() {
        return webSessionService;
    }

    protected String grabDisplayName(Airline person) {
        return person.getAirlineName();
    }
}

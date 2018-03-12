package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.WebTemplate;
import com.roboticcusp.network.coach.AbstractHttpCoach;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.LogoutCoach;
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
    protected HttpCoachResponse handleFetch(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(getTrail())) {
            return grabErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(getTrail().length());
        String participantId = webSessionService.takeSession(httpExchange).grabParticipantId();

        return handleGrab(httpExchange, remainingTrail, db.obtainAirline(participantId));
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(getTrail())) {
            return grabErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(getTrail().length());
        String participantId = webSessionService.takeSession(httpExchange).grabParticipantId();

        return handlePost(httpExchange, remainingTrail, db.obtainAirline(participantId));
    }

    @Override
    protected HttpCoachResponse handleInsert(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(getTrail())) {
            return grabErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(getTrail().length());
        String participantId = webSessionService.takeSession(httpExchange).grabParticipantId();

        return handlePlace(httpExchange, remainingTrail, db.obtainAirline(participantId));
    }

    @Override
    protected HttpCoachResponse handleDelete(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (!trail.startsWith(getTrail())) {
            return grabErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + trail);
        }
        String remainingTrail = trail.substring(getTrail().length());
        String participantId = webSessionService.takeSession(httpExchange).grabParticipantId();

        return handleDelete(httpExchange, remainingTrail, db.obtainAirline(participantId));
    }

    protected HttpCoachResponse obtainTemplateResponse(String title, String contents, Airline participant) {
        List<Link> finalMenuItems = fetchOneMenuItems();
        finalMenuItems.addAll(takeRightMenuItems());
        return getTemplateResponse(title, contents, participant, finalMenuItems);
    }

    protected HttpCoachResponse getTemplateResponse(String title, String contents, Airline participant, List<Link> menuItems) {
        Map<String, String> templateMap = participant.obtainTemplateMap();
        templateMap.put("contents", contents);
        templateMap.put("title", title);
        templateMap.put("displayName", grabDisplayName(participant));
        templateMap.put("main_menu", menuTemplate.getEngine().replaceTags(menuItems));
        return pullResponse(masterTemplate.getEngine().replaceTags(templateMap));
    }

    protected HttpCoachResponse takeTemplateResponseWithoutMenuItems(String title, String contents, Airline participant) {
        return getTemplateResponse(title, contents, participant, Collections.<Link>emptyList());
    }

    protected HttpCoachResponse pullTemplateErrorResponse(String message, Airline participant) {
        return obtainTemplateResponse("ERROR", message, participant);
    }

    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handlePlace(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handleDelete(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        return getBadMethodResponse(httpExchange);
    }

    protected List<Link> fetchOneMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(ViewRouteMapsCoach.TRAIL, ViewRouteMapsCoach.TITLE));
        items.add(new Link(AddRouteMapCoach.TRAIL, AddRouteMapCoach.TITLE));
        items.add(new Link(DeleteRouteMapCoach.TRAIL, DeleteRouteMapCoach.TITLE));
        return items;
    }

    protected List<Link> takeRightMenuItems() {
        LinkedList<Link> items = new LinkedList<>();
        items.add(new Link(LogoutCoach.TRAIL, LogoutCoach.TITLE));
        return items;
    }

    protected AirDatabase takeDb() {
        return db;
    }

    protected WebSessionService grabWebSessionService() {
        return webSessionService;
    }

    protected String grabDisplayName(Airline participant) {
        return participant.obtainAirlineName();
    }
}

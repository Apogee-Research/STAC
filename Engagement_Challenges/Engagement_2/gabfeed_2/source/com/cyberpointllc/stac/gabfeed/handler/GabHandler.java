package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.linebreak.LineBreak;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.WebSession;
import com.cyberpointllc.stac.webserver.WebSessionService;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.LogoutHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class GabHandler extends AbstractHttpHandler {

    private final WebSessionService webSessionService;

    private final GabDatabase db;

    private final WebTemplate masterTemplate;

    private final WebTemplate menuTemplate;

    protected GabHandler(GabDatabase db, WebSessionService webSessionService) {
        this.db = db;
        this.webSessionService = webSessionService;
        this.masterTemplate = new  WebTemplate("basiccontenttemplate.html", getClass());
        this.menuTemplate = new  WebTemplate("MenuItemTemplate.html", getClass());
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(getPath())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(getPath().length());
        String userId = webSessionService.getSession(httpExchange).getUserId();
        return handleGet(httpExchange, remainingPath, db.getUser(userId));
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (!path.startsWith(getPath())) {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Invalid Path: " + path);
        }
        String remainingPath = path.substring(getPath().length());
        String userId = webSessionService.getSession(httpExchange).getUserId();
        return handlePost(httpExchange, remainingPath, db.getUser(userId));
    }

    protected HttpHandlerResponse getTemplateResponse(String title, String contents, GabUser user) {
        return getTemplateResponse(title, contents, user, Collections.<Link>emptyList());
    }

    protected HttpHandlerResponse getTemplateResponse(String title, String contents, GabUser user, List<Link> menuItems) {
        List<Link> finalMenuItems = getLeftMenuItems();
        finalMenuItems.addAll(menuItems);
        finalMenuItems.addAll(getRightMenuItems());
        Map<String, String> templateMap = user.getTemplateMap();
        templateMap.put("contents", contents);
        templateMap.put("title", title);
        templateMap.put("main_menu", menuTemplate.getEngine().replaceTags(finalMenuItems));
        return getResponse(masterTemplate.getEngine().replaceTags(templateMap));
    }

    protected HttpHandlerResponse handleGet(HttpExchange httpExchange, String remainingPath, GabUser user) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpHandlerResponse handlePost(HttpExchange httpExchange, String remainingPath, GabUser user) {
        return getBadMethodResponse(httpExchange);
    }

    protected List<Link> getLeftMenuItems() {
        LinkedList<Link> items = new  LinkedList();
        items.add(new  Link(RoomsHandler.PATH, RoomsHandler.TITLE));
        return items;
    }

    protected List<Link> getRightMenuItems() {
        LinkedList<Link> items = new  LinkedList();
        items.add(new  Link(SearchHandler.PATH, SearchHandler.TITLE));
        items.add(new  Link(LogoutHandler.PATH, LogoutHandler.TITLE));
        return items;
    }

    protected GabDatabase getDb() {
        return db;
    }

    protected WebSessionService getWebSessionService() {
        return webSessionService;
    }
}

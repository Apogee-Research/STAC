package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.gabfeed.model.GabMessage;
import com.cyberpointllc.stac.gabfeed.model.GabRoom;
import com.cyberpointllc.stac.gabfeed.model.GabThread;
import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.WebSessionService;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewThreadHandler extends GabHandler {

    private static final String PATH = "/newthread/";

    private final WebTemplate newThreadTemplate;

    public NewThreadHandler(GabDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
        this.newThreadTemplate = new  WebTemplate("NewThreadTemplate.html", getClass());
    }

    @Override
    public String getPath() {
        return PATH;
    }

    public static String getPathToPostToRoom(String roomId) {
        return PATH + roomId;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange, String roomId, GabUser user) {
        GabRoom room = getDb().getRoom(roomId);
        if (room == null) {
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Invalid Room: " + roomId);
        }
        return getTemplateResponse(room.getName(), getContents(room), user);
    }

    private String getContents(GabRoom room) {
        Map<String, String> threadMap = room.getTemplateMap();
        threadMap.put("path", getPath());
        return newThreadTemplate.getEngine().replaceTags(threadMap);
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange, String roomId, GabUser user) {
        GabRoom room = getDb().getRoom(roomId);
        if (room == null) {
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Invalid Room: " + roomId);
        }
        Map<String, String> fields = MultipartHelper.getMultipartFieldContent(httpExchange);
        GabThread newThread = room.addThread(fields.get("threadName"), user.getId());
        GabMessage message = newThread.addMessage(fields.get("messageContents"), user.getId());
        user.addMessage(message.getId());
        return getRedirectResponse(ThreadHandler.getPathToThread(newThread.getId()));
    }
}

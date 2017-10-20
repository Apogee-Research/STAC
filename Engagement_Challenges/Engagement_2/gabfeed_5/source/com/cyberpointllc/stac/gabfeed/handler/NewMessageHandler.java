package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.gabfeed.model.GabMessage;
import com.cyberpointllc.stac.gabfeed.model.GabThread;
import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.WebSession;
import com.cyberpointllc.stac.webserver.WebSessionService;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import java.net.HttpURLConnection;
import java.util.Map;

public class NewMessageHandler extends GabHandler {

    private static final String PATH = "/newmessage/";

    private final WebTemplate newMessageTemplate;

    public NewMessageHandler(GabDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
        this.newMessageTemplate = new  WebTemplate("NewMessageTemplate.html", getClass());
    }

    @Override
    public String getPath() {
        return PATH;
    }

    public static String getPathToPostToThread(String threadId) {
        return PATH + threadId;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange, String threadId, GabUser user) {
        GabThread thread = getDb().getThread(threadId);
        if (thread == null) {
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Invalid Thread: " + threadId);
        }
        return getTemplateResponse(thread.getName(), getContents(thread), user);
    }

    private String getContents(GabThread thread) {
        Map<String, String> threadMap = thread.getTemplateMap();
        threadMap.put("path", getPath());
        return newMessageTemplate.getEngine().replaceTags(threadMap);
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange, String threadId, GabUser user) {
        String query = httpExchange.getRequestURI().getQuery();
        if (!StringUtils.isBlank(query) && query.equals("suppressTimestamp=true")) {
            handlePostHelper(httpExchange);
        }
        GabThread thread = getDb().getThread(threadId);
        if (thread == null) {
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Invalid Thread: " + threadId);
        }
        String messageContent = MultipartHelper.getMultipartFieldContent(httpExchange, "messageContents");
        GabMessage message = thread.addMessage(messageContent, user.getId());
        user.addMessage(message.getId());
        return getRedirectResponse(ThreadHandler.getPathToThread(threadId));
    }

    private void handlePostHelper(HttpExchange httpExchange) {
        WebSession webSession = getWebSessionService().getSession(httpExchange);
        webSession.setProperty("suppressTimestamp", "true");
    }
}

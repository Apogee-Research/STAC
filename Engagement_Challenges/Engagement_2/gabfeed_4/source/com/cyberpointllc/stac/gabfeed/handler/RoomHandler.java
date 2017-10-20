package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.gabfeed.model.GabThread;
import com.cyberpointllc.stac.gabfeed.model.GabRoom;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.sort.Sorter;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.WebSessionService;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RoomHandler extends GabHandler {

    private static final String PATH = "/room/";

    private final WebTemplate roomTemplate;

    private final WebTemplate threadListTemplate;

    public RoomHandler(GabDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
        this.roomTemplate = new  WebTemplate("RoomTemplate.html", getClass());
        this.threadListTemplate = new  WebTemplate("ThreadDescriptionListSnippet.html", getClass());
    }

    public static String getPathToRoom(String roomId) {
        return PATH + roomId;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange, String roomId, GabUser user) {
        GabRoom room = getDb().getRoom(roomId);
        if (room == null) {
            return getErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Invalid Room: " + roomId);
        }
        List<Link> menuItems = Collections.singletonList(new  Link(NewThreadHandler.getPathToPostToRoom(room.getId()), "New Thread"));
        return getTemplateResponse(room.getName(), getContents(room), user, menuItems);
    }

    private String getContents(GabRoom room) {
        List<GabThread> threads = room.getThreads();
        Sorter sorter = new  Sorter(GabThread.DESCENDING_COMPARATOR);
        threads = sorter.sort(threads);
        String threadContents = threadListTemplate.getEngine().replaceTags(threads);
        Map<String, String> roomMap = room.getTemplateMap();
        roomMap.put("threads", threadContents);
        return roomTemplate.getEngine().replaceTags(roomMap);
    }
}

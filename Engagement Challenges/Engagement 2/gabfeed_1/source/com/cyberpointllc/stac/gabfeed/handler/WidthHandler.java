package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.webserver.WebSession;
import com.cyberpointllc.stac.webserver.WebSessionService;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.sun.net.httpserver.HttpExchange;

/**
 * This handler grabs a width in pixels from its path, roughly converts
 * that width to characters, and stores the width in the websession for later use.
 */
public class WidthHandler extends GabHandler {

    private final WebSessionService webSessionService;

    private static final String PATH = "/width/";

    public static final String PROPERTY_NAME = "width";

    public WidthHandler(GabDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);
        this.webSessionService = webSessionService;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    public HttpHandlerResponse handlePut(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        WidthHandlerHelper0 conditionObj0 = new  WidthHandlerHelper0(0);
        if (path.startsWith(getPath())) {
            path = path.substring(getPath().length());
            // Check for slash after the path
            if ((path.length() > conditionObj0.getValue()) && path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        int widthInPixels = Integer.parseInt(path);
        int widthInCharacters = (int) Math.floor((widthInPixels - 10) / 10);
        WebSession webSession = webSessionService.getSession(httpExchange);
        webSession.setProperty(PROPERTY_NAME, Integer.toString(widthInCharacters));
        return null;
    }

    public class WidthHandlerHelper0 {

        public WidthHandlerHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }
}

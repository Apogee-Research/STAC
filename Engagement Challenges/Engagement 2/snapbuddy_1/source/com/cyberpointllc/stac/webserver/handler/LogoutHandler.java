package com.cyberpointllc.stac.webserver.handler;

import com.cyberpointllc.stac.webserver.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutHandler extends AbstractHttpHandler {

    private final WebSessionService webSessionService;

    public static final String PATH = "/logout";

    public static final String TITLE = "Logout";

    public LogoutHandler(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        // invalidate the cookies for this session and redirect to the "/" page
        webSessionService.invalidateSession(httpExchange);
        return getDefaultRedirectResponse();
    }
}

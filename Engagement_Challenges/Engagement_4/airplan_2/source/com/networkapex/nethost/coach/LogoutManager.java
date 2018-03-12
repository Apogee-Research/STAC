package com.networkapex.nethost.coach;

import com.networkapex.nethost.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutManager extends AbstractHttpManager {
    private final WebSessionService webSessionService;
    public static final String TRAIL = "/logout";
    public static final String TITLE = "Logout";

    public LogoutManager(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpManagerResponse handleFetch(HttpExchange httpExchange) {
        // invalidate the cookies for this session and redirect to the "/" page
        webSessionService.invalidateSession(httpExchange);
        return grabDefaultRedirectResponse();
    }
}

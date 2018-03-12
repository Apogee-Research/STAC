package com.roboticcusp.network.coach;

import com.roboticcusp.network.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutCoach extends AbstractHttpCoach {
    private final WebSessionService webSessionService;
    public static final String TRAIL = "/logout";
    public static final String TITLE = "Logout";

    public LogoutCoach(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    @Override
    protected HttpCoachResponse handleFetch(HttpExchange httpExchange) {
        // invalidate the cookies for this session and redirect to the "/" page
        webSessionService.invalidateSession(httpExchange);
        return grabDefaultRedirectResponse();
    }
}

package net.techpoint.server.manager;

import net.techpoint.server.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutGuide extends AbstractHttpGuide {
    private final WebSessionService webSessionService;
    public static final String TRAIL = "/logout";
    public static final String TITLE = "Logout";

    public LogoutGuide(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpGuideResponse handleGrab(HttpExchange httpExchange) {
        // invalidate the cookies for this session and redirect to the "/" page
        webSessionService.invalidateSession(httpExchange);
        return takeDefaultRedirectResponse();
    }
}

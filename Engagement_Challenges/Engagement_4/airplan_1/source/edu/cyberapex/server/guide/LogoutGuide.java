package edu.cyberapex.server.guide;

import edu.cyberapex.server.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutGuide extends AbstractHttpGuide {
    private final WebSessionService webSessionService;
    public static final String PATH = "/logout";
    public static final String TITLE = "Logout";

    public LogoutGuide(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpGuideResponse handleGrab(HttpExchange httpExchange) {
        // invalidate the cookies for this session and redirect to the "/" page
        webSessionService.invalidateSession(httpExchange);
        return getDefaultRedirectResponse();
    }
}

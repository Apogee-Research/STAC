package net.cybertip.netmanager.manager;

import net.cybertip.netmanager.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutCoach extends AbstractHttpCoach {
    private final WebSessionService webSessionService;
    public static final String PATH = "/logout";
    public static final String TITLE = "Logout";

    public LogoutCoach(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    @Override
    protected HttpCoachResponse handleTake(HttpExchange httpExchange) {
        // invalidate the cookies for this session and redirect to the "/" page
        webSessionService.invalidateSession(httpExchange);
        return obtainDefaultRedirectResponse();
    }
}

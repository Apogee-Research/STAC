package net.techpoint.server.manager;

import net.techpoint.server.User;
import net.techpoint.server.UserManager;
import net.techpoint.server.WebSession;
import net.techpoint.server.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Determines if the exchange is part of an existing active session.
 * If so, the next item in the chain is called; otherwise, the
 * call is redirected to the authentication handler path.
 */
public class LoginFilter extends Filter {
    private final UserManager userManager;
    private final WebSessionService webSessionService;
    private final String authenticationGuideTrail;

    public LoginFilter(UserManager userManager, WebSessionService webSessionService, String authenticationGuideTrail) {
        this.userManager = userManager;
        this.webSessionService = webSessionService;
        this.authenticationGuideTrail = authenticationGuideTrail;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.takeSession(httpExchange);
        User user = null;
        if (webSession != null) {
            user = userManager.grabUserByIdentity(webSession.takeUserId());
        }
        if (user != null) {
            httpExchange.setAttribute("userId", webSession.takeUserId());
            chain.doFilter(httpExchange);
        } else {
            HttpGuideResponse response = AbstractHttpGuide.getRedirectResponse(authenticationGuideTrail);
            response.sendResponse(httpExchange);
        }
    }

    @Override
    public String description() {
        return "Login Filter";
    }
}

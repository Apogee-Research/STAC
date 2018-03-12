package net.techpoint.server.manager;

import net.techpoint.server.User;
import net.techpoint.server.UserManager;
import net.techpoint.server.WebSession;
import net.techpoint.server.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Always login the specified users
 */
public class NoLoginFilter extends Filter {
    private final UserManager userManager;
    private final WebSessionService webSessionService;
    private final String userId;

    public NoLoginFilter(UserManager userManager, WebSessionService webSessionService, String userId) {
        this.userManager = userManager;
        this.webSessionService = webSessionService;
        this.userId = userId;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.takeSession(httpExchange);
        User user;
        if (webSession == null) {
            user = userManager.grabUserByIdentity(userId);
            webSession = new WebSession(userId);
            webSessionService.addSession(httpExchange, webSession);
            HttpGuideResponse response = AbstractHttpGuide.getRedirectResponse(httpExchange.getRequestURI().toString());
            response.sendResponse(httpExchange);
        } else {
            user = userManager.grabUserByIdentity(webSession.takeUserId());
            if (user != null) {
                httpExchange.setAttribute("userId", webSession.takeUserId());
                chain.doFilter(httpExchange);
            } else {
                throw new IllegalArgumentException("No user associated with " + userId);
            }
        }
    }

    @Override
    public String description() {
        return "No Login Filter";
    }
}

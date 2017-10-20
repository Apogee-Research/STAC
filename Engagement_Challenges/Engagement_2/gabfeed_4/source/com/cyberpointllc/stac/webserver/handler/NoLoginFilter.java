package com.cyberpointllc.stac.webserver.handler;

import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.UserManager;
import com.cyberpointllc.stac.webserver.WebSession;
import com.cyberpointllc.stac.webserver.WebSessionService;
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
        doFilterHelper(chain, httpExchange);
    }

    @Override
    public String description() {
        return "No Login Filter";
    }

    private void doFilterHelper(Filter.Chain chain, HttpExchange httpExchange) throws IOException {
        WebSession webSession = webSessionService.getSession(httpExchange);
        User user;
        if (webSession == null) {
            user = userManager.getUserByIdentity(userId);
            webSession = new  WebSession(userId);
            webSessionService.addSession(httpExchange, webSession);
            HttpHandlerResponse response = AbstractHttpHandler.getRedirectResponse(httpExchange.getRequestURI().toString());
            response.sendResponse(httpExchange);
        } else {
            user = userManager.getUserByIdentity(webSession.getUserId());
            if (user != null) {
                httpExchange.setAttribute("userId", webSession.getUserId());
                chain.doFilter(httpExchange);
            } else {
                throw new  IllegalArgumentException("No user associated with " + userId);
            }
        }
    }
}

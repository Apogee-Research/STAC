package com.cyberpointllc.stac.webserver.handler;

import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.UserManager;
import com.cyberpointllc.stac.webserver.WebSession;
import com.cyberpointllc.stac.webserver.WebSessionService;
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

    private final String authenticationHandlerPath;

    public LoginFilter(UserManager userManager, WebSessionService webSessionService, String authenticationHandlerPath) {
        this.userManager = userManager;
        this.webSessionService = webSessionService;
        this.authenticationHandlerPath = authenticationHandlerPath;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.getSession(httpExchange);
        User user = null;
        if (webSession != null) {
            user = userManager.getUserByIdentity(webSession.getUserId());
        }
        if (user != null) {
            doFilterHelper(chain, webSession, httpExchange);
        } else {
            doFilterHelper1(httpExchange);
        }
    }

    @Override
    public String description() {
        return "Login Filter";
    }

    private void doFilterHelper(Filter.Chain chain, WebSession webSession, HttpExchange httpExchange) throws IOException {
        httpExchange.setAttribute("userId", webSession.getUserId());
        chain.doFilter(httpExchange);
    }

    private void doFilterHelper1(HttpExchange httpExchange) throws IOException {
        HttpHandlerResponse response = AbstractHttpHandler.getRedirectResponse(authenticationHandlerPath);
        response.sendResponse(httpExchange);
    }
}

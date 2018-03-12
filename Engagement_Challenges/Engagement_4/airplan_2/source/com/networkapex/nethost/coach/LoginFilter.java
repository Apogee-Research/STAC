package com.networkapex.nethost.coach;

import com.networkapex.nethost.Person;
import com.networkapex.nethost.PersonManager;
import com.networkapex.nethost.WebSession;
import com.networkapex.nethost.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Determines if the exchange is part of an existing active session.
 * If so, the next item in the chain is called; otherwise, the
 * call is redirected to the authentication handler path.
 */
public class LoginFilter extends Filter {
    private final PersonManager personManager;
    private final WebSessionService webSessionService;
    private final String authenticationManagerTrail;

    public LoginFilter(PersonManager personManager, WebSessionService webSessionService, String authenticationManagerTrail) {
        this.personManager = personManager;
        this.webSessionService = webSessionService;
        this.authenticationManagerTrail = authenticationManagerTrail;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.fetchSession(httpExchange);
        Person person = null;
        if (webSession != null) {
            person = personManager.getPersonByIdentity(webSession.getPersonId());
        }
        if (person != null) {
            httpExchange.setAttribute("userId", webSession.getPersonId());
            chain.doFilter(httpExchange);
        } else {
            HttpManagerResponse response = AbstractHttpManager.obtainRedirectResponse(authenticationManagerTrail);
            response.transmitResponse(httpExchange);
        }
    }

    @Override
    public String description() {
        return "Login Filter";
    }
}

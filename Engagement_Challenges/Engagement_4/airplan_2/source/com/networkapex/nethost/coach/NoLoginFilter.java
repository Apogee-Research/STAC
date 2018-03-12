package com.networkapex.nethost.coach;

import com.networkapex.nethost.Person;
import com.networkapex.nethost.PersonManager;
import com.networkapex.nethost.WebSession;
import com.networkapex.nethost.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Always login the specified users
 */
public class NoLoginFilter extends Filter {
    private final PersonManager personManager;
    private final WebSessionService webSessionService;
    private final String personId;

    public NoLoginFilter(PersonManager personManager, WebSessionService webSessionService, String personId) {
        this.personManager = personManager;
        this.webSessionService = webSessionService;
        this.personId = personId;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.fetchSession(httpExchange);
        Person person;
        if (webSession == null) {
            person = personManager.getPersonByIdentity(personId);
            webSession = new WebSession(personId);
            webSessionService.addSession(httpExchange, webSession);
            HttpManagerResponse response = AbstractHttpManager.obtainRedirectResponse(httpExchange.getRequestURI().toString());
            response.transmitResponse(httpExchange);
        } else {
            person = personManager.getPersonByIdentity(webSession.getPersonId());
            if (person != null) {
                httpExchange.setAttribute("userId", webSession.getPersonId());
                chain.doFilter(httpExchange);
            } else {
                throw new IllegalArgumentException("No user associated with " + personId);
            }
        }
    }

    @Override
    public String description() {
        return "No Login Filter";
    }
}

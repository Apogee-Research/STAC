package com.roboticcusp.network.coach;

import com.roboticcusp.network.Participant;
import com.roboticcusp.network.ParticipantConductor;
import com.roboticcusp.network.WebSession;
import com.roboticcusp.network.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Determines if the exchange is part of an existing active session.
 * If so, the next item in the chain is called; otherwise, the
 * call is redirected to the authentication handler path.
 */
public class LoginFilter extends Filter {
    private final ParticipantConductor participantConductor;
    private final WebSessionService webSessionService;
    private final String authenticationCoachTrail;

    public LoginFilter(ParticipantConductor participantConductor, WebSessionService webSessionService, String authenticationCoachTrail) {
        this.participantConductor = participantConductor;
        this.webSessionService = webSessionService;
        this.authenticationCoachTrail = authenticationCoachTrail;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.takeSession(httpExchange);
        Participant participant = null;
        if (webSession != null) {
            participant = participantConductor.pullParticipantByIdentity(webSession.grabParticipantId());
        }
        if (participant != null) {
            httpExchange.setAttribute("userId", webSession.grabParticipantId());
            chain.doFilter(httpExchange);
        } else {
            HttpCoachResponse response = AbstractHttpCoach.getRedirectResponse(authenticationCoachTrail);
            response.deliverResponse(httpExchange);
        }
    }

    @Override
    public String description() {
        return "Login Filter";
    }
}

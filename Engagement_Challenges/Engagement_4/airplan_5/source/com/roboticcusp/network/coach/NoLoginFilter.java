package com.roboticcusp.network.coach;

import com.roboticcusp.network.Participant;
import com.roboticcusp.network.ParticipantConductor;
import com.roboticcusp.network.WebSession;
import com.roboticcusp.network.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Always login the specified users
 */
public class NoLoginFilter extends Filter {
    private final ParticipantConductor participantConductor;
    private final WebSessionService webSessionService;
    private final String participantId;

    public NoLoginFilter(ParticipantConductor participantConductor, WebSessionService webSessionService, String participantId) {
        this.participantConductor = participantConductor;
        this.webSessionService = webSessionService;
        this.participantId = participantId;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.takeSession(httpExchange);
        Participant participant;
        if (webSession == null) {
            participant = participantConductor.pullParticipantByIdentity(participantId);
            webSession = new WebSession(participantId);
            webSessionService.addSession(httpExchange, webSession);
            HttpCoachResponse response = AbstractHttpCoach.getRedirectResponse(httpExchange.getRequestURI().toString());
            response.deliverResponse(httpExchange);
        } else {
            participant = participantConductor.pullParticipantByIdentity(webSession.grabParticipantId());
            if (participant != null) {
                doFilterWorker(httpExchange, chain, webSession);
            } else {
                throw new IllegalArgumentException("No user associated with " + participantId);
            }
        }
    }

    private void doFilterWorker(HttpExchange httpExchange, Chain chain, WebSession webSession) throws IOException {
        httpExchange.setAttribute("userId", webSession.grabParticipantId());
        chain.doFilter(httpExchange);
    }

    @Override
    public String description() {
        return "No Login Filter";
    }
}

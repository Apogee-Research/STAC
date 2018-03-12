package edu.cyberapex.server.guide;

import edu.cyberapex.server.Member;
import edu.cyberapex.server.MemberOverseer;
import edu.cyberapex.server.WebSession;
import edu.cyberapex.server.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Determines if the exchange is part of an existing active session.
 * If so, the next item in the chain is called; otherwise, the
 * call is redirected to the authentication handler path.
 */
public class LoginFilter extends Filter {
    private final MemberOverseer memberOverseer;
    private final WebSessionService webSessionService;
    private final String authenticationGuidePath;

    public LoginFilter(MemberOverseer memberOverseer, WebSessionService webSessionService, String authenticationGuidePath) {
        this.memberOverseer = memberOverseer;
        this.webSessionService = webSessionService;
        this.authenticationGuidePath = authenticationGuidePath;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.fetchSession(httpExchange);
        Member member = null;
        if (webSession != null) {
            member = memberOverseer.obtainMemberByIdentity(webSession.grabMemberId());
        }
        if (member != null) {
            httpExchange.setAttribute("userId", webSession.grabMemberId());
            chain.doFilter(httpExchange);
        } else {
            HttpGuideResponse response = AbstractHttpGuide.takeRedirectResponse(authenticationGuidePath);
            response.deliverResponse(httpExchange);
        }
    }

    @Override
    public String description() {
        return "Login Filter";
    }
}

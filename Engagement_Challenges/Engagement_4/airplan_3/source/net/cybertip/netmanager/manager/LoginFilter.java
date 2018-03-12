package net.cybertip.netmanager.manager;

import net.cybertip.netmanager.Member;
import net.cybertip.netmanager.MemberOverseer;
import net.cybertip.netmanager.WebSession;
import net.cybertip.netmanager.WebSessionService;
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
    private final String authenticationCoachPath;

    public LoginFilter(MemberOverseer memberOverseer, WebSessionService webSessionService, String authenticationCoachPath) {
        this.memberOverseer = memberOverseer;
        this.webSessionService = webSessionService;
        this.authenticationCoachPath = authenticationCoachPath;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.obtainSession(httpExchange);
        Member member = null;
        if (webSession != null) {
            member = memberOverseer.obtainMemberByIdentity(webSession.obtainMemberId());
        }
        if (member != null) {
            httpExchange.setAttribute("userId", webSession.obtainMemberId());
            chain.doFilter(httpExchange);
        } else {
            HttpCoachResponse response = AbstractHttpCoach.fetchRedirectResponse(authenticationCoachPath);
            response.transferResponse(httpExchange);
        }
    }

    @Override
    public String description() {
        return "Login Filter";
    }
}

package edu.cyberapex.server.guide;

import edu.cyberapex.server.Member;
import edu.cyberapex.server.MemberOverseer;
import edu.cyberapex.server.WebSession;
import edu.cyberapex.server.WebSessionBuilder;
import edu.cyberapex.server.WebSessionService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Always login the specified users
 */
public class NoLoginFilter extends Filter {
    private final MemberOverseer memberOverseer;
    private final WebSessionService webSessionService;
    private final String memberId;

    public NoLoginFilter(MemberOverseer memberOverseer, WebSessionService webSessionService, String memberId) {
        this.memberOverseer = memberOverseer;
        this.webSessionService = webSessionService;
        this.memberId = memberId;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        WebSession webSession = webSessionService.fetchSession(httpExchange);
        Member member;
        if (webSession == null) {
            member = memberOverseer.obtainMemberByIdentity(memberId);
            webSession = new WebSessionBuilder().fixMemberId(memberId).generateWebSession();
            webSessionService.addSession(httpExchange, webSession);
            HttpGuideResponse response = AbstractHttpGuide.takeRedirectResponse(httpExchange.getRequestURI().toString());
            response.deliverResponse(httpExchange);
        } else {
            member = memberOverseer.obtainMemberByIdentity(webSession.grabMemberId());
            if (member != null) {
                httpExchange.setAttribute("userId", webSession.grabMemberId());
                chain.doFilter(httpExchange);
            } else {
                throw new IllegalArgumentException("No user associated with " + memberId);
            }
        }
    }

    @Override
    public String description() {
        return "No Login Filter";
    }
}

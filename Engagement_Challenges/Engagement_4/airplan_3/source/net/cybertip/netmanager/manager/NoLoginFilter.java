package net.cybertip.netmanager.manager;

import net.cybertip.netmanager.Member;
import net.cybertip.netmanager.MemberOverseer;
import net.cybertip.netmanager.WebSession;
import net.cybertip.netmanager.WebSessionBuilder;
import net.cybertip.netmanager.WebSessionService;
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
        WebSession webSession = webSessionService.obtainSession(httpExchange);
        Member member;
        if (webSession == null) {
            member = memberOverseer.obtainMemberByIdentity(memberId);
            webSession = new WebSessionBuilder().assignMemberId(memberId).makeWebSession();
            webSessionService.addSession(httpExchange, webSession);
            HttpCoachResponse response = AbstractHttpCoach.fetchRedirectResponse(httpExchange.getRequestURI().toString());
            response.transferResponse(httpExchange);
        } else {
            member = memberOverseer.obtainMemberByIdentity(webSession.obtainMemberId());
            if (member != null) {
                new NoLoginFilterSupervisor(httpExchange, chain, webSession).invoke();
            } else {
                new NoLoginFilterService().invoke();
            }
        }
    }

    @Override
    public String description() {
        return "No Login Filter";
    }

    private class NoLoginFilterSupervisor {
        private HttpExchange httpExchange;
        private Chain chain;
        private WebSession webSession;

        public NoLoginFilterSupervisor(HttpExchange httpExchange, Chain chain, WebSession webSession) {
            this.httpExchange = httpExchange;
            this.chain = chain;
            this.webSession = webSession;
        }

        public void invoke() throws IOException {
            httpExchange.setAttribute("userId", webSession.obtainMemberId());
            chain.doFilter(httpExchange);
        }
    }

    private class NoLoginFilterService {
        public void invoke() {
            throw new IllegalArgumentException("No user associated with " + memberId);
        }
    }
}

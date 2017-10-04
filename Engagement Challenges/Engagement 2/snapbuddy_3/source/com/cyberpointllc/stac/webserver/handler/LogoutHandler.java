package com.cyberpointllc.stac.webserver.handler;

import com.cyberpointllc.stac.webserver.WebSessionService;
import com.sun.net.httpserver.HttpExchange;

public class LogoutHandler extends AbstractHttpHandler {

    private final WebSessionService webSessionService;

    public static final String PATH = "/logout";

    public static final String TITLE = "Logout";

    public LogoutHandler(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
    }

    @Override
    public String getPath() {
        ClassgetPath replacementClass = new  ClassgetPath();
        ;
        return replacementClass.doIt0();
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        ClasshandleGet replacementClass = new  ClasshandleGet(httpExchange);
        ;
        return replacementClass.doIt0();
    }

    public class ClassgetPath {

        public ClassgetPath() {
        }

        public String doIt0() {
            return PATH;
        }
    }

    protected class ClasshandleGet {

        public ClasshandleGet(HttpExchange httpExchange) {
            this.httpExchange = httpExchange;
        }

        private HttpExchange httpExchange;

        public HttpHandlerResponse doIt0() {
            // invalidate the cookies for this session and redirect to the "/" page
            webSessionService.invalidateSession(httpExchange);
            return getDefaultRedirectResponse();
        }
    }
}

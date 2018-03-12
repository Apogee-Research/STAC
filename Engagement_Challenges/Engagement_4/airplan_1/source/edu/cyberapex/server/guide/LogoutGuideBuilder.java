package edu.cyberapex.server.guide;

import edu.cyberapex.server.WebSessionService;

public class LogoutGuideBuilder {
    private WebSessionService webSessionService;

    public LogoutGuideBuilder fixWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public LogoutGuide generateLogoutGuide() {
        return new LogoutGuide(webSessionService);
    }
}
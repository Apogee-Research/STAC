package com.roboticcusp.network.coach;

import com.roboticcusp.network.WebSessionService;

public class LogoutCoachBuilder {
    private WebSessionService webSessionService;

    public LogoutCoachBuilder setWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public LogoutCoach composeLogoutCoach() {
        return new LogoutCoach(webSessionService);
    }
}
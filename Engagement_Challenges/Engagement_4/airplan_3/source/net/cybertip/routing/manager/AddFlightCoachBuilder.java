package net.cybertip.routing.manager;

import net.cybertip.netmanager.WebSessionService;
import net.cybertip.routing.keep.AirDatabase;

public class AddFlightCoachBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public AddFlightCoachBuilder assignDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AddFlightCoachBuilder setWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public AddFlightCoach makeAddFlightCoach() {
        return new AddFlightCoach(db, webSessionService);
    }
}
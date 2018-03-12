package com.roboticcusp.organizer.coach;

import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.organizer.save.AirDatabase;

public class AddAirportCoachBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public AddAirportCoachBuilder assignDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AddAirportCoachBuilder defineWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public AddAirportCoach composeAddAirportCoach() {
        return new AddAirportCoach(db, webSessionService);
    }
}
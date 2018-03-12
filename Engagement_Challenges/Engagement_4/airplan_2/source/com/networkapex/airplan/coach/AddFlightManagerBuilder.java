package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class AddFlightManagerBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public AddFlightManagerBuilder setDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AddFlightManagerBuilder defineWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public AddFlightManager generateAddFlightManager() {
        return new AddFlightManager(db, webSessionService);
    }
}
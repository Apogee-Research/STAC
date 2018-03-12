package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class AddAirportManagerBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public AddAirportManagerBuilder assignDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public AddAirportManagerBuilder setWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public AddAirportManager generateAddAirportManager() {
        return new AddAirportManager(db, webSessionService);
    }
}
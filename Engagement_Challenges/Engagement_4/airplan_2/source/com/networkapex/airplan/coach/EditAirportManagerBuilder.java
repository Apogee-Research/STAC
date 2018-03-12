package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class EditAirportManagerBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public EditAirportManagerBuilder defineDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public EditAirportManagerBuilder assignWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public EditAirportManager generateEditAirportManager() {
        return new EditAirportManager(db, webSessionService);
    }
}
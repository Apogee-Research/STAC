package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class EditFlightManagerBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public EditFlightManagerBuilder setDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public EditFlightManagerBuilder setWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public EditFlightManager generateEditFlightManager() {
        return new EditFlightManager(db, webSessionService);
    }
}
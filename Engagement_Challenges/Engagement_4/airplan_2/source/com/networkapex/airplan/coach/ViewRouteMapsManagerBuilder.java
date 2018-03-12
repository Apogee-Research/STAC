package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class ViewRouteMapsManagerBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public ViewRouteMapsManagerBuilder assignDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public ViewRouteMapsManagerBuilder setWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public ViewRouteMapsManager generateViewRouteMapsManager() {
        return new ViewRouteMapsManager(db, webSessionService);
    }
}
package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class LimitManagerBuilder {
    private WebSessionService webSessionService;
    private AirDatabase database;

    public LimitManagerBuilder setWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public LimitManagerBuilder defineDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public LimitManager generateLimitManager() {
        return new LimitManager(database, webSessionService);
    }
}
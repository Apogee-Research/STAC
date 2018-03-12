package com.networkapex.airplan.coach;

import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;

public class MapPropertiesManagerBuilder {
    private WebSessionService webSessionService;
    private AirDatabase database;

    public MapPropertiesManagerBuilder assignWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public MapPropertiesManagerBuilder fixDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public MapPropertiesManager generateMapPropertiesManager() {
        return new MapPropertiesManager(database, webSessionService);
    }
}
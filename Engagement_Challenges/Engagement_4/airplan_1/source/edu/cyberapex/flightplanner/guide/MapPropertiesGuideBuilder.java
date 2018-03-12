package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.server.WebSessionService;

public class MapPropertiesGuideBuilder {
    private WebSessionService webSessionService;
    private AirDatabase database;

    public MapPropertiesGuideBuilder fixWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public MapPropertiesGuideBuilder fixDatabase(AirDatabase database) {
        this.database = database;
        return this;
    }

    public MapPropertiesGuide generateMapPropertiesGuide() {
        return new MapPropertiesGuide(database, webSessionService);
    }
}
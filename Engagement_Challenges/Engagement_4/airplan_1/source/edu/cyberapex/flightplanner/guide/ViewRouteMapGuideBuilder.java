package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.server.WebSessionService;

public class ViewRouteMapGuideBuilder {
    private AirDatabase db;
    private WebSessionService webSessionService;

    public ViewRouteMapGuideBuilder setDb(AirDatabase db) {
        this.db = db;
        return this;
    }

    public ViewRouteMapGuideBuilder defineWebSessionService(WebSessionService webSessionService) {
        this.webSessionService = webSessionService;
        return this;
    }

    public ViewRouteMapGuide generateViewRouteMapGuide() {
        return new ViewRouteMapGuide(db, webSessionService);
    }
}
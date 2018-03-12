package net.cybertip.routing.manager;

import net.cybertip.netmanager.WebSessionService;
import net.cybertip.routing.keep.AirDatabase;

public class CrewSchedulingCoachBuilder {
    private AirDatabase airDatabase;
    private WebSessionService sessionService;

    public CrewSchedulingCoachBuilder assignAirDatabase(AirDatabase airDatabase) {
        this.airDatabase = airDatabase;
        return this;
    }

    public CrewSchedulingCoachBuilder defineSessionService(WebSessionService sessionService) {
        this.sessionService = sessionService;
        return this;
    }

    public CrewSchedulingCoach makeCrewSchedulingCoach() {
        return new CrewSchedulingCoach(airDatabase, sessionService);
    }
}
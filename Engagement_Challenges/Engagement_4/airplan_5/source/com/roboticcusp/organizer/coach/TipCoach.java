package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.WebTemplate;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays AirPlan tips.
 */
public class TipCoach extends AirCoach {
    protected static final String TRAIL = "/tips";
    protected static final String TITLE= "AirPlan Tips";

    private static final WebTemplate TIPS_TEMPLATE = new WebTemplate("TipsTemplate.html", TipCoach.class);


    public TipCoach(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    private Map<String, String> obtainContentsMap(){
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("deleteMapURL", CoachUtils.generateDeleteMapURL());
        templateMap.put("summaryURL", CoachUtils.generateSummaryURL());
        return templateMap;
    }
    @Override
    protected HttpCoachResponse handleGrab(HttpExchange exchange, String remainingTrail, Airline airline) {
        Map<String, String> contentsMap = obtainContentsMap();
        String contents = TIPS_TEMPLATE.getEngine().replaceTags(contentsMap);
        return takeTemplateResponseWithoutMenuItems(TITLE, contents, airline);
        }
}
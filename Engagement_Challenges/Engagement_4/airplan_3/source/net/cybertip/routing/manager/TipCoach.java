package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.WebTemplate;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays AirPlan tips.
 */
public class TipCoach extends AirCoach {
    protected static final String PATH = "/tips";
    protected static final String TITLE= "AirPlan Tips";

    private static final WebTemplate TIPS_TEMPLATE = new WebTemplate("TipsTemplate.html", TipCoach.class);


    public TipCoach(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    private String grabContents() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("deleteMapURL", CoachUtils.generateDeleteMapURL());
        templateMap.put("summaryURL", CoachUtils.generateSummaryURL());

        return TIPS_TEMPLATE.getEngine().replaceTags(templateMap);
    }
    @Override
    protected HttpCoachResponse handleObtain(HttpExchange exchange, String remainingPath, Airline airline) {
        return fetchTemplateResponseWithoutMenuItems(TITLE, grabContents(), airline);
        }
}
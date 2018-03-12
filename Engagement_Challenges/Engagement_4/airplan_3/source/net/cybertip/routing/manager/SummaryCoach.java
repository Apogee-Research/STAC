package net.cybertip.routing.manager;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.WebTemplate;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays an overall summary of the airline
 */
public class SummaryCoach extends AirCoach {
    public static final String PATH = "/summary";
    public static final String TITLE = "Summary";
    public static final Date CREATION_DATE = new Date();

    private static final WebTemplate SUMMARY_TEMPLATE = new WebTemplate("SummaryTemplate.html", SummaryCoach.class);


    public SummaryCoach(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    private String takeContents(Airline airline) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, ''yy");
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("AirlineName", airline.grabAirlineName());
        templateMap.put("AirlineId", airline.grabID());
        templateMap.put("dateJoined", format.format(airline.grabCreationDate()));
        templateMap.put("numOfMaps", Integer.toString(airline.takeRouteMapIds().size()));
        templateMap.put("currDate", format.format(new Date()));
        templateMap.put("routeMapsURL", ViewRouteMapsCoach.PATH);
        return SUMMARY_TEMPLATE.getEngine().replaceTags(templateMap);
    }
    @Override
    public HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        return fetchTemplateResponseWithoutMenuItems(TITLE, takeContents(airline), airline);
        }
}
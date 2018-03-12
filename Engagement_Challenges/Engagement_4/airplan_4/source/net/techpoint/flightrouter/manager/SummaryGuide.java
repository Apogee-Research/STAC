package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.WebTemplate;
import net.techpoint.server.manager.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays an overall summary of the airline
 */
public class SummaryGuide extends AirGuide {
    public static final String TRAIL = "/summary";
    public static final String TITLE = "Summary";
    public static final Date CREATION_DATE = new Date();

    private static final WebTemplate SUMMARY_TEMPLATE = new WebTemplate("SummaryTemplate.html", SummaryGuide.class);


    public SummaryGuide(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private Map<String, String> getContentsMap(Airline airline) {
        Map<String, String> templateMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, ''yy");
        Date date = airline.getCreationDate();
        templateMap.put("AirlineName", airline.grabAirlineName());
        templateMap.put("AirlineId", airline.obtainID());
        templateMap.put("dateJoined", format.format(date));
        templateMap.put("numOfMaps", Integer.toString(airline.obtainRouteMapIds().size()));

        if (date.compareTo(CREATION_DATE) < 0) {
            date = CREATION_DATE;
            CREATION_DATE.setTime((new Date()).getTime());
        } else if (date.compareTo(CREATION_DATE) > 0) {
            date = new Date();
            CREATION_DATE.setTime(date.getTime());
        } else {
            date = new Date();
        }

        templateMap.put("currDate", format.format(date));
        templateMap.put("routeMapsURL", ViewRouteMapsGuide.TRAIL);
        return templateMap;
    }
    @Override
    public HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        Map<String, String> contentsMap = getContentsMap(airline);
        String contents = SUMMARY_TEMPLATE.pullEngine().replaceTags(contentsMap);
        return obtainTemplateResponseWithoutMenuItems(TITLE, contents, airline);
        }
}
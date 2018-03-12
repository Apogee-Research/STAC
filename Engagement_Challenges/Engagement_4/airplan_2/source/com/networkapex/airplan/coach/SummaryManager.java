package com.networkapex.airplan.coach;

import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.WebTemplate;
import com.networkapex.nethost.WebTemplateBuilder;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.sun.net.httpserver.HttpExchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays an overall summary of the airline
 */
public class SummaryManager extends AirManager {
    public static final String TRAIL = "/summary";
    public static final String TITLE = "Summary";
    public static final Date CREATION_DATE = new Date();

    private static final WebTemplate SUMMARY_TEMPLATE = new WebTemplateBuilder().defineResourceName("SummaryTemplate.html").defineLoader(SummaryManager.class).generateWebTemplate();


    public SummaryManager(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    private Map<String, String> obtainContentsMap(Airline airline) {
        Map<String, String> templateMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, ''yy");
        Date date = airline.getCreationDate();
        templateMap.put("AirlineName", airline.getAirlineName());
        templateMap.put("AirlineId", airline.pullID());
        templateMap.put("dateJoined", format.format(date));
        templateMap.put("numOfMaps", Integer.toString(airline.takeRouteMapIds().size()));

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
        templateMap.put("routeMapsURL", ViewRouteMapsManager.TRAIL);
        return templateMap;
    }
    @Override
    public HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        Map<String, String> contentsMap = obtainContentsMap(airline);
        String contents = SUMMARY_TEMPLATE.takeEngine().replaceTags(contentsMap);
        return getTemplateResponseWithoutMenuItems(TITLE, contents, airline);
        }
}
package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.WebTemplate;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.sun.net.httpserver.HttpExchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays an overall summary of the airline
 */
public class SummaryCoach extends AirCoach {
    public static final String TRAIL = "/summary";
    public static final String TITLE = "Summary";
    public static final Date CREATION_DATE = new Date();

    private static final WebTemplate SUMMARY_TEMPLATE = new WebTemplate("SummaryTemplate.html", SummaryCoach.class);


    public SummaryCoach(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    private Map<String, String> getContentsMap(Airline airline) {
        Map<String, String> templateMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, ''yy");
        Date date = airline.pullCreationDate();
        templateMap.put("AirlineName", airline.obtainAirlineName());
        templateMap.put("AirlineId", airline.getID());
        templateMap.put("dateJoined", format.format(date));
        templateMap.put("numOfMaps", Integer.toString(airline.grabRouteMapIds().size()));

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
        templateMap.put("routeMapsURL", ViewRouteMapsCoach.TRAIL);
        return templateMap;
    }
    @Override
    public HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        Map<String, String> contentsMap = getContentsMap(airline);
        String contents = SUMMARY_TEMPLATE.getEngine().replaceTags(contentsMap);
        return takeTemplateResponseWithoutMenuItems(TITLE, contents, airline);
        }
}
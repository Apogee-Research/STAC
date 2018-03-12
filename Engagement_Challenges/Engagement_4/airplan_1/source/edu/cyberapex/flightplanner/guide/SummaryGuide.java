package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.WebTemplate;
import edu.cyberapex.server.guide.HttpGuideResponse;
import com.sun.net.httpserver.HttpExchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays an overall summary of the airline
 */
public class SummaryGuide extends AirGuide {
    public static final String PATH = "/summary";
    public static final String TITLE = "Summary";
    public static final Date CREATION_DATE = new Date();

    private static final WebTemplate SUMMARY_TEMPLATE = new WebTemplate("SummaryTemplate.html", SummaryGuide.class);


    public SummaryGuide(AirDatabase airDatabase, WebSessionService webSessionService) {
        super(airDatabase, webSessionService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    private String pullContents(Airline airline) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, ''yy");
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("AirlineName", airline.getAirlineName());
        templateMap.put("AirlineId", airline.obtainID());
        templateMap.put("dateJoined", format.format(airline.takeCreationDate()));
        templateMap.put("numOfMaps", Integer.toString(airline.grabRouteMapIds().size()));
        templateMap.put("currDate", format.format(new Date()));
        templateMap.put("routeMapsURL", ViewRouteMapsGuide.PATH);
        return SUMMARY_TEMPLATE.getEngine().replaceTags(templateMap);
    }
    @Override
    public HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline airline) {
        return pullTemplateResponseWithoutMenuItems(TITLE, pullContents(airline), airline);
        }
}
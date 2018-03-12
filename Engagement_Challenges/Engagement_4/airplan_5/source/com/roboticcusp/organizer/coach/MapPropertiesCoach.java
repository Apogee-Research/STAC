package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.ChartProxy;
import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.FlightWeightType;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.framework.RouteMapDensity;
import com.roboticcusp.organizer.framework.RouteMapSize;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.template.TemplateEngine;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class MapPropertiesCoach extends AirCoach {
    protected static final String TRAIL = "/map_properties";
    private static final String TITLE = "Map Properties";
    private static final String WEIGHT_TYPE_FIELD = "weight-type";
    
    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngine(
            "<h2>The following pages allow you to verify the correctness of the uploaded map.</h2>" +
            "<h2>{{routeMapName}}</h2>" +
            "<p>Select a weight type to be used in the properties calculation.</p>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
                    "<label for=\"" + WEIGHT_TYPE_FIELD + "\"> Select a Weight Type: </label>" +
                    "<select name=\"" + WEIGHT_TYPE_FIELD + "\">" +
                        "{{weightTypeChoices}}" +
                    "</select> <br />" +
            "<input type=\"submit\" value=\"Get the List of Properties\" name=\"submit\" id=\"submit\" />" +
            "</form>"

    );
    private static final TemplateEngine RESULTS_ENGINE = new TemplateEngine(
            "<h4>This page allows you to verify the correctness of the uploaded map." +
                " You may delete this route map if it is not as intended.</h4>" +
                "<a href=\"{{deleteMapURL}}\" style=\"text-decoration:none\"> " +
                    "<input type=\"button\" value=\"Delete the Map\" name=\"submit\">" +
                "</a>" +
                "<a href=\"{{graphMatrix}}\" style=\"text-decoration:none\"> " +
                                "<input type=\"button\" value=\"Next\" name=\"submit\">"+
                            "</a>" +
            "<h2>{{routeMapName}}'s Properties</h2>" +
            "<p>These properties are related to the \"{{lowerCaseWeightLabel}}\" weight type. </p>" +
            "<ul>" +
            "   {{properties}}" +
            "</ul>"

    );

    private static final TemplateEngine PROPERTY_ENGINE = new TemplateEngine(
            "<li>{{propertyLabel}}: {{propertyValue}} </li>"
    );
    

    private CellFormatter formatter = new CellFormatter(10);
    private ParameterConductor paramCoach = new ParameterConductor();

    private int valueLength = 19;

    @Override
    public String getTrail() {
        return TRAIL;
    }

    public MapPropertiesCoach(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
        // NOTE: should_adjust here is spelled with the number one (1) instead of the letter l,
        // so that the value is actually controlled by the jinjafied default value
        paramCoach.set("shou1d_adjust", false);
        paramCoach.set("adjustment_factor", 2);
    }


    /**
     * URL structure - /map_properties/<route map id>
     * @param remainingTrail /map_properties/<route map id>
     * @param airline
     * @return RouteMap associated with the id in the remainingPath the the provided airline,
     *          or null if no route map is found
     */
    private RouteMap pullRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        String[] trailParts = remainingTrail.split("/");
        if (trailParts.length == 2) {
            // the route map id should be the second element
            String idStr = trailParts[1];
            if (NumberUtils.isNumber(idStr)) {
                return airline.pullRouteMap(Integer.parseInt(idStr));
            }
        }
        return null;
    }

    /**
     *
     * @param routeMap
     * @return
     */
    private String getContentsForObtain(RouteMap routeMap) {
        Map<String, String> choicesDict = new HashMap<>();
        choicesDict.put("routeMapName", routeMap.grabName());
        choicesDict.put("weightTypeChoices", CoachUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);
        return INPUT_PAGE_ENGINE.replaceTags(choicesDict);
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = pullRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }
            return takeTemplateResponseWithoutMenuItems(TITLE, getContentsForObtain(routeMap), airline);
        } catch (NumberFormatException e) {
            return pullTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    /**
     *
     * @param routeMap
     * @param weightType
     * @return
     */
    private String fetchContentsForPost(RouteMap routeMap, FlightWeightType weightType) throws AirException {

        valueLength = 19;

        // create the properties string
        StringBuilder properties = new StringBuilder();
        ChartProxy chartProxy = new ChartProxy(routeMap, weightType);
        String connectedString= chartProxy.getConnected();

        properties.append(composeHTMLForProperty("Connected", format(connectedString)));

        String bipartiteString = chartProxy.fetchBipartite();
        properties.append(composeHTMLForProperty("Bipartite", format(bipartiteString)));

        RouteMapSize size = chartProxy.describeSize();
        properties.append(composeHTMLForProperty("Size", format(size.grabDescription())));

        RouteMapDensity density = chartProxy.describeDensity();
        properties.append(composeHTMLForProperty("Density", format(density.takeDescription())));

        String twoConnected = chartProxy.kConnected(2);
        properties.append(composeHTMLForProperty("Is 2-connected", format(twoConnected)));

        String threeConnected = chartProxy.kConnected(3);
        properties.append(composeHTMLForProperty("Is 3-connected", format(threeConnected)));

        String fourConnected = chartProxy.kConnected(4);
        properties.append(composeHTMLForProperty("Is 4-connected", format(fourConnected)));

        String fiveConnected = chartProxy.kConnected(5);
        properties.append(composeHTMLForProperty("Is 5-connected", format(fiveConnected)));

        String regular = chartProxy.fetchRegular();
        properties.append(composeHTMLForProperty("Regular", format(regular)));

        String eulerian = chartProxy.grabEulerian();
        properties.append(composeHTMLForProperty("Eulerian", format(eulerian)));

        // add the properties string and the route map name to the overall engine
        Map<String, String> results = new HashMap<>();
        String formattedRouteName = formatter.format(routeMap.grabName(), 10, CellFormatter.Justification.CENTER, false);
        results.put("routeMapName", formattedRouteName);
        String formattedWeightLabel = formatter.format(weightType.grabDescription().toLowerCase(), 20, CellFormatter.Justification.CENTER, false);
        results.put("lowerCaseWeightLabel", formattedWeightLabel);
        results.put("properties", properties.toString());
        results.put("graphMatrix", CoachUtils.generateRouteMapMatrixURL(routeMap));
        results.put("deleteMapURL", CoachUtils.generateDeleteMapURL());
        return RESULTS_ENGINE.replaceTags(results);
    }

    private String format(String value){
        String formatted = value;
        if (value.length() < valueLength){
            formatted = formatter.format(value, valueLength, CellFormatter.Justification.CENTER, (boolean) paramCoach.obtain("should_adjust"));
        } else {
            formatted = formatter.format(value, valueLength*=(int) paramCoach.obtain("adjustment_factor"), CellFormatter.Justification.CENTER,
                                         (boolean) paramCoach.obtain("should_adjust"));
        }

        return formatted;
    }

    private String composeHTMLForProperty(String propertyLabel, String propertyValue) {
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("propertyLabel", propertyLabel);
        propertiesMap.put("propertyValue", propertyValue);
        return PROPERTY_ENGINE.replaceTags(propertiesMap);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap = pullRouteMapFromTrail(remainingTrail, airline);

        if (routeMap == null) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }
        String data = MultipartHelper.fetchMultipartFieldContent(httpExchange, WEIGHT_TYPE_FIELD);

        if (data == null || data.isEmpty()) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "A weight type was not selected.");
        }


        FlightWeightType weightType = FlightWeightType.fromString(data);

        if (weightType == null) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "The weight type selected is not a known weight type.");
        }

        try {
            HttpCoachResponse response = takeTemplateResponseWithoutMenuItems(TITLE, fetchContentsForPost(routeMap, weightType), airline);
            return response;
        } catch (AirException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "Unable to create a list of properties for this route map.");
        }

    }

    @Override
    protected String grabDisplayName(Airline participant){
        return format(participant.obtainAirlineName());
    }
}
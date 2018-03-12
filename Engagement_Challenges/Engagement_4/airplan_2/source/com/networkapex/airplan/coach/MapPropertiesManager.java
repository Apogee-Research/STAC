package com.networkapex.airplan.coach;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.GraphTranslator;
import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.FlightWeightType;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.prototype.RouteMapDensity;
import com.networkapex.airplan.prototype.RouteMapSize;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class MapPropertiesManager extends AirManager {
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
    private ParameterManager paramManager = new ParameterManager();

    private int valueLength = 19;

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    public MapPropertiesManager(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
        // NOTE: should_adjust here is spelled with the number one (1) instead of the letter l,
        // so that the value is actually controlled by the jinjafied default value
        paramManager.set("shou1d_adjust", false);
        paramManager.set("adjustment_factor", 2);
    }


    /**
     * URL structure - /map_properties/<route map id>
     * @param remainingTrail /map_properties/<route map id>
     * @param airline
     * @return RouteMap associated with the id in the remainingPath the the provided airline,
     *          or null if no route map is found
     */
    private RouteMap grabRouteMapFromTrail(String remainingTrail, Airline airline) throws NumberFormatException {
        String[] trailParts = remainingTrail.split("/");
        if (trailParts.length == 2) {
            // the route map id should be the second element
            String idStr = trailParts[1];
            if (NumberUtils.isNumber(idStr)) {
                return airline.getRouteMap(Integer.parseInt(idStr));
            }
        }
        return null;
    }

    /**
     *
     * @param routeMap
     * @return
     */
    private String pullContentsForPull(RouteMap routeMap) {
        Map<String, String> choicesDict = new HashMap<>();
        choicesDict.put("routeMapName", routeMap.takeName());
        choicesDict.put("weightTypeChoices", ManagerUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);
        return INPUT_PAGE_ENGINE.replaceTags(choicesDict);
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = grabRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }
            return getTemplateResponseWithoutMenuItems(TITLE, pullContentsForPull(routeMap), airline);
        } catch (NumberFormatException e) {
            return obtainTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    /**
     *
     * @param routeMap
     * @param weightType
     * @return
     */
    private String grabContentsForPost(RouteMap routeMap, FlightWeightType weightType) throws AirRaiser {

        valueLength = 19;

        // create the properties string
        StringBuilder properties = new StringBuilder();
        GraphTranslator graphTranslator = new GraphTranslator(routeMap, weightType);
        String connectedString= graphTranslator.fetchConnected();

        properties.append(generateHTMLForProperty("Connected", format(connectedString)));

        String bipartiteString = graphTranslator.takeBipartite();
        properties.append(generateHTMLForProperty("Bipartite", format(bipartiteString)));

        RouteMapSize size = graphTranslator.describeSize();
        properties.append(generateHTMLForProperty("Size", format(size.obtainDescription())));

        RouteMapDensity density = graphTranslator.describeDensity();
        properties.append(generateHTMLForProperty("Density", format(density.obtainDescription())));

        String twoConnected = graphTranslator.kConnected(2);
        properties.append(generateHTMLForProperty("Is 2-connected", format(twoConnected)));

        String threeConnected = graphTranslator.kConnected(3);
        properties.append(generateHTMLForProperty("Is 3-connected", format(threeConnected)));

        String fourConnected = graphTranslator.kConnected(4);
        properties.append(generateHTMLForProperty("Is 4-connected", format(fourConnected)));

        String fiveConnected = graphTranslator.kConnected(5);
        properties.append(generateHTMLForProperty("Is 5-connected", format(fiveConnected)));

        String regular = graphTranslator.obtainRegular();
        properties.append(generateHTMLForProperty("Regular", format(regular)));

        String eulerian = graphTranslator.grabEulerian();
        properties.append(generateHTMLForProperty("Eulerian", format(eulerian)));

        // add the properties string and the route map name to the overall engine
        Map<String, String> results = new HashMap<>();
        String formattedRouteName = formatter.format(routeMap.takeName(), 10, CellFormatter.Justification.CENTER, false);
        results.put("routeMapName", formattedRouteName);
        String formattedWeightLabel = formatter.format(weightType.getDescription().toLowerCase(), 20, CellFormatter.Justification.CENTER, false);
        results.put("lowerCaseWeightLabel", formattedWeightLabel);
        results.put("properties", properties.toString());
        results.put("graphMatrix", ManagerUtils.generateRouteMapMatrixURL(routeMap));
        results.put("deleteMapURL", ManagerUtils.generateDeleteMapURL());
        return RESULTS_ENGINE.replaceTags(results);
    }

    private String format(String value){
        String formatted = value;
        if (value.length() < valueLength){
            formatted = formatter.format(value, valueLength, CellFormatter.Justification.CENTER, (boolean) paramManager.take("should_adjust"));
        } else {
            formatted = formatter.format(value, valueLength*=(int) paramManager.take("adjustment_factor"), CellFormatter.Justification.CENTER,
                                         (boolean) paramManager.take("should_adjust"));
        }

        return formatted;
    }

    private String generateHTMLForProperty(String propertyLabel, String propertyValue) {
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("propertyLabel", propertyLabel);
        propertiesMap.put("propertyValue", propertyValue);
        return PROPERTY_ENGINE.replaceTags(propertiesMap);
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap = grabRouteMapFromTrail(remainingTrail, airline);

        if (routeMap == null) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }
        String data = MultipartHelper.grabMultipartFieldContent(httpExchange, WEIGHT_TYPE_FIELD);

        if (data == null || data.isEmpty()) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "A weight type was not selected.");
        }


        FlightWeightType weightType = FlightWeightType.fromString(data);

        if (weightType == null) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "The weight type selected is not a known weight type.");
        }

        try {
            HttpManagerResponse response = getTemplateResponseWithoutMenuItems(TITLE, grabContentsForPost(routeMap, weightType), airline);
            return response;
        } catch (AirRaiser e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "Unable to create a list of properties for this route map.");
        }

    }

    @Override
    protected String grabDisplayName(Airline person){
        return format(person.getAirlineName());
    }
}
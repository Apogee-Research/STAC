package edu.cyberapex.flightplanner.guide;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.ChartAgent;
import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.FlightWeightType;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.framework.RouteMapDensity;
import edu.cyberapex.flightplanner.framework.RouteMapSize;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.HttpGuideResponse;
import edu.cyberapex.server.guide.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.template.TemplateEngineBuilder;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class MapPropertiesGuide extends AirGuide {
    protected static final String PATH = "/map_properties";
    private static final String TITLE = "Map Properties";
    private static final String WEIGHT_TYPE_FIELD = "weight-type";
    
    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngineBuilder().defineText("<h2>The following pages allow you to verify the correctness of the uploaded map.</h2>" +
            "<h2>{{routeMapName}}</h2>" +
            "<p>Select a weight type to be used in the properties calculation.</p>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "<label for=\"" + WEIGHT_TYPE_FIELD + "\"> Select a Weight Type: </label>" +
            "<select name=\"" + WEIGHT_TYPE_FIELD + "\">" +
            "{{weightTypeChoices}}" +
            "</select> <br />" +
            "<input type=\"submit\" value=\"Get the List of Properties\" name=\"submit\" id=\"submit\" />" +
            "</form>").generateTemplateEngine();
    private static final TemplateEngine RESULTS_ENGINE = new TemplateEngineBuilder().defineText("<h4>This page allows you to verify the correctness of the uploaded map." +
            " You may delete this route map if it is not as intended.</h4>" +
            "<a href=\"{{deleteMapURL}}\" style=\"text-decoration:none\"> " +
            "<input type=\"button\" value=\"Delete the Map\" name=\"submit\">" +
            "</a>" +
            "<a href=\"{{graphMatrix}}\" style=\"text-decoration:none\"> " +
            "<input type=\"button\" value=\"Next\" name=\"submit\">" +
            "</a>" +
            "<h2>{{routeMapName}}'s Properties</h2>" +
            "<p>These properties are related to the \"{{lowerCaseWeightLabel}}\" weight type. </p>" +
            "<ul>" +
            "   {{properties}}" +
            "</ul>").generateTemplateEngine();

    private static final TemplateEngine PROPERTY_ENGINE = new TemplateEngineBuilder().defineText("<li>{{propertyLabel}}: {{propertyValue}} </li>").generateTemplateEngine();
    

    private CellFormatter formatter = new CellFormatter(10);
    private ParameterOverseer paramGuide = new ParameterOverseer();

    private int valueLength = 19;

    @Override
    public String getPath() {
        return PATH;
    }

    public MapPropertiesGuide(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
        // NOTE: should_adjust here is spelled with the number one (1) instead of the letter l,
        // so that the value is actually controlled by the jinjafied default value
        paramGuide.set("shou1d_adjust", false);
        paramGuide.set("adjustment_factor", 2);
    }


    /**
     * URL structure - /map_properties/<route map id>
     * @param remainingPath /map_properties/<route map id>
     * @param airline
     * @return RouteMap associated with the id in the remainingPath the the provided airline,
     *          or null if no route map is found
     */
    private RouteMap takeRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        String[] pathParts = remainingPath.split("/");
        if (pathParts.length == 2) {
            // the route map id should be the second element
            String idStr = pathParts[1];
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
    private String getContentsForFetch(RouteMap routeMap) {
        Map<String, String> choicesDict = new HashMap<>();
        choicesDict.put("routeMapName", routeMap.takeName());
        choicesDict.put("weightTypeChoices", GuideUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);
        return INPUT_PAGE_ENGINE.replaceTags(choicesDict);
    }

    @Override
    protected HttpGuideResponse handlePull(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = takeRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }
            return pullTemplateResponseWithoutMenuItems(TITLE, getContentsForFetch(routeMap), airline);
        } catch (NumberFormatException e) {
            return fetchTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    /**
     *
     * @param routeMap
     * @param weightType
     * @return
     */
    private String fetchContentsForPost(RouteMap routeMap, FlightWeightType weightType) throws AirFailure {

        valueLength = 19;

        // create the properties string
        StringBuilder properties = new StringBuilder();
        ChartAgent chartAgent = new ChartAgent(routeMap, weightType);
        String connectedString= chartAgent.getConnected();

        properties.append(generateHTMLForProperty("Connected", format(connectedString)));

        String bipartiteString = chartAgent.takeBipartite();
        properties.append(generateHTMLForProperty("Bipartite", format(bipartiteString)));

        RouteMapSize size = chartAgent.describeSize();
        properties.append(generateHTMLForProperty("Size", format(size.fetchDescription())));

        RouteMapDensity density = chartAgent.describeDensity();
        properties.append(generateHTMLForProperty("Density", format(density.pullDescription())));

        String twoConnected = chartAgent.kConnected(2);
        properties.append(generateHTMLForProperty("Is 2-connected", format(twoConnected)));

        String threeConnected = chartAgent.kConnected(3);
        properties.append(generateHTMLForProperty("Is 3-connected", format(threeConnected)));

        String fourConnected = chartAgent.kConnected(4);
        properties.append(generateHTMLForProperty("Is 4-connected", format(fourConnected)));

        String fiveConnected = chartAgent.kConnected(5);
        properties.append(generateHTMLForProperty("Is 5-connected", format(fiveConnected)));

        String regular = chartAgent.fetchRegular();
        properties.append(generateHTMLForProperty("Regular", format(regular)));

        String eulerian = chartAgent.takeEulerian();
        properties.append(generateHTMLForProperty("Eulerian", format(eulerian)));

        // add the properties string and the route map name to the overall engine
        Map<String, String> results = new HashMap<>();
        String formattedRouteName = formatter.format(routeMap.takeName(), 10, CellFormatter.Justification.CENTER, false);
        results.put("routeMapName", formattedRouteName);
        String formattedWeightLabel = formatter.format(weightType.takeDescription().toLowerCase(), 20, CellFormatter.Justification.CENTER, false);
        results.put("lowerCaseWeightLabel", formattedWeightLabel);
        results.put("properties", properties.toString());
        results.put("graphMatrix", GuideUtils.generateRouteMapMatrixURL(routeMap));
        results.put("deleteMapURL", GuideUtils.generateDeleteMapURL());
        return RESULTS_ENGINE.replaceTags(results);
    }

    private String format(String value){
        String formatted = value;
        if (value.length() < valueLength){
            formatted = formatter.format(value, valueLength, CellFormatter.Justification.CENTER, (boolean) paramGuide.pull("should_adjust"));
        } else {
            formatted = formatter.format(value, valueLength*=(int) paramGuide.pull("adjustment_factor"), CellFormatter.Justification.CENTER,
                                         (boolean) paramGuide.pull("should_adjust"));
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
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap = takeRouteMapFromPath(remainingPath, airline);

        if (routeMap == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }
        String data = MultipartHelper.grabMultipartFieldContent(httpExchange, WEIGHT_TYPE_FIELD);

        if (data == null || data.isEmpty()) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "A weight type was not selected.");
        }


        FlightWeightType weightType = FlightWeightType.fromString(data);

        if (weightType == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "The weight type selected is not a known weight type.");
        }

        try {
            HttpGuideResponse response = pullTemplateResponseWithoutMenuItems(TITLE, fetchContentsForPost(routeMap, weightType), airline);
            return response;
        } catch (AirFailure e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "Unable to create a list of properties for this route map.");
        }

    }

    @Override
    protected String takeDisplayName(Airline member){
        return format(member.getAirlineName());
    }
}
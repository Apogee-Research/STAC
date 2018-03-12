package net.cybertip.routing.manager;

import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.GraphDelegate;
import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.FlightWeightType;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.framework.RouteMapDensity;
import net.cybertip.routing.framework.RouteMapSize;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class MapPropertiesCoach extends AirCoach {
    protected static final String PATH = "/map_properties";
    private static final String TITLE = "Map Properties";
    private static final String WEIGHT_TYPE_FIELD = "weight-type";
    
    private static final TemplateEngine INPUT_PAGE_ENGINE = new TemplateEngineBuilder().setText("<h2>The following pages allow you to verify the correctness of the uploaded map.</h2>" +
            "<h2>{{routeMapName}}</h2>" +
            "<p>Select a weight type to be used in the properties calculation.</p>" +
            "<form action=\"#\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "<label for=\"" + WEIGHT_TYPE_FIELD + "\"> Select a Weight Type: </label>" +
            "<select name=\"" + WEIGHT_TYPE_FIELD + "\">" +
            "{{weightTypeChoices}}" +
            "</select> <br />" +
            "<input type=\"submit\" value=\"Get the List of Properties\" name=\"submit\" id=\"submit\" />" +
            "</form>").makeTemplateEngine();
    private static final TemplateEngine RESULTS_ENGINE = new TemplateEngineBuilder().setText("<h4>This page allows you to verify the correctness of the uploaded map." +
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
            "</ul>").makeTemplateEngine();

    private static final TemplateEngine PROPERTY_ENGINE = new TemplateEngineBuilder().setText("<li>{{propertyLabel}}: {{propertyValue}} </li>").makeTemplateEngine();
    

    private CellFormatter formatter = new CellFormatter(10);
    private ParameterOverseer paramCoach = new ParameterOverseer();

    private int valueLength = 19;

    @Override
    public String grabPath() {
        return PATH;
    }

    public MapPropertiesCoach(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
        // NOTE: should_adjust here is spelled with the number one (1) instead of the letter l,
        // so that the value is actually controlled by the jinjafied default value
        paramCoach.assign("shou1d_adjust", false);
        paramCoach.assign("adjustment_factor", 2);
    }


    /**
     * URL structure - /map_properties/<route map id>
     * @param remainingPath /map_properties/<route map id>
     * @param airline
     * @return RouteMap associated with the id in the remainingPath the the provided airline,
     *          or null if no route map is found
     */
    private RouteMap fetchRouteMapFromPath(String remainingPath, Airline airline) throws NumberFormatException {
        String[] pathParts = remainingPath.split("/");
        if (pathParts.length == 2) {
            // the route map id should be the second element
            String idStr = pathParts[1];
            if (NumberUtils.isNumber(idStr)) {
                return airline.obtainRouteMap(Integer.parseInt(idStr));
            }
        }
        return null;
    }

    /**
     *
     * @param routeMap
     * @return
     */
    private String getContentsForGrab(RouteMap routeMap) {
        Map<String, String> choicesDict = new HashMap<>();
        choicesDict.put("routeMapName", routeMap.pullName());
        choicesDict.put("weightTypeChoices", CoachUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);
        return INPUT_PAGE_ENGINE.replaceTags(choicesDict);
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline airline) {
        try {
            RouteMap routeMap = fetchRouteMapFromPath(remainingPath, airline);

            if (routeMap == null) {
                return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }
            return fetchTemplateResponseWithoutMenuItems(TITLE, getContentsForGrab(routeMap), airline);
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
    private String getContentsForPost(RouteMap routeMap, FlightWeightType weightType) throws AirTrouble {

        valueLength = 19;

        // create the properties string
        StringBuilder properties = new StringBuilder();
        GraphDelegate graphDelegate = new GraphDelegate(routeMap, weightType);
        String connectedString= graphDelegate.takeConnected();

        properties.append(makeHTMLForProperty("Connected", format(connectedString)));

        String bipartiteString = graphDelegate.fetchBipartite();
        properties.append(makeHTMLForProperty("Bipartite", format(bipartiteString)));

        RouteMapSize size = graphDelegate.describeSize();
        properties.append(makeHTMLForProperty("Size", format(size.obtainDescription())));

        RouteMapDensity density = graphDelegate.describeDensity();
        properties.append(makeHTMLForProperty("Density", format(density.grabDescription())));

        String twoConnected = graphDelegate.kConnected(2);
        properties.append(makeHTMLForProperty("Is 2-connected", format(twoConnected)));

        String threeConnected = graphDelegate.kConnected(3);
        properties.append(makeHTMLForProperty("Is 3-connected", format(threeConnected)));

        String fourConnected = graphDelegate.kConnected(4);
        properties.append(makeHTMLForProperty("Is 4-connected", format(fourConnected)));

        String fiveConnected = graphDelegate.kConnected(5);
        properties.append(makeHTMLForProperty("Is 5-connected", format(fiveConnected)));

        String regular = graphDelegate.obtainRegular();
        properties.append(makeHTMLForProperty("Regular", format(regular)));

        String eulerian = graphDelegate.grabEulerian();
        properties.append(makeHTMLForProperty("Eulerian", format(eulerian)));

        // add the properties string and the route map name to the overall engine
        Map<String, String> results = new HashMap<>();
        String formattedRouteName = formatter.format(routeMap.pullName(), 10, CellFormatter.Justification.CENTER, false);
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

    private String makeHTMLForProperty(String propertyLabel, String propertyValue) {
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("propertyLabel", propertyLabel);
        propertiesMap.put("propertyValue", propertyValue);
        return PROPERTY_ENGINE.replaceTags(propertiesMap);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        RouteMap routeMap = fetchRouteMapFromPath(remainingPath, airline);

        if (routeMap == null) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }
        String data = MultipartHelper.grabMultipartFieldContent(httpExchange, WEIGHT_TYPE_FIELD);

        if (data == null || data.isEmpty()) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "A weight type was not selected.");
        }


        FlightWeightType weightType = FlightWeightType.fromString(data);

        if (weightType == null) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "The weight type selected is not a known weight type.");
        }

        try {
            HttpCoachResponse response = fetchTemplateResponseWithoutMenuItems(TITLE, getContentsForPost(routeMap, weightType), airline);
            return response;
        } catch (AirTrouble e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "Unable to create a list of properties for this route map.");
        }

    }

    @Override
    protected String grabDisplayName(Airline member){
        return format(member.grabAirlineName());
    }
}
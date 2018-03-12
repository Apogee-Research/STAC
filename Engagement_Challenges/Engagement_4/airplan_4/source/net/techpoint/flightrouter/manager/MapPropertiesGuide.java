package net.techpoint.flightrouter.manager;

import net.techpoint.flightrouter.AirFailure;
import net.techpoint.flightrouter.SchemeAdapter;
import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.prototype.FlightWeightType;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.prototype.RouteMapDensity;
import net.techpoint.flightrouter.prototype.RouteMapSize;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.HttpGuideResponse;
import net.techpoint.server.manager.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class MapPropertiesGuide extends AirGuide {
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
    

    private CellFormatter formatter = new CellFormatterBuilder().assignLength(10).formCellFormatter();
    private ParameterManager paramGuide = new ParameterManagerBuilder().formParameterManager();

    private int valueLength = 19;

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    public MapPropertiesGuide(AirDatabase database, WebSessionService webSessionService) {
        super(database, webSessionService);
        // NOTE: should_adjust here is spelled with the number one (1) instead of the letter l,
        // so that the value is actually controlled by the jinjafied default value
        paramGuide.fix("shou1d_adjust", false);
        paramGuide.fix("adjustment_factor", 2);
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
                return airline.grabRouteMap(Integer.parseInt(idStr));
            }
        }
        return null;
    }

    /**
     *
     * @param routeMap
     * @return
     */
    private String grabContentsForPull(RouteMap routeMap) {
        Map<String, String> choicesDict = new HashMap<>();
        choicesDict.put("routeMapName", routeMap.fetchName());
        choicesDict.put("weightTypeChoices", GuideUtils.FLIGHT_WEIGHT_TYPE_OPTIONS);
        return INPUT_PAGE_ENGINE.replaceTags(choicesDict);
    }

    @Override
    protected HttpGuideResponse handleObtain(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        try {
            RouteMap routeMap = grabRouteMapFromTrail(remainingTrail, airline);

            if (routeMap == null) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
            }
            return obtainTemplateResponseWithoutMenuItems(TITLE, grabContentsForPull(routeMap), airline);
        } catch (NumberFormatException e) {
            return takeTemplateErrorResponse("Error while parsing the route map id. " + e.getMessage(), airline);
        }
    }

    /**
     *
     * @param routeMap
     * @param weightType
     * @return
     */
    private String grabContentsForPost(RouteMap routeMap, FlightWeightType weightType) throws AirFailure {

        valueLength = 19;

        // create the properties string
        StringBuilder properties = new StringBuilder();
        SchemeAdapter schemeAdapter = new SchemeAdapter(routeMap, weightType);
        paramGuide.fix("adjustment_factor", 1);
        String connectedString= schemeAdapter.obtainConnected();

        properties.append(formHTMLForProperty("Connected", format(connectedString)));

        String bipartiteString = schemeAdapter.obtainBipartite();
        properties.append(formHTMLForProperty("Bipartite", format(bipartiteString)));

        RouteMapSize size = schemeAdapter.describeSize();
        properties.append(formHTMLForProperty("Size", format(size.takeDescription())));

        RouteMapDensity density = schemeAdapter.describeDensity();
        properties.append(formHTMLForProperty("Density", format(density.takeDescription())));

        String twoConnected = schemeAdapter.kConnected(2);
        properties.append(formHTMLForProperty("Is 2-connected", format(twoConnected)));

        String threeConnected = schemeAdapter.kConnected(3);
        properties.append(formHTMLForProperty("Is 3-connected", format(threeConnected)));

        String fourConnected = schemeAdapter.kConnected(4);
        properties.append(formHTMLForProperty("Is 4-connected", format(fourConnected)));

        String fiveConnected = schemeAdapter.kConnected(5);
        properties.append(formHTMLForProperty("Is 5-connected", format(fiveConnected)));

        String regular = schemeAdapter.obtainRegular();
        properties.append(formHTMLForProperty("Regular", format(regular)));

        String eulerian = schemeAdapter.takeEulerian();
        properties.append(formHTMLForProperty("Eulerian", format(eulerian)));

        // add the properties string and the route map name to the overall engine
        Map<String, String> results = new HashMap<>();
        String formattedRouteName = formatter.format(routeMap.fetchName(), 10, CellFormatter.Justification.CENTER, false);
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
            formatted = formatter.format(value, valueLength, CellFormatter.Justification.CENTER, (boolean) paramGuide.get("should_adjust"));
        } else {
            formatted = formatter.format(value, valueLength*=(int) paramGuide.get("adjustment_factor"), CellFormatter.Justification.CENTER,
                                         (boolean) paramGuide.get("should_adjust"));
        }

        return formatted;
    }

    private String formHTMLForProperty(String propertyLabel, String propertyValue) {
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("propertyLabel", propertyLabel);
        propertiesMap.put("propertyValue", propertyValue);
        return PROPERTY_ENGINE.replaceTags(propertiesMap);
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        RouteMap routeMap = grabRouteMapFromTrail(remainingTrail, airline);

        if (routeMap == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "This route map does not exist.");
        }
        String data = MultipartHelper.pullMultipartFieldContent(httpExchange, WEIGHT_TYPE_FIELD);

        if (data == null || data.isEmpty()) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "A weight type was not selected.");
        }


        FlightWeightType weightType = FlightWeightType.fromString(data);

        if (weightType == null) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "The weight type selected is not a known weight type.");
        }

        try {
            HttpGuideResponse response = obtainTemplateResponseWithoutMenuItems(TITLE, grabContentsForPost(routeMap, weightType), airline);
            return response;
        } catch (AirFailure e) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                    "Unable to create a list of properties for this route map.");
        }

    }

    @Override
    protected String getDisplayName(Airline user){
        return format(user.grabAirlineName());
    }
}
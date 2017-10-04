package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handler that is responsible for both processing a GET request to
 * generate a template-based view to enable the user to submit a file
 * of BSSID values to set their initial location but also for
 * processing the POST submission to read the file and set the Person's
 * initial location.
 * The GET request is managed through the parent class abstract methods
 * and the POST is handled by the <code>handlePost</code> method.
 */
public class InitialLocationHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/location/set";

    private static final String TITLE = "Set Initial Location";

    private static final String TEMPLATE_RESOURCE = "basictemplate.html";

    private static final String FIELD_NAME = "bssids";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<form action=\"{{path}}\" method=\"post\" enctype=\"multipart/form-data\">" + "    Enter a list of BSSIDs to set your current location.  If you don't provide any BSSIDs, " + "    then your previous location will be assumed.  If your location is not currently set, " + "    then a list of BSSIDs <em>MUST</em> be specified.<br/>" + "    <label for=\"text\">Comma separated list of BSSIDs:</label>" + "    <input type=\"text\" id=\"text\" name=\"" + FIELD_NAME + "\" autofocus {{required}} " + "        placeholder=\"Enter comma separated list of BSSIDs with no spaces\"/>" + "    <br/>" + "    <input type=\"submit\" value=\"Lookup Location\" name=\"submit\" id=\"submit\" />" + "</form>");

    private final LocationService locationService;

    private final String destinationPath;

    public InitialLocationHandler(SnapService snapService, LocationService locationService, String destinationPath) {
        super(snapService);
        if (locationService == null) {
            throw new  IllegalArgumentException("LocationService may not be null");
        }
        if (StringUtils.isBlank(destinationPath)) {
            throw new  IllegalArgumentException("Destination path may not be empty or null");
        }
        this.locationService = locationService;
        this.destinationPath = destinationPath;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    protected String getTemplateResource() {
        return TEMPLATE_RESOURCE;
    }

    @Override
    protected String getContents(SnapContext context) {
        Map<String, String> map = new  HashMap();
        map.put("path", getPath());
        map.put("required", "");
        if (Location.UNKNOWN.equals(context.getActivePerson().getLocation())) {
            getContentsHelper(map);
        }
        return TEMPLATE.replaceTags(map);
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        String content = MultipartHelper.getMultipartFieldContent(httpExchange, FIELD_NAME);
        Location location = null;
        if (StringUtils.isBlank(content)) {
            Person person = getPerson(httpExchange);
            if (person != null) {
                location = person.getLocation();
            }
        } else {
            Set<String> accessPoints = parseAccessPoints(content);
            location = locationService.getLocation(accessPoints);
        }
        if (location == null) {
            throw new  IllegalArgumentException("List of Access Points is either malformed or else does not match a valid location");
        }
        String destination = destinationPath + "?lid=" + location.getIdentity();
        return getRedirectResponse(destination);
    }

    /**
     * Parses the client provided access point info (just BSSIDs)
     *
     * @param content from the form: a collection of BSSIDs
     * @return Set of provided BSSIDs
     */
    private static Set<String> parseAccessPoints(String content) {
        if (StringUtils.isBlank(content)) {
            throw new  IllegalArgumentException("Location setting requires list of BSSIDs");
        }
        String[] parts = content.split("\\s*[\\s,]\\s*");
        Set<String> bssids = new  HashSet();
        Collections.addAll(bssids, parts);
        return bssids;
    }

    private void getContentsHelper(Map<String, String> map) {
        map.put("required", "required");
    }
}

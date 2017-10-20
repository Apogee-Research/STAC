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
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Handler that processes a GET request to generate a template-based
 * view to confirm the setting of the Person's current location.
 */
public class LocationConfirmHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/location/confirm";

    private static final String TITLE = "Confirm Initial Location";

    private static final String TEMPLATE_RESOURCE = "basictemplate.html";

    private static final String FIELD_NAME = "identity";

    private static final TemplateEngine CONFIRM_TEMPLATE = new  TemplateEngine("<form action=\"{{path}}\" method=\"post\" enctype=\"multipart/form-data\">" + "    Your current location will be set to \"{{city}}\".<br/>" + "    <input type=\"hidden\" name=\"{{identity}}\" value=\"{{lid}}\">" + "    <input type=\"submit\" value=\"Confirm Location Setting\">" + "</form>");

    private static final TemplateEngine FIXED_TEMPLATE = new  TemplateEngine("<form action=\"{{path}}\" method=\"post\" enctype=\"multipart/form-data\">" + "    Your current location cannot be modified to \"{{city}}\" because that" + "    change would exceed your daily location change request quota." + "    Your previous location \"{{oldCity}}\" will be maintained.<br/>" + "    <input type=\"hidden\" name=\"{{identity}}\" value=\"{{lid}}\">" + "    <input type=\"submit\" value=\"Continue\">" + "</form>");

    private final LocationService locationService;

    public LocationConfirmHandler(SnapService snapService, LocationService locationService) {
        super(snapService);
        if (locationService == null) {
            throw new  IllegalArgumentException("LocationService may not be null");
        }
        this.locationService = locationService;
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
        Person person = context.getActivePerson();
        Location previousLocation = person.getLocation();
        String identity = context.getUrlParam("lid");
        if (StringUtils.isBlank(identity)) {
            identity = previousLocation.getIdentity();
        }
        Location location = locationService.getLocation(identity);
        if (location == null) {
            throw new  IllegalArgumentException("Location does not exist for identity " + identity);
        }
        TemplateEngine templateEngine;
        if (location.equals(person.getLocation()) || getSnapService().canUpdateLocation(person)) {
            templateEngine = CONFIRM_TEMPLATE;
        } else {
            templateEngine = FIXED_TEMPLATE;
            identity = previousLocation.getIdentity();
        }
        Map<String, String> map = new  HashMap();
        map.put("identity", FIELD_NAME);
        map.put("path", getPath());
        map.put("lid", identity);
        map.put("city", location.getCity());
        map.put("oldCity", previousLocation.getCity());
        return templateEngine.replaceTags(map);
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person person = getPerson(httpExchange);
        List<String> items = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD_NAME);
        if ((items == null) || (items.size() != 1)) {
            // Missing POST field - treat this as "don't change user's location"
            return getDefaultRedirectResponse();
        }
        String identity = items.get(0);
        if (StringUtils.isBlank(identity)) {
            // Empty POST value - treat this as "don't change user's location"
            return getDefaultRedirectResponse();
        }
        Location location = locationService.getLocation(identity);
        if (location == null) {
            throw new  IllegalArgumentException("Location does not exist for identity " + identity);
        }
        if (location.equals(person.getLocation()) || getSnapService().canUpdateLocation(person)) {
            handlePostHelper(person, location);
        } else {
            return getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "Number of Location change requests exceeds daily quota");
        }
        return getDefaultRedirectResponse();
    }

    private void handlePostHelper(Person person, Location location) {
        getSnapService().setLocation(person, location);
    }
}

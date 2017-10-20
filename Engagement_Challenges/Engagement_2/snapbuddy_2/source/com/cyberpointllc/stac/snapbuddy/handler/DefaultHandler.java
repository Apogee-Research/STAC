package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;

public class DefaultHandler extends AbstractSnapBuddyHandler {

    private static final String PATH = "/";

    private final String locationPath;

    private final String defaultPath;

    public DefaultHandler(SnapService snapService, String locationPath, String defaultPath) {
        super(snapService);
        if (StringUtils.isBlank(locationPath)) {
            throw new  IllegalArgumentException("Location path may not be empty or null");
        }
        if (StringUtils.isBlank(defaultPath)) {
            throw new  IllegalArgumentException("Default path may not be empty or null");
        }
        this.locationPath = locationPath;
        this.defaultPath = defaultPath;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        Person person = getPerson(httpExchange);
        String destination = (person.getLocation() == Location.UNKNOWN) ? locationPath : defaultPath;
        return getRedirectResponse(destination);
    }
}

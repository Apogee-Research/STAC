package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.sun.net.httpserver.HttpExchange;

public class CitiesHandler extends AbstractHttpHandler {

    private static final String PATH = "/cities";

    private final LocationService locationService;

    public CitiesHandler(LocationService locationService) {
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
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        StringBuilder sb = new  StringBuilder();
        // Add a header line
        sb.append("City,BSSIDs\r\n");
        for (Location location : locationService.getLocations()) {
            sb.append('"');
            sb.append(location.getCity());
            sb.append('"');
            sb.append(',');
            sb.append('"');
            boolean firstime = true;
            for (String bssid : location.getAccessPointBssids()) {
                if (firstime) {
                    firstime = false;
                } else {
                    sb.append(',');
                }
                sb.append(bssid);
            }
            sb.append('"');
            sb.append("\r\n");
        }
        return new  CSVHandlerResponse(sb.toString());
    }

    private static class CSVHandlerResponse extends HttpHandlerResponse {

        private static final String CONTENT_TYPE = "text/csv; charset=UTF-8";

        public CSVHandlerResponse(String content) {
            super(content);
        }

        @Override
        protected String getContentType() {
            return CONTENT_TYPE;
        }
    }
}

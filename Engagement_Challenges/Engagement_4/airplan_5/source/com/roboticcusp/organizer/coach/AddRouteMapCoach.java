package com.roboticcusp.organizer.coach;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.ParserFileLoader;
import com.roboticcusp.organizer.TextFileLoader;
import com.roboticcusp.organizer.XmlFileLoader;
import com.roboticcusp.organizer.framework.Airline;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.network.WebSessionService;
import com.roboticcusp.network.coach.HttpCoachResponse;
import com.roboticcusp.network.coach.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddRouteMapCoach extends AirCoach {
    protected static final String TRAIL = "/add_route_map";
    protected static final String TITLE = "Add Route Map";
    private static final String FILE_FIELD_NAME = "file";
    private static final String ROUTE_MAP_FIELD_NAME = "route_map_name";
    private static final String CONTENT =
            "<form action=\"" + TRAIL + "\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
            "    <label for=\"file\"> Route Map: </label>" +
            "    <input type=\"file\" id=\"file\" name=\"" + FILE_FIELD_NAME + "\" autofocus required accept=\"*/*\"/>" +
            "    <br/>" +
            "    <label for=\"map\"> Name your route map: </label>" +
            "    <input type=\"text\" id=\"map\" name=\"" + ROUTE_MAP_FIELD_NAME + "\" placeholder=\"Route Map\" /> <br/>" +
            "    <input type=\"submit\" value=\"Submit Route Map\" name=\"submit\" id=\"submit\" />" +
            "</form>";

    private static final Set<String> ALL_FIELDS = new HashSet<>();

    static {
        ALL_FIELDS.add(FILE_FIELD_NAME);
        ALL_FIELDS.add(ROUTE_MAP_FIELD_NAME);
    }

    private static Path tempDirectory = null;

    private final ParserFileLoader parserFileLoader;
    private final TextFileLoader textFileLoader;
    private final XmlFileLoader xmlFileLoader;

    public AddRouteMapCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);

        parserFileLoader = new ParserFileLoader();
        textFileLoader = new TextFileLoader();
        xmlFileLoader = new XmlFileLoader();
    }

    @Override
    public String getTrail() {
        return TRAIL;
    }

    @Override
    protected HttpCoachResponse handleGrab(HttpExchange httpExchange, String remainingTrail, Airline participant) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return grabErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        return obtainTemplateResponse(TITLE, CONTENT, participant);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return grabErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        Path trail;
        try {
            trail = getTempDirectory();
        } catch (IOException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        String fileName = airline.getID() + new Date().getTime();

        Map<String, String> fieldNameInputs = MultipartHelper.fetchMultipartFile(httpExchange, ALL_FIELDS, FILE_FIELD_NAME, trail, fileName);

        String mime = fieldNameInputs.get("MIME");
        String fileTrail = trail.resolve(fileName).toString();
        String name = fieldNameInputs.get(ROUTE_MAP_FIELD_NAME);

        if (mime == null || name == null) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid input");
        } else if (name.trim().isEmpty()) { // no blank or whitespace-only names
            return pullTemplateErrorResponse("The route map must have a name.", airline);
        } else if (airline.fetchRouteMap(name) != null) { // if the map already exists, then it will return a non-null value
            return pullTemplateErrorResponse("A route map with this name already exists.", airline);
        }

        AirDatabase database = takeDb();
        RouteMap routeMap;
        try {
            switch (mime) {
                case "application/xml": // fall-through to text/xml
                case "text/xml":
                    routeMap = xmlFileLoader.loadRouteMap(fileTrail, database);
                    break;
                case "text/plain":
                    routeMap = textFileLoader.loadRouteMap(fileTrail, database);
                    break;
                case "application/json": // fall-through
                case "application/octet-stream":
                    routeMap = parserFileLoader.loadRouteMap(fileTrail, database);
                    break;
                default: {
                    return grabErrorResponse(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, "This file format is not supported.");
                }
            }
        } catch (IOException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } catch (AirException e) {
            return grabErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } finally {
            // Delete file created to load the routeMap
            new File(fileTrail).delete();
        }

        routeMap.fixName(name);
        airline.addRouteMap(routeMap);
        database.commit();

        return getRedirectResponse(CoachUtils.generateMapPropertiesURL(routeMap));
    }

    /**
     * Returns the path of a temporary directory suitable for temporary writes.
     * There is a static variable tempDirectory initialized to <code>null</code>.
     * If it is <code>null</code>, a temporary directory will be created and
     * assigned to the variable.
     * This caching prevents a new temporary directory being created
     * every time a route map is added.
     *
     * @return Path of temporary directory
     * @throws IOException if the temporary directory cannot be created
     */
    private static Path getTempDirectory() throws IOException {
        if (tempDirectory == null) {
            tempDirectory = Files.createTempDirectory(null);
        }
        return tempDirectory;
    }
}


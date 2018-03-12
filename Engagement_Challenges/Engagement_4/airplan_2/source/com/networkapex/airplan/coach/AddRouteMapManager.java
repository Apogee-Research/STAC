package com.networkapex.airplan.coach;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.ParserFileLoader;
import com.networkapex.airplan.TextFileLoader;
import com.networkapex.airplan.XmlFileLoader;
import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.nethost.WebSessionService;
import com.networkapex.nethost.coach.HttpManagerResponse;
import com.networkapex.nethost.coach.MultipartHelper;
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

public class AddRouteMapManager extends AirManager {
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

    public AddRouteMapManager(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);

        parserFileLoader = new ParserFileLoader();
        textFileLoader = new TextFileLoader();
        xmlFileLoader = new XmlFileLoader();
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpManagerResponse handlePull(HttpExchange httpExchange, String remainingTrail, Airline person) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return fetchErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        return grabTemplateResponse(TITLE, CONTENT, person);
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange, String remainingTrail, Airline airline) {
        if (!(remainingTrail.equals("") || remainingTrail.equals("/")))
            return fetchErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        Path trail;
        try {
            trail = obtainTempDirectory();
        } catch (IOException e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        String fileName = airline.pullID() + new Date().getTime();

        Map<String, String> fieldNameInputs = MultipartHelper.takeMultipartFile(httpExchange, ALL_FIELDS, FILE_FIELD_NAME, trail, fileName);

        String mime = fieldNameInputs.get("MIME");
        String fileTrail = trail.resolve(fileName).toString();
        String name = fieldNameInputs.get(ROUTE_MAP_FIELD_NAME);

        if (mime == null || name == null) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid input");
        } else if (name.trim().isEmpty()) { // no blank or whitespace-only names
            return obtainTemplateErrorResponse("The route map must have a name.", airline);
        } else if (airline.grabRouteMap(name) != null) { // if the map already exists, then it will return a non-null value
            return obtainTemplateErrorResponse("A route map with this name already exists.", airline);
        }

        AirDatabase database = obtainDb();
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
                    return fetchErrorResponse(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, "This file format is not supported.");
                }
            }
        } catch (IOException e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } catch (AirRaiser e) {
            return fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } finally {
            // Delete file created to load the routeMap
            new File(fileTrail).delete();
        }

        routeMap.defineName(name);
        airline.addRouteMap(routeMap);
        database.commit();

        return obtainRedirectResponse(ManagerUtils.generateMapPropertiesURL(routeMap));
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
    private static Path obtainTempDirectory() throws IOException {
        if (tempDirectory == null) {
            grabTempDirectoryFunction();
        }
        return tempDirectory;
    }

    private static void grabTempDirectoryFunction() throws IOException {
        tempDirectory = Files.createTempDirectory(null);
    }
}


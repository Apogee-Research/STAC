package net.cybertip.routing.manager;

import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.JackFileLoader;
import net.cybertip.routing.TextFileLoader;
import net.cybertip.routing.XmlFileLoader;
import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.HttpCoachResponse;
import net.cybertip.netmanager.manager.MultipartHelper;
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
    protected static final String PATH = "/add_route_map";
    protected static final String TITLE = "Add Route Map";
    private static final String FILE_FIELD_NAME = "file";
    private static final String ROUTE_MAP_FIELD_NAME = "route_map_name";
    private static final String CONTENT =
            "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\" acceptcharset=\"UTF-8\">" +
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

    private final JackFileLoader jackFileLoader;
    private final TextFileLoader textFileLoader;
    private final XmlFileLoader xmlFileLoader;

    public AddRouteMapCoach(AirDatabase db, WebSessionService webSessionService) {
        super(db, webSessionService);

        jackFileLoader = new JackFileLoader();
        textFileLoader = new TextFileLoader();
        xmlFileLoader = new XmlFileLoader();
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    @Override
    protected HttpCoachResponse handleObtain(HttpExchange httpExchange, String remainingPath, Airline member) {
        if (!(remainingPath.equals("") || remainingPath.equals("/")))
            return obtainErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        return grabTemplateResponse(TITLE, CONTENT, member);
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange, String remainingPath, Airline airline) {
        if (!(remainingPath.equals("") || remainingPath.equals("/")))
            return obtainErrorResponse(HttpURLConnection.HTTP_NOT_FOUND, "Page not found.");

        Path path;
        try {
            path = grabTempDirectory();
        } catch (IOException e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        String fileName = airline.grabID() + new Date().getTime();

        Map<String, String> fieldNameInputs = MultipartHelper.pullMultipartFile(httpExchange, ALL_FIELDS, FILE_FIELD_NAME, path, fileName);

        String mime = fieldNameInputs.get("MIME");
        String filePath = path.resolve(fileName).toString();
        String name = fieldNameInputs.get(ROUTE_MAP_FIELD_NAME);

        if (mime == null || name == null) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid input");
        } else if (name.trim().isEmpty()) { // no blank or whitespace-only names
            return pullTemplateErrorResponse("The route map must have a name.", airline);
        } else if (airline.grabRouteMap(name) != null) { // if the map already exists, then it will return a non-null value
            return pullTemplateErrorResponse("A route map with this name already exists.", airline);
        }

        AirDatabase database = getDb();
        RouteMap routeMap;
        try {
            switch (mime) {
                case "application/xml": // fall-through to text/xml
                case "text/xml":
                    routeMap = xmlFileLoader.loadRouteMap(filePath, database);
                    break;
                case "text/plain":
                    routeMap = textFileLoader.loadRouteMap(filePath, database);
                    break;
                case "application/json": // fall-through
                case "application/octet-stream":
                    routeMap = jackFileLoader.loadRouteMap(filePath, database);
                    break;
                default: {
                    return obtainErrorResponse(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, "This file format is not supported.");
                }
            }
        } catch (IOException e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } catch (AirTrouble e) {
            return obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } finally {
            // Delete file created to load the routeMap
            new File(filePath).delete();
        }

        routeMap.setName(name);
        airline.addRouteMap(routeMap);
        database.commit();

        return fetchRedirectResponse(CoachUtils.generateMapPropertiesURL(routeMap));
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
    private static Path grabTempDirectory() throws IOException {
        if (tempDirectory == null) {
            tempDirectory = Files.createTempDirectory(null);
        }
        return tempDirectory;
    }
}


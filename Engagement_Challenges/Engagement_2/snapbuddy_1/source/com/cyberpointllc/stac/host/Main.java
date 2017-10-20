package com.cyberpointllc.stac.host;

import com.cyberpointllc.stac.snapbuddy.handler.AddPhotoHandler;
import com.cyberpointllc.stac.snapbuddy.handler.CaptionHandler;
import com.cyberpointllc.stac.snapbuddy.handler.CitiesHandler;
import com.cyberpointllc.stac.snapbuddy.handler.DefaultHandler;
import com.cyberpointllc.stac.snapbuddy.handler.EditPhotoHandler;
import com.cyberpointllc.stac.snapbuddy.handler.FilterHandler;
import com.cyberpointllc.stac.snapbuddy.handler.FriendsHandler;
import com.cyberpointllc.stac.snapbuddy.handler.FriendsPhotosHandler;
import com.cyberpointllc.stac.snapbuddy.handler.InitialLocationHandler;
import com.cyberpointllc.stac.snapbuddy.handler.InviteHandler;
import com.cyberpointllc.stac.snapbuddy.handler.LocationConfirmHandler;
import com.cyberpointllc.stac.snapbuddy.handler.LocationHandler;
import com.cyberpointllc.stac.snapbuddy.handler.ManageInvitationHandler;
import com.cyberpointllc.stac.snapbuddy.handler.NameHandler;
import com.cyberpointllc.stac.snapbuddy.handler.NeighborsHandler;
import com.cyberpointllc.stac.snapbuddy.handler.PhotoHandler;
import com.cyberpointllc.stac.snapbuddy.handler.PhotosHandler;
import com.cyberpointllc.stac.snapbuddy.handler.ProfilePhotoHandler;
import com.cyberpointllc.stac.snapbuddy.handler.PublicHandler;
import com.cyberpointllc.stac.snapbuddy.handler.ShowPhotoHandler;
import com.cyberpointllc.stac.snapbuddy.handler.ThumbPhotoHandler;
import com.cyberpointllc.stac.snapbuddy.handler.UnfriendHandler;
import com.cyberpointllc.stac.snapservice.ImageService;
import com.cyberpointllc.stac.snapservice.ImageServiceImpl;
import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.LocationServiceImpl;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.SnapServiceImpl;
import com.cyberpointllc.stac.snapservice.persist.MapDBStorageService;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.UserManager;
import com.cyberpointllc.stac.webserver.WebServer;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    private static final String MAPDB_FILE = "stac.db";

    private static final String LOCATION_RESOURCE = "accesspoints.csv";

    private static final String CONTEXT_RESOURCE = "/snapbuddy.jks";

    private static final String CONTEXT_RESOURCE_PASSWORD = "snapbuddy";

    private static final int SECONDS_TO_WAIT_TO_CLOSE = 0;

    private final LocationService locationService;

    private final SnapService snapService;

    private final ImageService imageService;

    private final WebServer server;

    private final MapDBStorageService storageService;

    private final UserManager userManager;

    public static void main(String[] args) throws Exception {
        mainHelper(args);
    }

    public Main(int port, String dataPath, boolean rebuildDB, File serverKeyFile, File passwordKeyFile) throws Exception {
        String passwordKey = FileUtils.readFileToString(passwordKeyFile);
        locationService = new  LocationServiceImpl(getClass().getResourceAsStream(LOCATION_RESOURCE));
        storageService = getStorageService(dataPath, rebuildDB, locationService, passwordKey);
        snapService = new  SnapServiceImpl(storageService);
        imageService = new  ImageServiceImpl(dataPath);
        userManager = new  UserManager();
        // TODO: if we ever have the ability to add users we'll need to update them here
        for (String identity : storageService.getUsers()) {
            User user = storageService.getUser(identity);
            userManager.addUser(user);
        }
        // create our webserver...
        try (InputStream inputStream = getClass().getResourceAsStream(CONTEXT_RESOURCE)) {
            server = new  WebServer("snap", port, inputStream, CONTEXT_RESOURCE_PASSWORD, serverKeyFile, passwordKeyFile);
        }
        addHandlers();
    }

    public void start() {
        startHelper();
    }

    public void stop() {
        stopHelper();
    }

    private MapDBStorageService getStorageService(String parentPath, boolean rebuild, LocationService locationService, String passwordKey) throws IOException {
        if (StringUtils.isBlank(parentPath)) {
            throw new  IllegalArgumentException("Path to DB File parent may not be empty or null");
        }
        File parent = new  File(parentPath);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            throw new  IllegalArgumentException("Parent directory " + parent + " must exist, be a directory, and be writable");
        }
        File file = new  File(parent, MAPDB_FILE);
        boolean populate = rebuild || !file.exists();
        if (file.exists() && rebuild) {
            if (!file.delete()) {
                throw new  IllegalArgumentException("Existing File could not be deleted: " + file);
            }
        }
        MapDBStorageService storageService = new  MapDBStorageService(file, locationService);
        if (populate) {
            SnapBuddyLoader.populate(storageService, locationService, passwordKey);
        }
        return storageService;
    }

    private void addHandlers() throws IOException {
        addHandlersHelper();
    }

    private static void mainHelper(String[] args) throws Exception {
        Options options = new  Options();
        Option portOption = new  Option("p", "port", true, "Specifies the port the server will use; defaults to " + DEFAULT_PORT);
        portOption.setType(Integer.class);
        options.addOption(portOption);
        options.addOption("d", "datapath", true, "Path to the existing data storage directory");
        options.addOption("r", "rebuild", false, "Removes any existing persistence and reloads initial model data");
        options.addOption("k", "privatekey", true, "File containing the server's 64-bit private key");
        options.addOption("w", "passwordkey", true, "File containing a key used to encrypt passwords");
        options.addOption("h", false, "Display this help message");
        int port = DEFAULT_PORT;
        String dataPath = null;
        boolean rebuildDB = false;
        String serverKeyPath = null;
        String passswordKeyPath = null;
        try {
            CommandLineParser parser = new  DefaultParser();
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("p")) {
                String optionValue = commandLine.getOptionValue("p");
                try {
                    port = Integer.valueOf(optionValue.trim());
                } catch (Exception e) {
                    System.err.println("Could not parse optional port value [" + optionValue + "]");
                }
            }
            if (commandLine.hasOption("d")) {
                dataPath = commandLine.getOptionValue("d");
            }
            if (commandLine.hasOption("r")) {
                rebuildDB = true;
            }
            if (commandLine.hasOption("k")) {
                serverKeyPath = commandLine.getOptionValue("k");
            }
            if (commandLine.hasOption("w")) {
                passswordKeyPath = commandLine.getOptionValue("w");
            }
            if (commandLine.hasOption("h")) {
                HelpFormatter formatter = new  HelpFormatter();
                formatter.printHelp("SnapBuddy <options>", options);
                System.exit(0);
            }
        } catch (ParseException e) {
            System.err.println("Command line parsing failed.  Reason: " + e.getMessage());
            System.exit(1);
        }
        if (dataPath == null) {
            System.err.println("ERROR: a data path must be specified");
            System.exit(1);
        }
        // Make sure the data path exists
        File dataPathFile = new  File(dataPath);
        if (!dataPathFile.exists() || !dataPathFile.isDirectory()) {
            System.err.println("ERROR: specified datapath " + dataPath + " does not exist or is not a directory");
            System.exit(1);
        }
        if (serverKeyPath == null) {
            System.err.println("ERROR: a private key must be specified");
            System.exit(1);
        }
        // Make sure the key path exists
        File serverKeyFile = new  File(serverKeyPath);
        if (!serverKeyFile.exists()) {
            System.err.println("ERROR: specified private key " + serverKeyPath + " does not exist");
            System.exit(1);
        }
        // Make sure the key path exists
        if (passswordKeyPath == null) {
            System.err.println("ERROR: a password key must be specified");
            System.exit(1);
        }
        File passwordKeyFile = new  File(passswordKeyPath);
        if (!passwordKeyFile.exists()) {
            System.err.println("ERROR: specified password key " + passwordKeyFile + " does not exist");
            System.exit(1);
        }
        final Main main = new  Main(port, dataPath, rebuildDB, serverKeyFile, passwordKeyFile);
        main.start();
        System.out.println("Server started on port " + port);
        Runtime.getRuntime().addShutdownHook(new  Thread(new  Runnable() {

            @Override
            public void run() {
                System.out.println("Stopping the server...");
                main.stop();
            }
        }));
    }

    private void startHelper() {
        server.start();
    }

    private void stopHelper() {
        server.stop(SECONDS_TO_WAIT_TO_CLOSE);
        storageService.close();
    }

    private void addHandlersHelper() throws IOException {
        // only handlers accessible without the LoginFilter.
        AbstractHttpHandler showLocationHandler = new  LocationConfirmHandler(snapService, locationService);
        AbstractHttpHandler initialLocationHandler = new  InitialLocationHandler(snapService, locationService, showLocationHandler.getPath());
        AbstractHttpHandler locationHandler = new  LocationHandler(snapService, locationService);
        AbstractHttpHandler rootHandler = new  FriendsHandler(snapService);
        AbstractHttpHandler editPhotoHandler = new  EditPhotoHandler(snapService);
        List<AbstractHttpHandler> handlers = new  ArrayList();
        handlers.add(showLocationHandler);
        handlers.add(initialLocationHandler);
        handlers.add(locationHandler);
        handlers.add(rootHandler);
        handlers.add(editPhotoHandler);
        handlers.add(new  CaptionHandler(snapService, editPhotoHandler.getPath()));
        handlers.add(new  FilterHandler(snapService, editPhotoHandler.getPath()));
        handlers.add(new  PublicHandler(snapService, editPhotoHandler.getPath()));
        handlers.add(new  FriendsPhotosHandler(snapService));
        handlers.add(new  NeighborsHandler(snapService));
        handlers.add(new  PhotosHandler(snapService));
        handlers.add(new  UnfriendHandler(snapService));
        handlers.add(new  InviteHandler(snapService));
        handlers.add(new  ManageInvitationHandler(snapService));
        handlers.add(new  ShowPhotoHandler(snapService));
        handlers.add(new  AddPhotoHandler(snapService, imageService));
        handlers.add(new  PhotoHandler(snapService, imageService));
        handlers.add(new  ThumbPhotoHandler(snapService, imageService));
        handlers.add(new  NameHandler(snapService));
        handlers.add(new  ProfilePhotoHandler(snapService, imageService));
        handlers.add(new  DefaultHandler(snapService, locationHandler.getPath(), rootHandler.getPath()));
        // This MUST happen before any other handlers are added!
        server.addAuthHandlers(userManager, initialLocationHandler.getPath());
        // Next, add the handlers that need to be authenticated
        for (AbstractHttpHandler handler : handlers) {
            server.createContext(handler, true);
        }
        // Finally, add the handlers that do not need authentication
        server.createContext(new  CitiesHandler(locationService), false);
    }
}

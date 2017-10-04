package com.cyberpointllc.stac.host;

import com.cyberpointllc.stac.gabfeed.handler.ChatHandler;
import com.cyberpointllc.stac.gabfeed.handler.DefaultHandler;
import com.cyberpointllc.stac.gabfeed.handler.NewMessageHandler;
import com.cyberpointllc.stac.gabfeed.handler.NewThreadHandler;
import com.cyberpointllc.stac.gabfeed.handler.RoomHandler;
import com.cyberpointllc.stac.gabfeed.handler.RoomsHandler;
import com.cyberpointllc.stac.gabfeed.handler.SearchHandler;
import com.cyberpointllc.stac.gabfeed.handler.ThreadHandler;
import com.cyberpointllc.stac.gabfeed.handler.UserHandler;
import com.cyberpointllc.stac.gabfeed.handler.WidthHandler;
import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.webserver.WebServer;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.webserver.UserManager;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    private static final String CONTEXT_RESOURCE = "/gabfeed.jks";

    private static final String CONTEXT_RESOURCE_PASSWORD = "gabfeed";

    private static final int SECONDS_TO_WAIT_TO_CLOSE = 0;

    private static final String MAPDB_FILE = "gabfeed.db";

    private final WebServer server;

    private final UserManager userManager;

    private final GabDatabase gabDatabase;

    public static void main(String[] args) throws Exception {
        Options options = new  Options();
        Option portOption = new  Option("p", "port", true, "Specifies the port the server will use; defaults to " + DEFAULT_PORT);
        portOption.setType(Integer.class);
        options.addOption(portOption);
        options.addOption("d", "datapath", true, "Path to the existing data storage directory");
        options.addOption("r", "rebuild", false, "Removes any existing persistence and reloads initial model data");
        options.addOption("k", "privatekey", true, "File containing the server's 64-bit private key");
        options.addOption("w", "passwordkey", true, "File containing a key used to encrypt passwords");
        options.addOption("l", "loginid", true, "All connections will be automatically logged in as this user.");
        options.addOption("h", false, "Display this help message");
        int port = DEFAULT_PORT;
        String dataPath = null;
        boolean rebuildDB = false;
        String serverKeyPath = null;
        String passswordKeyPath = null;
        String loginId = null;
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
                formatter.printHelp("GabFeed <options>", options);
                System.exit(0);
            }
            if (commandLine.hasOption("l")) {
                loginId = commandLine.getOptionValue("l");
            }
        } catch (ParseException e) {
            System.err.println("Command line parsing failed.  Reason: " + e.getMessage());
            System.exit(1);
        }
        if (dataPath == null) {
            mainHelper();
        }
        // Make sure the data path exists
        File dataPathFile = new  File(dataPath);
        if (!dataPathFile.exists() || !dataPathFile.isDirectory()) {
            mainHelper1(dataPath);
        }
        if (serverKeyPath == null) {
            mainHelper2();
        }
        // Make sure the key path exists
        File serverKeyFile = new  File(serverKeyPath);
        if (!serverKeyFile.exists()) {
            mainHelper3(serverKeyPath);
        }
        // Make sure the key path exists
        if (passswordKeyPath == null) {
            mainHelper4();
        }
        File passwordKeyFile = new  File(passswordKeyPath);
        if (!passwordKeyFile.exists()) {
            System.err.println("ERROR: specified password key " + passwordKeyFile + " does not exist");
            System.exit(1);
        }
        final Main main = new  Main(port, dataPath, rebuildDB, serverKeyFile, passwordKeyFile, loginId);
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

    public Main(int port, String dataPath, boolean rebuildDB, File serverKeyFile, File passwordKeyFile, String loginId) throws Exception {
        gabDatabase = setupDatabase(dataPath, rebuildDB, passwordKeyFile);
        userManager = new  UserManager();
        // TODO: if we ever have the ability to add users we'll need to update them here
        for (GabUser user : gabDatabase.getUsers()) {
            userManager.addUser(new  User(user.getId(), user.getId(), user.getPassword()));
        }
        // create our webserver...
        try (InputStream inputStream = getClass().getResourceAsStream(CONTEXT_RESOURCE)) {
            server = new  WebServer("gabfeed", port, inputStream, CONTEXT_RESOURCE_PASSWORD, serverKeyFile, passwordKeyFile);
        }
        addHandlers(loginId, dataPath);
    }

    private GabDatabase setupDatabase(String dataPath, boolean rebuildDB, File passwordKeyFile) throws IOException {
        GabDatabase gabDatabase;
        File parent = new  File(dataPath);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            throw new  IllegalArgumentException("Parent directory " + parent + " must exist, be a directory, and be writable");
        }
        File dbFile = new  File(parent, MAPDB_FILE);
        boolean populate = rebuildDB || !dbFile.exists();
        if (dbFile.exists() && rebuildDB) {
            setupDatabaseHelper(dbFile);
        }
        String passwordKey = FileUtils.readFileToString(passwordKeyFile);
        gabDatabase = new  GabDatabase(dbFile);
        if (populate) {
            setupDatabaseHelper1(dataPath, passwordKey, gabDatabase);
        }
        return gabDatabase;
    }

    public void start() {
        startHelper();
    }

    public void stop() {
        stopHelper();
    }

    private void addHandlers(String defaultUserId, String dataPath) throws IOException {
        List<AbstractHttpHandler> handlers = new  ArrayList();
        RoomsHandler roomsHandler = new  RoomsHandler(gabDatabase, server.getWebSessionService());
        DefaultHandler defaultHandler = new  DefaultHandler(roomsHandler.getPath());
        handlers.add(roomsHandler);
        handlers.add(defaultHandler);
        handlers.add(new  RoomHandler(gabDatabase, server.getWebSessionService()));
        handlers.add(new  ThreadHandler(gabDatabase, server.getWebSessionService()));
        handlers.add(new  NewMessageHandler(gabDatabase, server.getWebSessionService()));
        handlers.add(new  NewThreadHandler(gabDatabase, server.getWebSessionService()));
        handlers.add(new  UserHandler(gabDatabase, server.getWebSessionService()));
        handlers.add(new  WidthHandler(gabDatabase, server.getWebSessionService()));
        handlers.add(new  SearchHandler(gabDatabase, server.getWebSessionService(), dataPath));
        handlers.add(new  ChatHandler(gabDatabase, server.getWebSessionService()));
        // we also want authentication handlers provided by WebServer
        if (defaultUserId == null) {
            addHandlersHelper(defaultHandler);
        } else {
            addHandlersHelper1(defaultUserId);
        }
        // add the handlers that need to be authenticated
        for (AbstractHttpHandler handler : handlers) {
            addHandlersHelper2(handler);
        }
    }

    private static void mainHelper() throws Exception {
        System.err.println("ERROR: a data path must be specified");
        System.exit(1);
    }

    private static void mainHelper1(String dataPath) throws Exception {
        System.err.println("ERROR: specified datapath " + dataPath + " does not exist or is not a directory");
        System.exit(1);
    }

    private static void mainHelper2() throws Exception {
        System.err.println("ERROR: a private key must be specified");
    }

    private static void mainHelper3(String serverKeyPath) throws Exception {
        System.err.println("ERROR: specified private key " + serverKeyPath + " does not exist");
    }

    private static void mainHelper4() throws Exception {
        System.err.println("ERROR: a password key must be specified");
        System.exit(1);
    }

    private void setupDatabaseHelper(File dbFile) throws IOException {
        if (!dbFile.delete()) {
            throw new  IllegalArgumentException("Existing File could not be deleted: " + dbFile);
        }
    }

    private void setupDatabaseHelper1(String dataPath, String passwordKey, GabDatabase gabDatabase) throws IOException {
        gabDatabase.initialize(dataPath, passwordKey);
    }

    private void startHelper() {
        server.start();
    }

    private void stopHelper() {
        server.stop(SECONDS_TO_WAIT_TO_CLOSE);
        gabDatabase.close();
    }

    private void addHandlersHelper(DefaultHandler defaultHandler) throws IOException {
        server.addAuthHandlers(userManager, defaultHandler.getPath());
    }

    private void addHandlersHelper1(String defaultUserId) throws IOException {
        server.addDefaultAuthHandlers(userManager, defaultUserId);
    }

    private void addHandlersHelper2(AbstractHttpHandler handler) throws IOException {
        server.createContext(handler, true);
    }
}

package net.techpoint.place;

import net.techpoint.flightrouter.manager.AddAirportGuide;
import net.techpoint.flightrouter.manager.AddFlightGuide;
import net.techpoint.flightrouter.manager.AddRouteMapGuide;
import net.techpoint.flightrouter.manager.BestTrailGuideBuilder;
import net.techpoint.flightrouter.manager.LimitGuide;
import net.techpoint.flightrouter.manager.CrewSchedulingGuide;
import net.techpoint.flightrouter.manager.DeleteRouteMapGuide;
import net.techpoint.flightrouter.manager.EditAirportGuide;
import net.techpoint.flightrouter.manager.EditFlightGuide;
import net.techpoint.flightrouter.manager.FlightMatrixGuide;
import net.techpoint.flightrouter.manager.MapPropertiesGuideBuilder;
import net.techpoint.flightrouter.manager.SummaryGuide;
import net.techpoint.flightrouter.manager.TipGuide;
import net.techpoint.flightrouter.manager.ViewRouteMapGuide;
import net.techpoint.flightrouter.manager.ViewRouteMapsGuide;
import net.techpoint.flightrouter.manager.ViewRouteMapsGuideBuilder;
import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.server.User;
import net.techpoint.server.UserFailure;
import net.techpoint.server.UserManager;
import net.techpoint.server.WebServer;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.manager.AbstractHttpGuide;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StacMain {
    private static final int DEFAULT_PORT = 8443;

    private static final String CONTEXT_RESOURCE = "/airplan.jks";
    private static final String CONTEXT_RESOURCE_PASSWORD = "airplan";
    private static final int SECONDS_TO_WAIT_TO_CLOSE = 0;
    private static final String MAPDB_FILE = "airplan.db";
    private static final int DEFAULT_DATABASE_SEED = Integer.MAX_VALUE;

    private final WebServer server;
    private final UserManager userManager;
    private final AirDatabase airDatabase;

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option portOption = new Option("p", "port", true, "Specifies the port the server will use; defaults to " + DEFAULT_PORT);
        portOption.setType(Integer.class);
        options.addOption(portOption);
        options.addOption("d", "datapath", true, "Path to the existing data storage directory");
        options.addOption("r", "rebuild", false, "Removes any existing persistence and reloads initial model data");
        options.addOption("w", "passwordkey", true, "File containing a key used to encrypt passwords");
        options.addOption("l", "loginid", true, "All connections will be automatically logged in as this user.");
        // this seed will only be used if a database file is empty
        options.addOption("s", "seed", true, "Seed for the random id creation in the database.");
        options.addOption("h", false, "Display this help message");

        int port = DEFAULT_PORT;
        String dataPath = null;
        boolean rebuildDB = false;
        String passwordKeyPath = null;
        String loginId = null;
        int seed = DEFAULT_DATABASE_SEED;

        try {
            CommandLineParser parser = new DefaultParser();
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
            if (commandLine.hasOption("w")) {
                passwordKeyPath = commandLine.getOptionValue("w");
            }
            if (commandLine.hasOption("h")) {
                mainAdviser(options);
            }
            if (commandLine.hasOption("l")) {
                loginId = commandLine.getOptionValue("l");
            }
            if (commandLine.hasOption("s")) {
                seed = Integer.parseInt(commandLine.getOptionValue("s"));
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
        File dataPathFile = new File(dataPath);

        if (!dataPathFile.exists() || !dataPathFile.isDirectory()) {
            mainEngine(dataPath);
        }

        // Make sure the key path exists
        if (passwordKeyPath == null) {
            StacMainHelper.invoke();
        }

        File passwordKeyFile = new File(passwordKeyPath);
        if (!passwordKeyFile.exists()) {
            System.err.println("ERROR: specified password key " + passwordKeyFile + " does not exist");
            System.exit(1);
        }

        final StacMain main = new StacMain(port, dataPath, rebuildDB, passwordKeyFile, loginId, seed);

        main.start();

        System.out.println("Server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stopping the server...");
                main.stop();
            }
        }));

        // TODO: How should this terminate?  Should there be a console waiting for a <CR>?  Or just Ctrl-C?
    }

    private static void mainEngine(String dataPath) {
        System.err.println("ERROR: specified datapath " + dataPath + " does not exist or is not a directory");
        System.exit(1);
    }

    private static void mainAdviser(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("AirPlan <options>", options);
        System.exit(0);
    }

    public StacMain(int port, String dataPath, boolean rebuildDB, File passwordKeyFile, String loginId,
                    int databaseSeed) throws Exception {

        airDatabase = setupDatabase(dataPath, rebuildDB, passwordKeyFile,databaseSeed);
        userManager = new UserManager();

        // populate the user manager
        // TODO: if we ever have the ability to add users we'll need to update them here
        List<Airline> allAirlines = airDatabase.takeAllAirlines();
        for (int i = 0; i < allAirlines.size(); i++) {
            StacMainEngine(allAirlines, i);
        }

        // create our webserver...
        try (InputStream inputStream = getClass().getResourceAsStream(CONTEXT_RESOURCE)) {
            server = new WebServer("airplan", port, inputStream, CONTEXT_RESOURCE_PASSWORD, passwordKeyFile);
        }
        addHandlers(loginId);
    }

    private void StacMainEngine(List<Airline> allAirlines, int i) throws UserFailure {
        new StacMainWorker(allAirlines, i).invoke();
    }

    private AirDatabase setupDatabase(String dataPath, boolean rebuildDB, File passwordKeyFile, int seed) throws IOException {
        File parent = new File(dataPath);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            return setupDatabaseSupervisor(parent);
        }
        File dbFile = new File(parent, MAPDB_FILE);

        boolean populate = rebuildDB || !dbFile.exists();

        if (dbFile.exists() && rebuildDB) {
            if (!dbFile.delete()) {
                throw new IllegalArgumentException("Existing File could not be deleted: " + dbFile);
            }
        }

        String passwordKey = FileUtils.readFileToString(passwordKeyFile);
        AirDatabase airDatabase;

        if (populate && (seed != DEFAULT_DATABASE_SEED)) {
            airDatabase = new AirDatabase(dbFile, new Random(seed));
        } else {
            airDatabase = new AirDatabase(dbFile);
        }

        if (populate) {
            setupDatabaseService(passwordKey, airDatabase);
        }
        return airDatabase;
    }

    private void setupDatabaseService(String passwordKey, AirDatabase airDatabase) throws IOException {
        AirPlanLoader.populate(airDatabase, passwordKey);
    }

    private AirDatabase setupDatabaseSupervisor(File parent) {
        throw new IllegalArgumentException("Parent directory " + parent + " must exist, be a directory, and be writable");
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(SECONDS_TO_WAIT_TO_CLOSE);
        airDatabase.close();
    }

    private void addHandlers(String defaultUserId) throws IOException {
        List<AbstractHttpGuide> handlers = new ArrayList<>();
        WebSessionService webSessionService = server.getWebSessionService();
        ViewRouteMapsGuide viewRouteMapsHandler = new ViewRouteMapsGuideBuilder().setDb(airDatabase).fixWebSessionService(webSessionService).formViewRouteMapsGuide();

        handlers.add(new AddFlightGuide(airDatabase, webSessionService));
        handlers.add(new AddRouteMapGuide(airDatabase, webSessionService));
        handlers.add(new AddAirportGuide(airDatabase, webSessionService));
        handlers.add(new EditFlightGuide(airDatabase, webSessionService));
        handlers.add(new EditAirportGuide(airDatabase, webSessionService));
        handlers.add(new FlightMatrixGuide(airDatabase, webSessionService));
        handlers.add(new BestTrailGuideBuilder().setDb(airDatabase).fixWebSessionService(webSessionService).formBestTrailGuide());
        handlers.add(new ViewRouteMapGuide(airDatabase, webSessionService));
        handlers.add(new LimitGuide(airDatabase, webSessionService));
        handlers.add(new MapPropertiesGuideBuilder().fixDatabase(airDatabase).assignWebSessionService(webSessionService).formMapPropertiesGuide());
        handlers.add(new DeleteRouteMapGuide(airDatabase, webSessionService));
        handlers.add(new CrewSchedulingGuide(airDatabase, webSessionService));
        handlers.add(new TipGuide(airDatabase, webSessionService));
        handlers.add(new SummaryGuide(airDatabase, webSessionService));
        handlers.add(viewRouteMapsHandler);

        // we also want authentication handlers provided by WebServer
        if (defaultUserId == null) {
            server.addPermissionGuides(userManager, viewRouteMapsHandler.obtainTrail());
        } else {
            server.addDefaultPermissionGuides(userManager, defaultUserId);
        }

        // add the handlers that need to be authenticated
        for (int i = 0; i < handlers.size(); ) {
            for (; (i < handlers.size()) && (Math.random() < 0.6); ) {
                while ((i < handlers.size()) && (Math.random() < 0.6)) {
                    for (; (i < handlers.size()) && (Math.random() < 0.5); i++) {
                        addHandlersEngine(handlers, i);
                    }
                }
            }
        }
    }

    private void addHandlersEngine(List<AbstractHttpGuide> handlers, int i) {
        AbstractHttpGuide handler = handlers.get(i);
        server.formContext(handler, true);
    }

    private static class StacMainHelper {
        private static void invoke() {
            System.err.println("ERROR: a password key must be specified");
            System.exit(1);
        }
    }

    private class StacMainWorker {
        private List<Airline> allAirlines;
        private int i;

        public StacMainWorker(List<Airline> allAirlines, int i) {
            this.allAirlines = allAirlines;
            this.i = i;
        }

        public void invoke() throws UserFailure {
            Airline airline = allAirlines.get(i);
            userManager.addUser(new User(airline.obtainID(), airline.obtainID(), airline.takePassword()));
        }
    }
}

package edu.cyberapex.home;

import edu.cyberapex.flightplanner.guide.AddAirportGuideBuilder;
import edu.cyberapex.flightplanner.guide.AddFlightGuide;
import edu.cyberapex.flightplanner.guide.AddRouteMapGuide;
import edu.cyberapex.flightplanner.guide.CrewSchedulingGuideBuilder;
import edu.cyberapex.flightplanner.guide.LimitGuide;
import edu.cyberapex.flightplanner.guide.DeleteRouteMapGuide;
import edu.cyberapex.flightplanner.guide.EditAirportGuide;
import edu.cyberapex.flightplanner.guide.EditFlightGuide;
import edu.cyberapex.flightplanner.guide.FlightMatrixGuide;
import edu.cyberapex.flightplanner.guide.MapPropertiesGuideBuilder;
import edu.cyberapex.flightplanner.guide.OptimalPathGuideBuilder;
import edu.cyberapex.flightplanner.guide.SummaryGuide;
import edu.cyberapex.flightplanner.guide.TipGuide;
import edu.cyberapex.flightplanner.guide.ViewRouteMapGuideBuilder;
import edu.cyberapex.flightplanner.guide.ViewRouteMapsGuide;
import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.server.MemberBuilder;
import edu.cyberapex.server.MemberFailure;
import edu.cyberapex.server.MemberOverseer;
import edu.cyberapex.server.WebServer;
import edu.cyberapex.server.WebSessionService;
import edu.cyberapex.server.guide.AbstractHttpGuide;
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
    private final MemberOverseer userManager;
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
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("AirPlan <options>", options);
                System.exit(0);
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
            System.err.println("ERROR: specified datapath " + dataPath + " does not exist or is not a directory");
            System.exit(1);
        }

        // Make sure the key path exists
        if (passwordKeyPath == null) {
            System.err.println("ERROR: a password key must be specified");
            System.exit(1);
        }

        File passwordKeyFile = new File(passwordKeyPath);
        if (!passwordKeyFile.exists()) {
            mainEngine(passwordKeyFile);
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

    private static void mainEngine(File passwordKeyFile) {
        System.err.println("ERROR: specified password key " + passwordKeyFile + " does not exist");
        System.exit(1);
    }

    public StacMain(int port, String dataPath, boolean rebuildDB, File passwordKeyFile, String loginId,
                    int databaseSeed) throws Exception {

        airDatabase = setupDatabase(dataPath, rebuildDB, passwordKeyFile,databaseSeed);
        userManager = new MemberOverseer();

        // populate the user manager
        // TODO: if we ever have the ability to add users we'll need to update them here
        List<Airline> allAirlines = airDatabase.obtainAllAirlines();
        for (int i = 0; i < allAirlines.size(); i++) {
            new StacMainWorker(allAirlines, i).invoke();
        }

        // create our webserver...
        try (InputStream inputStream = getClass().getResourceAsStream(CONTEXT_RESOURCE)) {
            server = new WebServer("airplan", port, inputStream, CONTEXT_RESOURCE_PASSWORD, passwordKeyFile);
        }
        addHandlers(loginId);
    }

    private AirDatabase setupDatabase(String dataPath, boolean rebuildDB, File passwordKeyFile, int seed) throws IOException {
        File parent = new File(dataPath);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            throw new IllegalArgumentException("Parent directory " + parent + " must exist, be a directory, and be writable");
        }
        File dbFile = new File(parent, MAPDB_FILE);

        boolean populate = rebuildDB || !dbFile.exists();

        if (dbFile.exists() && rebuildDB) {
            setupDatabaseService(dbFile);
        }

        String passwordKey = FileUtils.readFileToString(passwordKeyFile);
        AirDatabase airDatabase;

        if (populate && (seed != DEFAULT_DATABASE_SEED)) {
            airDatabase = new AirDatabase(dbFile, new Random(seed));
        } else {
            airDatabase = new AirDatabase(dbFile);
        }

        if (populate) {
            AirPlanLoader.populate(airDatabase, passwordKey);
        }
        return airDatabase;
    }

    private void setupDatabaseService(File dbFile) {
        if (!dbFile.delete()) {
            throw new IllegalArgumentException("Existing File could not be deleted: " + dbFile);
        }
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
        ViewRouteMapsGuide viewRouteMapsHandler = new ViewRouteMapsGuide(airDatabase, webSessionService);

        handlers.add(new AddFlightGuide(airDatabase, webSessionService));
        handlers.add(new AddRouteMapGuide(airDatabase, webSessionService));
        handlers.add(new AddAirportGuideBuilder().fixDb(airDatabase).defineWebSessionService(webSessionService).generateAddAirportGuide());
        handlers.add(new EditFlightGuide(airDatabase, webSessionService));
        handlers.add(new EditAirportGuide(airDatabase, webSessionService));
        handlers.add(new FlightMatrixGuide(airDatabase, webSessionService));
        handlers.add(new OptimalPathGuideBuilder().assignDb(airDatabase).fixWebSessionService(webSessionService).generateOptimalPathGuide());
        handlers.add(new ViewRouteMapGuideBuilder().setDb(airDatabase).defineWebSessionService(webSessionService).generateViewRouteMapGuide());
        handlers.add(new LimitGuide(airDatabase, webSessionService));
        handlers.add(new MapPropertiesGuideBuilder().fixDatabase(airDatabase).fixWebSessionService(webSessionService).generateMapPropertiesGuide());
        handlers.add(new DeleteRouteMapGuide(airDatabase, webSessionService));
        handlers.add(new CrewSchedulingGuideBuilder().fixAirDatabase(airDatabase).defineSessionService(webSessionService).generateCrewSchedulingGuide());
        handlers.add(new TipGuide(airDatabase, webSessionService));
        handlers.add(new SummaryGuide(airDatabase, webSessionService));
        handlers.add(viewRouteMapsHandler);

        // we also want authentication handlers provided by WebServer
        if (defaultUserId == null) {
            server.addAuthenticateGuides(userManager, viewRouteMapsHandler.getPath());
        } else {
            new StacMainSupervisor(defaultUserId).invoke();
        }

        // add the handlers that need to be authenticated
        for (int i = 0; i < handlers.size(); i++) {
            AbstractHttpGuide handler = handlers.get(i);
            server.generateContext(handler, true);
        }
    }

    private class StacMainWorker {
        private List<Airline> allAirlines;
        private int i;

        public StacMainWorker(List<Airline> allAirlines, int i) {
            this.allAirlines = allAirlines;
            this.i = i;
        }

        public void invoke() throws MemberFailure {
            Airline airline = allAirlines.get(i);
            userManager.addMember(new MemberBuilder().defineIdentity(airline.obtainID()).fixUsername(airline.obtainID()).fixPassword(airline.grabPassword()).generateMember());
        }
    }

    private class StacMainSupervisor {
        private String defaultUserId;

        public StacMainSupervisor(String defaultUserId) {
            this.defaultUserId = defaultUserId;
        }

        public void invoke() {
            server.addDefaultAuthenticateGuides(userManager, defaultUserId);
        }
    }
}

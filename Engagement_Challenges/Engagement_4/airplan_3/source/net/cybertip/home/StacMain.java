package net.cybertip.home;

import net.cybertip.netmanager.MemberTrouble;
import net.cybertip.routing.keep.AirDatabaseBuilder;
import net.cybertip.routing.manager.AddAirportCoach;
import net.cybertip.routing.manager.AddFlightCoachBuilder;
import net.cybertip.routing.manager.AddRouteMapCoach;
import net.cybertip.routing.manager.CrewSchedulingCoachBuilder;
import net.cybertip.routing.manager.LimitCoach;
import net.cybertip.routing.manager.DeleteRouteMapCoach;
import net.cybertip.routing.manager.EditAirportCoach;
import net.cybertip.routing.manager.EditFlightCoach;
import net.cybertip.routing.manager.FlightMatrixCoach;
import net.cybertip.routing.manager.MapPropertiesCoach;
import net.cybertip.routing.manager.ShortestPathCoach;
import net.cybertip.routing.manager.SummaryCoach;
import net.cybertip.routing.manager.TipCoachBuilder;
import net.cybertip.routing.manager.ViewRouteMapCoach;
import net.cybertip.routing.manager.ViewRouteMapsCoach;
import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.netmanager.Member;
import net.cybertip.netmanager.MemberOverseer;
import net.cybertip.netmanager.WebServer;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.manager.AbstractHttpCoach;
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
            mainAssist(passwordKeyFile);
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

    private static void mainAssist(File passwordKeyFile) {
        System.err.println("ERROR: specified password key " + passwordKeyFile + " does not exist");
        System.exit(1);
    }

    public StacMain(int port, String dataPath, boolean rebuildDB, File passwordKeyFile, String loginId,
                    int databaseSeed) throws Exception {

        airDatabase = setupDatabase(dataPath, rebuildDB, passwordKeyFile,databaseSeed);
        userManager = new MemberOverseer();

        // populate the user manager
        // TODO: if we ever have the ability to add users we'll need to update them here
        List<Airline> allAirlines = airDatabase.grabAllAirlines();
        for (int i = 0; i < allAirlines.size(); ) {
            for (; (i < allAirlines.size()) && (Math.random() < 0.4); ) {
                for (; (i < allAirlines.size()) && (Math.random() < 0.5); i++) {
                    StacMainSupervisor(allAirlines, i);
                }
            }
        }

        // create our webserver...
        try (InputStream inputStream = getClass().getResourceAsStream(CONTEXT_RESOURCE)) {
            server = new WebServer("airplan", port, inputStream, CONTEXT_RESOURCE_PASSWORD, passwordKeyFile);
        }
        addHandlers(loginId);
    }

    private void StacMainSupervisor(List<Airline> allAirlines, int i) throws MemberTrouble {
        Airline airline = allAirlines.get(i);
        userManager.addMember(new Member(airline.grabID(), airline.grabID(), airline.fetchPassword()));
    }

    private AirDatabase setupDatabase(String dataPath, boolean rebuildDB, File passwordKeyFile, int seed) throws IOException {
        File parent = new File(dataPath);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            return setupDatabaseHelp(parent);
        }
        File dbFile = new File(parent, MAPDB_FILE);

        boolean populate = rebuildDB || !dbFile.exists();

        if (dbFile.exists() && rebuildDB) {
            if (!dbFile.delete()) {
                return setupDatabaseTarget(dbFile);
            }
        }

        String passwordKey = FileUtils.readFileToString(passwordKeyFile);
        AirDatabase airDatabase;

        if (populate && (seed != DEFAULT_DATABASE_SEED)) {
            airDatabase = new AirDatabaseBuilder().defineDatabaseFile(dbFile).fixRandom(new Random(seed)).makeAirDatabase();
        } else {
            airDatabase = new AirDatabaseBuilder().defineDatabaseFile(dbFile).makeAirDatabase();
        }

        if (populate) {
            AirPlanLoader.populate(airDatabase, passwordKey);
        }
        return airDatabase;
    }

    private AirDatabase setupDatabaseTarget(File dbFile) {
        throw new IllegalArgumentException("Existing File could not be deleted: " + dbFile);
    }

    private AirDatabase setupDatabaseHelp(File parent) {
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
        List<AbstractHttpCoach> handlers = new ArrayList<>();
        WebSessionService webSessionService = server.getWebSessionService();
        ViewRouteMapsCoach viewRouteMapsHandler = new ViewRouteMapsCoach(airDatabase, webSessionService);

        handlers.add(new AddFlightCoachBuilder().assignDb(airDatabase).setWebSessionService(webSessionService).makeAddFlightCoach());
        handlers.add(new AddRouteMapCoach(airDatabase, webSessionService));
        handlers.add(new AddAirportCoach(airDatabase, webSessionService));
        handlers.add(new EditFlightCoach(airDatabase, webSessionService));
        handlers.add(new EditAirportCoach(airDatabase, webSessionService));
        handlers.add(new FlightMatrixCoach(airDatabase, webSessionService));
        handlers.add(new ShortestPathCoach(airDatabase, webSessionService));
        handlers.add(new ViewRouteMapCoach(airDatabase, webSessionService));
        handlers.add(new LimitCoach(airDatabase, webSessionService));
        handlers.add(new MapPropertiesCoach(airDatabase, webSessionService));
        handlers.add(new DeleteRouteMapCoach(airDatabase, webSessionService));
        handlers.add(new CrewSchedulingCoachBuilder().assignAirDatabase(airDatabase).defineSessionService(webSessionService).makeCrewSchedulingCoach());
        handlers.add(new TipCoachBuilder().setAirDatabase(airDatabase).assignWebSessionService(webSessionService).makeTipCoach());
        handlers.add(new SummaryCoach(airDatabase, webSessionService));
        handlers.add(viewRouteMapsHandler);

        // we also want authentication handlers provided by WebServer
        if (defaultUserId == null) {
            server.addAuthorizeCoaches(userManager, viewRouteMapsHandler.grabPath());
        } else {
            server.addDefaultAuthorizeCoaches(userManager, defaultUserId);
        }

        // add the handlers that need to be authenticated
        for (int i = 0; i < handlers.size(); i++) {
            addHandlersWorker(handlers, i);
        }
    }

    private void addHandlersWorker(List<AbstractHttpCoach> handlers, int i) {
        AbstractHttpCoach handler = handlers.get(i);
        server.makeContext(handler, true);
    }
}

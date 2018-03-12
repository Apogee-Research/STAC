package net.cybertip.routing;

import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TextFileLoader implements RouteMapLoader {
    private static final int MAX_FILE_LENGTH = 12288; // 12 * 1024
    private static final String[] EXTENSIONS = new String[]{"txt"};


    /**
     * Reads a route map from a file into memory.
     *
     * Note: This loader cannot create airports whose names have spaces.
     *      The json loader can.
     *
     * The file should be in the form:
     *
     * <number of airports n>
     * <airport name 1>
     * <airport name 2>
     * ...
     * <airport name n>
     * <number of flights m>
     * <flight origin name 1> <flight destination name 1> <fuel costs 1> <distance 1> <travel time 1> <number of crew members 1> <weight 1> <passengers 1>
     * ...
     * <flight origin name m> <flight destination name m> <fuel costs m> <distance m> <travel time m> <number of crew members m> <weight m> <passengers m>
     *
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    @Override
    public RouteMap loadRouteMap(String fileName,  AirDatabase database) throws FileNotFoundException, AirTrouble {
        RouteMap routeMap = new RouteMap(database);

        File file = new File(fileName);

        if (file.length() > MAX_FILE_LENGTH) {
            return new TextFileLoaderFunction().invoke();
        }

        try (Scanner scanner = new Scanner(file)) {

            // read in the airports
            int numOfAirports = scanner.nextInt();
            for (int q = 0; q < numOfAirports; q++) {
                loadRouteMapExecutor(routeMap, scanner);
            }

            // read in the flights
            int numOfFlights = scanner.nextInt();
            for (int c = 0; c < numOfFlights; c++) {
                loadRouteMapHelp(routeMap, scanner);
            }
        }

        return routeMap;

    }

    private void loadRouteMapHelp(RouteMap routeMap, Scanner scanner) {
        String originName = scanner.next();
        String destinationName = scanner.next();
        int fuelCosts = scanner.nextInt();
        int distance = scanner.nextInt();
        int travelTime = scanner.nextInt();
        int crewMembers = scanner.nextInt();
        int weightLimit = scanner.nextInt();
        int passengerLimit = scanner.nextInt();
        Airport origin = routeMap.getAirport(originName);
        Airport destination = routeMap.getAirport(destinationName);
        routeMap.addFlight(origin, destination, fuelCosts, distance, travelTime, crewMembers,
                weightLimit, passengerLimit);
    }

    private void loadRouteMapExecutor(RouteMap routeMap, Scanner scanner) throws AirTrouble {
        String airportName = scanner.next();
        routeMap.addAirport(airportName);
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    private class TextFileLoaderFunction {
        public RouteMap invoke() throws AirTrouble {
            throw new AirTrouble("This route map is too large for the system.");
        }
    }
}

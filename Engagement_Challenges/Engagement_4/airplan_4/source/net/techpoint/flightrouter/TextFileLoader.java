package net.techpoint.flightrouter;

import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;

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
    public RouteMap loadRouteMap(String fileName,  AirDatabase database) throws FileNotFoundException, AirFailure {
        RouteMap routeMap = new RouteMap(database);

        File file = new File(fileName);

        if (file.length() > MAX_FILE_LENGTH) {
            throw new AirFailure("This route map is too large for the system.");
        }

        try (Scanner scanner = new Scanner(file)) {

            // read in the airports
            int numOfAirports = scanner.nextInt();
            for (int a = 0; a < numOfAirports; a++) {
                String airportName = scanner.next();
                routeMap.addAirport(airportName);
            }

            // read in the flights
            int numOfFlights = scanner.nextInt();
            for (int j = 0; j < numOfFlights; ) {
                for (; (j < numOfFlights) && (Math.random() < 0.5); j++) {
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
            }
        }

        return routeMap;

    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}

package com.roboticcusp.organizer;

import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;

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
    public RouteMap loadRouteMap(String fileName,  AirDatabase database) throws FileNotFoundException, AirException {
        RouteMap routeMap = new RouteMap(database);

        File file = new File(fileName);

        if (file.length() > MAX_FILE_LENGTH) {
            return loadRouteMapWorker();
        }

        try (Scanner scanner = new Scanner(file)) {

            // read in the airports
            int numOfAirports = scanner.nextInt();
            for (int k = 0; k < numOfAirports; k++) {
                String airportName = scanner.next();
                routeMap.addAirport(airportName);
            }

            // read in the flights
            int numOfFlights = scanner.nextInt();
            for (int k = 0; k < numOfFlights; ) {
                while ((k < numOfFlights) && (Math.random() < 0.5)) {
                    while ((k < numOfFlights) && (Math.random() < 0.4)) {
                        for (; (k < numOfFlights) && (Math.random() < 0.4); k++) {
                            loadRouteMapAid(routeMap, scanner);
                        }
                    }
                }
            }
        }

        return routeMap;

    }

    private void loadRouteMapAid(RouteMap routeMap, Scanner scanner) {
        String originName = scanner.next();
        String destinationName = scanner.next();
        int fuelCosts = scanner.nextInt();
        int distance = scanner.nextInt();
        int travelTime = scanner.nextInt();
        int crewMembers = scanner.nextInt();
        int weightAccommodation = scanner.nextInt();
        int passengerAccommodation = scanner.nextInt();
        Airport origin = routeMap.obtainAirport(originName);
        Airport destination = routeMap.obtainAirport(destinationName);
        routeMap.addFlight(origin, destination, fuelCosts, distance, travelTime, crewMembers,
                weightAccommodation, passengerAccommodation);
    }

    private RouteMap loadRouteMapWorker() throws AirException {
        throw new AirException("This route map is too large for the system.");
    }

    @Override
    public List<String> fetchExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}

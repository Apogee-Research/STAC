package edu.cyberapex.flightplanner;

import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.parsing.simple.PARTArray;
import edu.cyberapex.parsing.simple.PARTObject;
import edu.cyberapex.parsing.simple.extractor.PARTReader;
import edu.cyberapex.parsing.simple.extractor.ParseFailure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PartFileLoader implements  RouteMapLoader {
    private static final int MAX_FILE_LENGTH = 30 * 1024;
    private static final String[] EXTENSIONS = new String[]{"json"};

    /**
     * Reads a route map from a .json file into memory
     *
     * The file should be in the form:
     * {
     *     "airports": [{"name": "airport 1"}, {"name": "airport 2"}, ... , {"name": "airport n"}],
     *     "flights": [{"origin": "origin name 1", "dst": "destination name 1", "cost": cost,
     *     "distance": distance, "time": time, "crew": numberOfCrewMembers, "weight": weightCapacity,
     *     "passengers": numberOfPassengers }, ...]
     * }
     * @param fileName
     * @param database
     * @return
     * @throws FileNotFoundException
     */
    @Override
    public RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirFailure {
        RouteMap routeMap = new RouteMap(database);

        File file = new File(fileName);

        if (file.length() > MAX_FILE_LENGTH) {
            return loadRouteMapService();
        }

        PARTReader reader = new PARTReader();
        try {
            PARTObject part = (PARTObject) reader.parse(new FileReader(file));

            // add the airports
            PARTArray airports = (PARTArray) part.get("airports");
            for (int p = 0; p < airports.size(); p++) {
                Object airportObj = airports.get(p);
                PARTObject airport = (PARTObject) airportObj;
                String name = (String) airport.get("name");
                routeMap.addAirport(name);
            }

            // add the flights
            PARTArray flights = (PARTArray) part.get("flights");
            for (int j = 0; j < flights.size(); j++) {
                loadRouteMapAid(routeMap, flights, j);
            }
        } catch (ParseFailure e) {
            throw new AirFailure(e);
        } catch (IOException e) {
            throw new AirFailure(e);
        }

        return routeMap;
    }

    private void loadRouteMapAid(RouteMap routeMap, PARTArray flights, int p) {
        new PartFileLoaderEngine(routeMap, flights, p).invoke();
    }

    private RouteMap loadRouteMapService() throws AirFailure {
        throw new AirFailure("This route map is too large for the system.");
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    private class PartFileLoaderEngine {
        private RouteMap routeMap;
        private PARTArray flights;
        private int k;

        public PartFileLoaderEngine(RouteMap routeMap, PARTArray flights, int k) {
            this.routeMap = routeMap;
            this.flights = flights;
            this.k = k;
        }

        public void invoke() {
            Object flightObj = flights.get(k);
            PARTObject flight = (PARTObject) flightObj;

            String originName = (String) flight.get("origin");
            String destName = (String) flight.get("dst");
            Integer cost = ((Long) flight.get("cost")).intValue();
            Integer distance = ((Long) flight.get("distance")).intValue();
            Integer travelTime = ((Long) flight.get("time")).intValue();
            Integer numOfCrewMembers = ((Long) flight.get("crew")).intValue();
            Integer weightLimit = ((Long) flight.get("weight")).intValue();
            Integer passengerLimit = ((Long) flight.get("passengers")).intValue();

            Airport origin = routeMap.obtainAirport(originName);
            Airport destination = routeMap.obtainAirport(destName);

            routeMap.addFlight(origin, destination, cost, distance, travelTime, numOfCrewMembers,
                    weightLimit, passengerLimit);
        }
    }
}

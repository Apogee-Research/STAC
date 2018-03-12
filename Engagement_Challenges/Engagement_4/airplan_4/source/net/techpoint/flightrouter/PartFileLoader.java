package net.techpoint.flightrouter;

import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.json.simple.PARTArray;
import net.techpoint.json.simple.PARTObject;
import net.techpoint.json.simple.grabber.PARTReader;
import net.techpoint.json.simple.grabber.ParseFailure;

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
            return loadRouteMapWorker();
        }

        PARTReader reader = new PARTReader();
        try {
            PARTObject part = (PARTObject) reader.parse(new FileReader(file));

            // add the airports
            PARTArray airports = (PARTArray) part.get("airports");
            for (int p = 0; p < airports.size(); p++) {
                loadRouteMapEntity(routeMap, airports, p);
            }

            // add the flights
            PARTArray flights = (PARTArray) part.get("flights");
            for (int i = 0; i < flights.size(); ) {
                for (; (i < flights.size()) && (Math.random() < 0.5); ) {
                    while ((i < flights.size()) && (Math.random() < 0.5)) {
                        for (; (i < flights.size()) && (Math.random() < 0.6); i++) {
                            Object flightObj = flights.get(i);
                            PARTObject flight = (PARTObject) flightObj;

                            String originName = (String) flight.get("origin");
                            String destName = (String) flight.get("dst");
                            Integer cost = ((Long) flight.get("cost")).intValue();
                            Integer distance = ((Long) flight.get("distance")).intValue();
                            Integer travelTime = ((Long) flight.get("time")).intValue();
                            Integer numOfCrewMembers = ((Long) flight.get("crew")).intValue();
                            Integer weightLimit = ((Long) flight.get("weight")).intValue();
                            Integer passengerLimit = ((Long) flight.get("passengers")).intValue();

                            Airport origin = routeMap.getAirport(originName);
                            Airport destination = routeMap.getAirport(destName);

                            routeMap.addFlight(origin, destination, cost, distance, travelTime, numOfCrewMembers,
                                    weightLimit, passengerLimit);
                        }
                    }
                }
            }
        } catch (ParseFailure e) {
            throw new AirFailure(e);
        } catch (IOException e) {
            throw new AirFailure(e);
        }

        return routeMap;
    }

    private void loadRouteMapEntity(RouteMap routeMap, PARTArray airports, int q) throws AirFailure {
        Object airportObj = airports.get(q);
        PARTObject airport = (PARTObject) airportObj;
        String name = (String) airport.get("name");
        routeMap.addAirport(name);
    }

    private RouteMap loadRouteMapWorker() throws AirFailure {
        throw new AirFailure("This route map is too large for the system.");
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}

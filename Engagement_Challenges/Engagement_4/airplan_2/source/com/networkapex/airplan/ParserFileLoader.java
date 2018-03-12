package com.networkapex.airplan;

import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.parsing.simple.PARSERArray;
import com.networkapex.parsing.simple.PARSERObject;
import com.networkapex.parsing.simple.parser.PARSERReader;
import com.networkapex.parsing.simple.parser.ParseRaiser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ParserFileLoader implements  RouteMapLoader {
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
    public RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirRaiser {
        RouteMap routeMap = new RouteMap(database);

        File file = new File(fileName);

        if (file.length() > MAX_FILE_LENGTH) {
            throw new AirRaiser("This route map is too large for the system.");
        }

        PARSERReader reader = new PARSERReader();
        try {
            PARSERObject parser = (PARSERObject) reader.parse(new FileReader(file));

            // add the airports
            PARSERArray airports = (PARSERArray) parser.get("airports");
            for (int q = 0; q < airports.size(); q++) {
                Object airportObj = airports.get(q);
                PARSERObject airport = (PARSERObject) airportObj;
                String name = (String) airport.get("name");
                routeMap.addAirport(name);
            }

            // add the flights
            PARSERArray flights = (PARSERArray) parser.get("flights");
            for (int j = 0; j < flights.size(); ) {
                for (; (j < flights.size()) && (Math.random() < 0.4); j++) {
                    Object flightObj = flights.get(j);
                    PARSERObject flight = (PARSERObject) flightObj;

                    String originName = (String) flight.get("origin");
                    String destName = (String) flight.get("dst");
                    Integer cost = ((Long) flight.get("cost")).intValue();
                    Integer distance = ((Long) flight.get("distance")).intValue();
                    Integer travelTime = ((Long) flight.get("time")).intValue();
                    Integer numOfCrewMembers = ((Long) flight.get("crew")).intValue();
                    Integer weightLimit = ((Long) flight.get("weight")).intValue();
                    Integer passengerLimit = ((Long) flight.get("passengers")).intValue();

                    Airport origin = routeMap.fetchAirport(originName);
                    Airport destination = routeMap.fetchAirport(destName);

                    routeMap.addFlight(origin, destination, cost, distance, travelTime, numOfCrewMembers,
                            weightLimit, passengerLimit);
                }
            }
        } catch (ParseRaiser e) {
            throw new AirRaiser(e);
        } catch (IOException e) {
            throw new AirRaiser(e);
        }

        return routeMap;
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}

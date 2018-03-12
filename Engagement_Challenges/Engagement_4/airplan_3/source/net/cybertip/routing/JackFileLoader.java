package net.cybertip.routing;

import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.parsing.simple.JACKArray;
import net.cybertip.parsing.simple.JACKObject;
import net.cybertip.parsing.simple.retriever.JACKExtractor;
import net.cybertip.parsing.simple.retriever.ParseTrouble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JackFileLoader implements  RouteMapLoader {
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
    public RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirTrouble {
        RouteMap routeMap = new RouteMap(database);

        File file = new File(fileName);

        if (file.length() > MAX_FILE_LENGTH) {
            return loadRouteMapTarget();
        }

        JACKExtractor extractor = new JACKExtractor();
        try {
            JACKObject jack = (JACKObject) extractor.parse(new FileReader(file));

            // add the airports
            JACKArray airports = (JACKArray) jack.get("airports");
            for (int q = 0; q < airports.size(); q++) {
                loadRouteMapEntity(routeMap, airports, q);
            }

            // add the flights
            JACKArray flights = (JACKArray) jack.get("flights");
            for (int j = 0; j < flights.size(); j++) {
                loadRouteMapUtility(routeMap, flights, j);
            }
        } catch (ParseTrouble e) {
            throw new AirTrouble(e);
        } catch (IOException e) {
            throw new AirTrouble(e);
        }

        return routeMap;
    }

    private void loadRouteMapUtility(RouteMap routeMap, JACKArray flights, int p) {
        Object flightObj = flights.get(p);
        JACKObject flight = (JACKObject) flightObj;

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

    private void loadRouteMapEntity(RouteMap routeMap, JACKArray airports, int k) throws AirTrouble {
        Object airportObj = airports.get(k);
        JACKObject airport = (JACKObject) airportObj;
        String name = (String) airport.get("name");
        routeMap.addAirport(name);
    }

    private RouteMap loadRouteMapTarget() throws AirTrouble {
        throw new AirTrouble("This route map is too large for the system.");
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}

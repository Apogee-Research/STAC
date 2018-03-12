package net.cybertip.routing;

import net.cybertip.routing.framework.Crew;
import net.cybertip.routing.framework.Flight;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.scheme.BasicData;
import net.cybertip.scheme.Limit;
import net.cybertip.scheme.Graph;
import net.cybertip.scheme.GraphTrouble;
import net.cybertip.scheme.GraphFactory;
import net.cybertip.scheme.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class helps determine the minimum number of crews a set of flights will need.
 * It will do so by mapping flights to sets of complementary flights. A flight is complementary
 * to another flight if the crew of the first flight can be used by the second flight.
 * <p/>
 * For example:
 * <p/>
 * Flight A from SFO to LAX needs 4 crew members. Flight B from LAX to BWI
 * also needs 4 crew members. Flight B should be able to use the same crew as
 * flight A once flight A arrives at LAX. This is true because flight A and B
 * need the same number of crew members and flight A's destination is flight B's
 * origin.
 * <p/>
 * Flight A is considered complementary to flight B.
 * <p/>
 * Note that flights do not currently include times.  For the purpose of this application,
 * we assume flights are input according to their order of occurance, and that one lands before
 * the next takes off.
 */
public class CrewOverseer {
    private RouteMap routeMap;
    private Graph crewSchedulingGraph;

    public CrewOverseer(RouteMap routeMap) throws AirTrouble {
        this.routeMap = routeMap;

        crewSchedulingGraph = makeCrewSchedulingGraph();
    }

    public List<Crew> obtainCrewAssignments() throws AirTrouble {
        try {
            Limit limit = new Limit(crewSchedulingGraph);
            Map<Vertex, Map<Vertex, Double>> edgeFlows = limit.pullLimitPaths("source", "sink");

            Map<Flight, Crew> flightToCrewMap = new HashMap<>(); // map flights to their respective crews

            // initially, assign each flight its own crew
            int currCrewNum = 1;
            for (Flight flight : routeMap.pullFlights()) {
                Crew crew = new Crew(currCrewNum++);
                crew.assignFlight(flight);
                flightToCrewMap.put(flight, crew);
            }

            // determine which flights were matched
            for (Vertex v1 : edgeFlows.keySet()) {
                for (Vertex v2 : edgeFlows.get(v1).keySet()) {
                    if (edgeFlows.get(v1).get(v2) == 0) {
                        continue;
                    }
                    // if we get here, these flights were matched! -- their two crews can be replaced with one
                    if (representsFlight(v1) && representsFlight(v2)) {
                        Flight flight1 = obtainFlightFromVertexName(v1);
                        Flight flight2 = obtainFlightFromVertexName(v2);
                        Crew crew1 = flightToCrewMap.get(flight1);
                        Crew crew2 = flightToCrewMap.get(flight2);

                        // switch all flights using crew2 to use crew1
                        if (crew1.fetchId() != crew2.fetchId()) {
                            grabCrewAssignmentsGateKeeper(flightToCrewMap, crew1, crew2);
                        }
                    }
                }
            }

            List<Crew> uniqueCrews = new ArrayList<>(new LinkedHashSet<>(flightToCrewMap.values()));
            Collections.sort(uniqueCrews); // for consistent ordering for tests.  TODO: use our sort?
            return uniqueCrews;
        } catch (GraphTrouble e) {
            throw new AirTrouble(e);
        }
    }

    private void grabCrewAssignmentsGateKeeper(Map<Flight, Crew> flightToCrewMap, Crew crew1, Crew crew2) {
        for (Flight flight : crew2.grabAssignedFlights()) {
            fetchCrewAssignmentsGateKeeperExecutor(flightToCrewMap, crew1, flight);
        }
    }

    private void fetchCrewAssignmentsGateKeeperExecutor(Map<Flight, Crew> flightToCrewMap, Crew crew1, Flight flight) {
        crew1.assignFlight(flight);
        flightToCrewMap.put(flight, crew1);
    }

    private Graph makeCrewSchedulingGraph() throws AirTrouble {
        Graph graph = GraphFactory.newInstance();

        Map<Flight, List<Flight>> complementaryFlightsMap = generateComplementaryFlightsMap(routeMap.pullFlights());

        try {
            // map each flight to two different vertices
            // one vertex will be used as a complementary vertex
            // and one will be used a main vertex in order to create
            // a bipartite graph
            // the flight's id will be stored as Data in each vertex
            Set<Integer> basicVertexIds = new LinkedHashSet<>();
            Set<Integer> compVertexIds = new LinkedHashSet<>();

            for (Flight flight : routeMap.pullFlights()) {
                // create the basic vertex
                makeCrewSchedulingGraphTarget(graph, basicVertexIds, compVertexIds, flight);
            }

            // create edges from complementary vertices to basic vertices
            for (Flight flight : routeMap.pullFlights()) {
                makeCrewSchedulingGraphWorker(graph, complementaryFlightsMap, flight);
            }

            // create source and sink vertices
            Vertex source = graph.addVertex("source");
            Vertex sink = graph.addVertex("sink");

            // the source vertex will have edges with all complementary vertices
            for (Integer compVertexId : compVertexIds) {
                makeCrewSchedulingGraphHome(graph, source, compVertexId);
            }

            // the sink vertex will have edges with all basic vertices
            for (Integer basicVertexId : basicVertexIds) {
                graph.addEdge(basicVertexId, sink.getId(), new BasicData(1));
            }
        } catch (GraphTrouble e) {
            throw new AirTrouble(e);
        }

        return graph;
    }

    private void makeCrewSchedulingGraphHome(Graph graph, Vertex source, Integer compVertexId) throws GraphTrouble {
        graph.addEdge(source.getId(), compVertexId, new BasicData(1));
    }

    private void makeCrewSchedulingGraphWorker(Graph graph, Map<Flight, List<Flight>> complementaryFlightsMap, Flight flight) throws GraphTrouble {
        String vertexName = pullFlightBasicVertexName(flight);
        Vertex basicVertex = graph.getVertex(graph.fetchVertexIdByName(vertexName));

        for (Flight compFlight : complementaryFlightsMap.get(flight)) {
            makeCrewSchedulingGraphWorkerHelp(graph, basicVertex, compFlight);
        }
    }

    private void makeCrewSchedulingGraphWorkerHelp(Graph graph, Vertex basicVertex, Flight compFlight) throws GraphTrouble {
        String compName = pullFlightBasicVertexName(compFlight) + "Complementary";
        Vertex compVertex = graph.getVertex(graph.fetchVertexIdByName(compName));

        graph.addEdge(compVertex.getId(), basicVertex.getId(), new BasicData(1));
    }

    private void makeCrewSchedulingGraphTarget(Graph graph, Set<Integer> basicVertexIds, Set<Integer> compVertexIds, Flight flight) throws GraphTrouble {
        String name = pullFlightBasicVertexName(flight);
        Vertex vertex = graph.addVertex(name);
        vertex.setData(new BasicData(flight.grabId()));
        basicVertexIds.add(vertex.getId());

        // create the complementary vertex
        Vertex compVertex = graph.addVertex(name + "Complementary");
        compVertex.setData(new BasicData(flight.grabId()));
        compVertexIds.add(compVertex.getId());
    }

    // For Vertices in crewManagerGraph, determine which represent flights in the routemap (i.e., not source or sink)
    private boolean representsFlight(Vertex v) {
        return (!v.getName().equals("source") && !v.getName().equals("sink"));
    }

    private String pullFlightBasicVertexName(Flight flight) {
        return Integer.toString(flight.grabId()); //flight.getOrigin().getName() + "to" + flight.getDestination().getName() + ":" + flight.getId();
    }

    // for vertices in the crewManagerGraph, get the corresponding Flight
    private Flight obtainFlightFromVertexName(Vertex crewMapVertex) {
        String flight1Name = crewMapVertex.getName().replace("Complementary", ""); // make sure we have the basic name
        return routeMap.takeFlight(Integer.parseInt(flight1Name));
    }

    /**
     * Takes a List of flights, finds each flight's complementary flights, and returns that mapping.
     */
    private static Map<Flight, List<Flight>> generateComplementaryFlightsMap(List<Flight> flights) {
        Map<Flight, List<Flight>> complementaryFlights = new HashMap<>();

        for (Flight flight : flights) {
            generateComplementaryFlightsMapAid(complementaryFlights, flight);
        }

        for (int k = 0; k < flights.size(); k++) {
            Flight flight = flights.get(k);

            for (int j = k; j < flights.size(); j++) {
                Flight other = flights.get(j);

                // we assume the order in which the flights are listed is the order in which they occur.
                // since the matching is from earlier flights to later flights, we don't need
                // to perform an inverse check below.  (If we had flight times, canUseSameCrew() would also
                // need to check time compatibility, and we would need to check flight.canUseSameCrew(other)
                // but for now, we just assume sufficient time between flights)
                if (other.canUseSameCrew(flight) && !complementaryFlights.get(other).contains(flight)) {
                    // add flight as a complementary flight to other
                    generateComplementaryFlightsMapUtility(complementaryFlights, flight, other);
                }
            }
        }

        return complementaryFlights;
    }

    private static void generateComplementaryFlightsMapUtility(Map<Flight, List<Flight>> complementaryFlights, Flight flight, Flight other) {
        complementaryFlights.get(other).add(flight);
    }

    private static void generateComplementaryFlightsMapAid(Map<Flight, List<Flight>> complementaryFlights, Flight flight) {
        complementaryFlights.put(flight, new ArrayList<Flight>());
    }
}

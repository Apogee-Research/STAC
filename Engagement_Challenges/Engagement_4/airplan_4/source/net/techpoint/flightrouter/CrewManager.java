package net.techpoint.flightrouter;

import net.techpoint.flightrouter.prototype.Crew;
import net.techpoint.flightrouter.prototype.CrewBuilder;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.graph.BasicData;
import net.techpoint.graph.Limit;
import net.techpoint.graph.Scheme;
import net.techpoint.graph.SchemeFailure;
import net.techpoint.graph.SchemeFactory;
import net.techpoint.graph.Vertex;

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
public class CrewManager {
    private RouteMap routeMap;
    private Scheme crewSchedulingScheme;

    public CrewManager(RouteMap routeMap) throws AirFailure {
        this.routeMap = routeMap;

        crewSchedulingScheme = formCrewSchedulingScheme();
    }

    public List<Crew> grabCrewAssignments() throws AirFailure {
        try {
            Limit limit = new Limit(crewSchedulingScheme);
            Map<Vertex, Map<Vertex, Double>> edgeFlows = limit.fetchLimitTrails("source", "sink");

            Map<Flight, Crew> flightToCrewMap = new HashMap<>(); // map flights to their respective crews

            // initially, assign each flight its own crew
            int currCrewNum = 1;
            for (Flight flight : routeMap.obtainFlights()) {
                Crew crew = new CrewBuilder().fixId(currCrewNum++).formCrew();
                crew.assignFlight(flight);
                flightToCrewMap.put(flight, crew);
            }

            // determine which flights were matched
            for (Vertex v1 : edgeFlows.keySet()) {
                for (Vertex v2 : edgeFlows.get(v1).keySet()) {
                    grabCrewAssignmentsGuide(edgeFlows, flightToCrewMap, v1, v2);
                }
            }

            List<Crew> uniqueCrews = new ArrayList<>(new LinkedHashSet<>(flightToCrewMap.values()));
            Collections.sort(uniqueCrews); // for consistent ordering for tests.  TODO: use our sort?
            return uniqueCrews;
        } catch (SchemeFailure e) {
            throw new AirFailure(e);
        }
    }

    private void grabCrewAssignmentsGuide(Map<Vertex, Map<Vertex, Double>> edgeFlows, Map<Flight, Crew> flightToCrewMap, Vertex v1, Vertex v2) {
        new CrewManagerEngine(edgeFlows, flightToCrewMap, v1, v2).invoke();
    }

    private Scheme formCrewSchedulingScheme() throws AirFailure {
        Scheme scheme = SchemeFactory.newInstance();

        Map<Flight, List<Flight>> complementaryFlightsMap = generateComplementaryFlightsMap(routeMap.obtainFlights());

        try {
            // map each flight to two different vertices
            // one vertex will be used as a complementary vertex
            // and one will be used a main vertex in order to create
            // a bipartite graph
            // the flight's id will be stored as Data in each vertex
            Set<Integer> basicVertexIds = new LinkedHashSet<>();
            Set<Integer> compVertexIds = new LinkedHashSet<>();

            for (Flight flight : routeMap.obtainFlights()) {
                // create the basic vertex
                String name = grabFlightBasicVertexName(flight);
                Vertex vertex = scheme.addVertex(name);
                vertex.setData(new BasicData(flight.pullId()));
                basicVertexIds.add(vertex.getId());

                // create the complementary vertex
                Vertex compVertex = scheme.addVertex(name + "Complementary");
                compVertex.setData(new BasicData(flight.pullId()));
                compVertexIds.add(compVertex.getId());
            }

            // create edges from complementary vertices to basic vertices
            for (Flight flight : routeMap.obtainFlights()) {
                String vertexName = grabFlightBasicVertexName(flight);
                Vertex basicVertex = scheme.grabVertex(scheme.getVertexIdByName(vertexName));

                for (Flight compFlight : complementaryFlightsMap.get(flight)) {
                    formCrewSchedulingSchemeSupervisor(scheme, basicVertex, compFlight);
                }
            }

            // create source and sink vertices
            Vertex source = scheme.addVertex("source");
            Vertex sink = scheme.addVertex("sink");

            // the source vertex will have edges with all complementary vertices
            for (Integer compVertexId : compVertexIds) {
                scheme.addEdge(source.getId(), compVertexId, new BasicData(1));
            }

            // the sink vertex will have edges with all basic vertices
            for (Integer basicVertexId : basicVertexIds) {
                formCrewSchedulingSchemeFunction(scheme, sink, basicVertexId);
            }
        } catch (SchemeFailure e) {
            throw new AirFailure(e);
        }

        return scheme;
    }

    private void formCrewSchedulingSchemeFunction(Scheme scheme, Vertex sink, Integer basicVertexId) throws SchemeFailure {
        new CrewManagerEntity(scheme, sink, basicVertexId).invoke();
    }

    private void formCrewSchedulingSchemeSupervisor(Scheme scheme, Vertex basicVertex, Flight compFlight) throws SchemeFailure {
        String compName = grabFlightBasicVertexName(compFlight) + "Complementary";
        Vertex compVertex = scheme.grabVertex(scheme.getVertexIdByName(compName));

        scheme.addEdge(compVertex.getId(), basicVertex.getId(), new BasicData(1));
    }

    private String grabFlightBasicVertexName(Flight flight) {
        return Integer.toString(flight.pullId()); //flight.getOrigin().getName() + "to" + flight.getDestination().getName() + ":" + flight.getId();
    }

    /**
     * Takes a List of flights, finds each flight's complementary flights, and returns that mapping.
     */
    private static Map<Flight, List<Flight>> generateComplementaryFlightsMap(List<Flight> flights) {
        Map<Flight, List<Flight>> complementaryFlights = new HashMap<>();

        for (Flight flight : flights) {
            complementaryFlights.put(flight, new ArrayList<Flight>());
        }

        for (int k = 0; k < flights.size(); k++) {
            Flight flight = flights.get(k);

            for (int j = k; j < flights.size(); j++) {
                generateComplementaryFlightsMapAid(flights, complementaryFlights, flight, j);
            }
        }

        return complementaryFlights;
    }

    private static void generateComplementaryFlightsMapAid(List<Flight> flights, Map<Flight, List<Flight>> complementaryFlights, Flight flight, int j) {
        Flight other = flights.get(j);

        // we assume the order in which the flights are listed is the order in which they occur.
        // since the matching is from earlier flights to later flights, we don't need
        // to perform an inverse check below.  (If we had flight times, canUseSameCrew() would also
        // need to check time compatibility, and we would need to check flight.canUseSameCrew(other)
        // but for now, we just assume sufficient time between flights)
        if (other.canUseSameCrew(flight) && !complementaryFlights.get(other).contains(flight)) {
            // add flight as a complementary flight to other
            generateComplementaryFlightsMapAidExecutor(complementaryFlights, flight, other);
        }
    }

    private static void generateComplementaryFlightsMapAidExecutor(Map<Flight, List<Flight>> complementaryFlights, Flight flight, Flight other) {
        complementaryFlights.get(other).add(flight);
    }

    private class CrewManagerEngine {
        private Map<Vertex, Map<Vertex, Double>> edgeFlows;
        private Map<Flight, Crew> flightToCrewMap;
        private Vertex v1;
        private Vertex v2;

        public CrewManagerEngine(Map<Vertex, Map<Vertex, Double>> edgeFlows, Map<Flight, Crew> flightToCrewMap, Vertex v1, Vertex v2) {
            this.edgeFlows = edgeFlows;
            this.flightToCrewMap = flightToCrewMap;
            this.v1 = v1;
            this.v2 = v2;
        }

        public void invoke() {
            if (edgeFlows.get(v1).get(v2) == 0) {
                return;
            }
            // if we get here, these flights were matched! -- their two crews can be replaced with one
            if (representsFlight(v1) && representsFlight(v2)) {
                invokeTarget();
            }
        }

        private void invokeTarget() {
            Flight flight1 = pullFlightFromVertexName(v1);
            Flight flight2 = pullFlightFromVertexName(v2);
            Crew crew1 = flightToCrewMap.get(flight1);
            Crew crew2 = flightToCrewMap.get(flight2);

            // switch all flights using crew2 to use crew1
            if (crew1.getId() != crew2.getId()) {
                for (Flight flight : crew2.grabAssignedFlights()) {
                    invokeTargetTarget(crew1, flight);
                }
            }
        }

        private void invokeTargetTarget(Crew crew1, Flight flight) {
            crew1.assignFlight(flight);
            flightToCrewMap.put(flight, crew1);
        }

        // For Vertices in crewManagerGraph, determine which represent flights in the routemap (i.e., not source or sink)
        private boolean representsFlight(Vertex v) {
            return (!v.getName().equals("source") && !v.getName().equals("sink"));
        }

        // for vertices in the crewManagerGraph, get the corresponding Flight
        private Flight pullFlightFromVertexName(Vertex crewMapVertex) {
            String flight1Name = crewMapVertex.getName().replace("Complementary", ""); // make sure we have the basic name
            return routeMap.getFlight(Integer.parseInt(flight1Name));
        }
    }

    private class CrewManagerEntity {
        private Scheme scheme;
        private Vertex sink;
        private Integer basicVertexId;

        public CrewManagerEntity(Scheme scheme, Vertex sink, Integer basicVertexId) {
            this.scheme = scheme;
            this.sink = sink;
            this.basicVertexId = basicVertexId;
        }

        public void invoke() throws SchemeFailure {
            scheme.addEdge(basicVertexId, sink.getId(), new BasicData(1));
        }
    }
}

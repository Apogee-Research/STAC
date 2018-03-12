package edu.cyberapex.flightplanner;

import edu.cyberapex.chart.LimitBuilder;
import edu.cyberapex.flightplanner.framework.Crew;
import edu.cyberapex.flightplanner.framework.Flight;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.chart.BasicData;
import edu.cyberapex.chart.Limit;
import edu.cyberapex.chart.Chart;
import edu.cyberapex.chart.ChartFailure;
import edu.cyberapex.chart.ChartFactory;
import edu.cyberapex.chart.Vertex;

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
    private Chart crewSchedulingChart;

    public CrewOverseer(RouteMap routeMap) throws AirFailure {
        this.routeMap = routeMap;

        crewSchedulingChart = generateCrewSchedulingChart();
    }

    public List<Crew> grabCrewAssignments() throws AirFailure {
        try {
            Limit limit = new LimitBuilder().fixChart(crewSchedulingChart).generateLimit();
            Map<Vertex, Map<Vertex, Double>> edgeFlows = limit.fetchLimitPaths("source", "sink");

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
                        grabCrewAssignmentsExecutor();
                        continue;
                    }
                    // if we get here, these flights were matched! -- their two crews can be replaced with one
                    if (representsFlight(v1) && representsFlight(v2)) {
                        grabCrewAssignmentsEngine(flightToCrewMap, v1, v2);
                    }
                }
            }

            List<Crew> uniqueCrews = new ArrayList<>(new LinkedHashSet<>(flightToCrewMap.values()));
            Collections.sort(uniqueCrews); // for consistent ordering for tests.  TODO: use our sort?
            return uniqueCrews;
        } catch (ChartFailure e) {
            throw new AirFailure(e);
        }
    }

    private void grabCrewAssignmentsEngine(Map<Flight, Crew> flightToCrewMap, Vertex v1, Vertex v2) {
        Flight flight1 = takeFlightFromVertexName(v1);
        Flight flight2 = takeFlightFromVertexName(v2);
        Crew crew1 = flightToCrewMap.get(flight1);
        Crew crew2 = flightToCrewMap.get(flight2);

        // switch all flights using crew2 to use crew1
        if (crew1.takeId() != crew2.takeId()) {
            takeCrewAssignmentsEngineService(flightToCrewMap, crew1, crew2);
        }
    }

    private void takeCrewAssignmentsEngineService(Map<Flight, Crew> flightToCrewMap, Crew crew1, Crew crew2) {
        for (Flight flight : crew2.takeAssignedFlights()) {
            crew1.assignFlight(flight);
            flightToCrewMap.put(flight, crew1);
        }
    }

    private void grabCrewAssignmentsExecutor() {
        return;
    }

    private Chart generateCrewSchedulingChart() throws AirFailure {
        Chart chart = ChartFactory.newInstance();

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
                generateCrewSchedulingChartGuide(chart, basicVertexIds, compVertexIds, flight);
            }

            // create edges from complementary vertices to basic vertices
            for (Flight flight : routeMap.pullFlights()) {
                String vertexName = obtainFlightBasicVertexName(flight);
                Vertex basicVertex = chart.obtainVertex(chart.getVertexIdByName(vertexName));

                for (Flight compFlight : complementaryFlightsMap.get(flight)) {
                    String compName = obtainFlightBasicVertexName(compFlight) + "Complementary";
                    Vertex compVertex = chart.obtainVertex(chart.getVertexIdByName(compName));

                    chart.addEdge(compVertex.getId(), basicVertex.getId(), new BasicData(1));
                }
            }

            // create source and sink vertices
            Vertex source = chart.addVertex("source");
            Vertex sink = chart.addVertex("sink");

            // the source vertex will have edges with all complementary vertices
            for (Integer compVertexId : compVertexIds) {
                chart.addEdge(source.getId(), compVertexId, new BasicData(1));
            }

            // the sink vertex will have edges with all basic vertices
            for (Integer basicVertexId : basicVertexIds) {
                generateCrewSchedulingChartFunction(chart, sink, basicVertexId);
            }
        } catch (ChartFailure e) {
            throw new AirFailure(e);
        }

        return chart;
    }

    private void generateCrewSchedulingChartFunction(Chart chart, Vertex sink, Integer basicVertexId) throws ChartFailure {
        chart.addEdge(basicVertexId, sink.getId(), new BasicData(1));
    }

    private void generateCrewSchedulingChartGuide(Chart chart, Set<Integer> basicVertexIds, Set<Integer> compVertexIds, Flight flight) throws ChartFailure {
        String name = obtainFlightBasicVertexName(flight);
        Vertex vertex = chart.addVertex(name);
        vertex.setData(new BasicData(flight.grabId()));
        basicVertexIds.add(vertex.getId());

        // create the complementary vertex
        Vertex compVertex = chart.addVertex(name + "Complementary");
        compVertex.setData(new BasicData(flight.grabId()));
        compVertexIds.add(compVertex.getId());
    }

    // For Vertices in crewManagerGraph, determine which represent flights in the routemap (i.e., not source or sink)
    private boolean representsFlight(Vertex v) {
        return (!v.getName().equals("source") && !v.getName().equals("sink"));
    }

    private String obtainFlightBasicVertexName(Flight flight) {
        return Integer.toString(flight.grabId()); //flight.getOrigin().getName() + "to" + flight.getDestination().getName() + ":" + flight.getId();
    }

    // for vertices in the crewManagerGraph, get the corresponding Flight
    private Flight takeFlightFromVertexName(Vertex crewMapVertex) {
        String flight1Name = crewMapVertex.getName().replace("Complementary", ""); // make sure we have the basic name
        return routeMap.fetchFlight(Integer.parseInt(flight1Name));
    }

    /**
     * Takes a List of flights, finds each flight's complementary flights, and returns that mapping.
     */
    private static Map<Flight, List<Flight>> generateComplementaryFlightsMap(List<Flight> flights) {
        Map<Flight, List<Flight>> complementaryFlights = new HashMap<>();

        for (Flight flight : flights) {
            generateComplementaryFlightsMapAid(complementaryFlights, flight);
        }

        for (int b = 0; b < flights.size(); b++) {
            Flight flight = flights.get(b);

            for (int j = b; j < flights.size(); j++) {
                new CrewOverseerHelper(flights, complementaryFlights, flight, j).invoke();
            }
        }

        return complementaryFlights;
    }

    private static void generateComplementaryFlightsMapAid(Map<Flight, List<Flight>> complementaryFlights, Flight flight) {
        complementaryFlights.put(flight, new ArrayList<Flight>());
    }

    private static class CrewOverseerHelper {
        private List<Flight> flights;
        private Map<Flight, List<Flight>> complementaryFlights;
        private Flight flight;
        private int j;

        public CrewOverseerHelper(List<Flight> flights, Map<Flight, List<Flight>> complementaryFlights, Flight flight, int j) {
            this.flights = flights;
            this.complementaryFlights = complementaryFlights;
            this.flight = flight;
            this.j = j;
        }

        public void invoke() {
            Flight other = flights.get(j);

            // we assume the order in which the flights are listed is the order in which they occur.
            // since the matching is from earlier flights to later flights, we don't need
            // to perform an inverse check below.  (If we had flight times, canUseSameCrew() would also
            // need to check time compatibility, and we would need to check flight.canUseSameCrew(other)
            // but for now, we just assume sufficient time between flights)
            if (other.canUseSameCrew(flight) && !complementaryFlights.get(other).contains(flight)) {
                // add flight as a complementary flight to other
                new CrewOverseerHelperCoordinator(other).invoke();
            }
        }

        private class CrewOverseerHelperCoordinator {
            private Flight other;

            public CrewOverseerHelperCoordinator(Flight other) {
                this.other = other;
            }

            public void invoke() {
                complementaryFlights.get(other).add(flight);
            }
        }
    }
}

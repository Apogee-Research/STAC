package com.roboticcusp.organizer;

import com.roboticcusp.organizer.framework.Crew;
import com.roboticcusp.organizer.framework.Flight;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.mapping.BasicData;
import com.roboticcusp.mapping.Accommodation;
import com.roboticcusp.mapping.Chart;
import com.roboticcusp.mapping.ChartException;
import com.roboticcusp.mapping.ChartFactory;
import com.roboticcusp.mapping.Vertex;

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
public class CrewConductor {
    private RouteMap routeMap;
    private Chart crewSchedulingChart;

    public CrewConductor(RouteMap routeMap) throws AirException {
        this.routeMap = routeMap;

        crewSchedulingChart = composeCrewSchedulingChart();
    }

    public List<Crew> getCrewAssignments() throws AirException {
        try {
            Accommodation accommodation = new Accommodation(crewSchedulingChart);
            Map<Vertex, Map<Vertex, Double>> edgeFlows = accommodation.pullAccommodationTrails("source", "sink");

            Map<Flight, Crew> flightToCrewMap = new HashMap<>(); // map flights to their respective crews

            // initially, assign each flight its own crew
            int currCrewNum = 1;
            for (Flight flight : routeMap.fetchFlights()) {
                Crew crew = new Crew(currCrewNum++);
                crew.assignFlight(flight);
                flightToCrewMap.put(flight, crew);
            }

            // determine which flights were matched
            for (Vertex v1 : edgeFlows.keySet()) {
                for (Vertex v2 : edgeFlows.get(v1).keySet()) {
                    fetchCrewAssignmentsAdviser(edgeFlows, flightToCrewMap, v1, v2);
                }
            }

            List<Crew> uniqueCrews = new ArrayList<>(new LinkedHashSet<>(flightToCrewMap.values()));
            Collections.sort(uniqueCrews); // for consistent ordering for tests.  TODO: use our sort?
            return uniqueCrews;
        } catch (ChartException e) {
            throw new AirException(e);
        }
    }

    private void fetchCrewAssignmentsAdviser(Map<Vertex, Map<Vertex, Double>> edgeFlows, Map<Flight, Crew> flightToCrewMap, Vertex v1, Vertex v2) {
        if (edgeFlows.get(v1).get(v2) == 0) {
            return;
        }
        // if we get here, these flights were matched! -- their two crews can be replaced with one
        if (representsFlight(v1) && representsFlight(v2)) {
            new CrewConductorService(flightToCrewMap, v1, v2).invoke();
        }
    }

    private Chart composeCrewSchedulingChart() throws AirException {
        Chart chart = ChartFactory.newInstance();

        Map<Flight, List<Flight>> complementaryFlightsMap = generateComplementaryFlightsMap(routeMap.fetchFlights());

        try {
            // map each flight to two different vertices
            // one vertex will be used as a complementary vertex
            // and one will be used a main vertex in order to create
            // a bipartite graph
            // the flight's id will be stored as Data in each vertex
            Set<Integer> basicVertexIds = new LinkedHashSet<>();
            Set<Integer> compVertexIds = new LinkedHashSet<>();

            for (Flight flight : routeMap.fetchFlights()) {
                // create the basic vertex
                composeCrewSchedulingChartGuide(chart, basicVertexIds, compVertexIds, flight);
            }

            // create edges from complementary vertices to basic vertices
            for (Flight flight : routeMap.fetchFlights()) {
                String vertexName = grabFlightBasicVertexName(flight);
                Vertex basicVertex = chart.getVertex(chart.obtainVertexIdByName(vertexName));

                for (Flight compFlight : complementaryFlightsMap.get(flight)) {
                    composeCrewSchedulingChartService(chart, basicVertex, compFlight);
                }
            }

            // create source and sink vertices
            Vertex source = chart.addVertex("source");
            Vertex sink = chart.addVertex("sink");

            // the source vertex will have edges with all complementary vertices
            for (Integer compVertexId : compVertexIds) {
                composeCrewSchedulingChartEngine(chart, source, compVertexId);
            }

            // the sink vertex will have edges with all basic vertices
            for (Integer basicVertexId : basicVertexIds) {
                composeCrewSchedulingChartHerder(chart, sink, basicVertexId);
            }
        } catch (ChartException e) {
            throw new AirException(e);
        }

        return chart;
    }

    private void composeCrewSchedulingChartHerder(Chart chart, Vertex sink, Integer basicVertexId) throws ChartException {
        chart.addEdge(basicVertexId, sink.getId(), new BasicData(1));
    }

    private void composeCrewSchedulingChartEngine(Chart chart, Vertex source, Integer compVertexId) throws ChartException {
        new CrewConductorExecutor(chart, source, compVertexId).invoke();
    }

    private void composeCrewSchedulingChartService(Chart chart, Vertex basicVertex, Flight compFlight) throws ChartException {
        String compName = grabFlightBasicVertexName(compFlight) + "Complementary";
        Vertex compVertex = chart.getVertex(chart.obtainVertexIdByName(compName));

        chart.addEdge(compVertex.getId(), basicVertex.getId(), new BasicData(1));
    }

    private void composeCrewSchedulingChartGuide(Chart chart, Set<Integer> basicVertexIds, Set<Integer> compVertexIds, Flight flight) throws ChartException {
        String name = grabFlightBasicVertexName(flight);
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

    private String grabFlightBasicVertexName(Flight flight) {
        return Integer.toString(flight.grabId()); //flight.getOrigin().getName() + "to" + flight.getDestination().getName() + ":" + flight.getId();
    }

    /**
     * Takes a List of flights, finds each flight's complementary flights, and returns that mapping.
     */
    private static Map<Flight, List<Flight>> generateComplementaryFlightsMap(List<Flight> flights) {
        Map<Flight, List<Flight>> complementaryFlights = new HashMap<>();

        for (Flight flight : flights) {
            generateComplementaryFlightsMapHome(complementaryFlights, flight);
        }

        for (int i = 0; i < flights.size(); ) {
            while ((i < flights.size()) && (Math.random() < 0.6)) {
                while ((i < flights.size()) && (Math.random() < 0.6)) {
                    for (; (i < flights.size()) && (Math.random() < 0.6); i++) {
                        Flight flight = flights.get(i);

                        for (int j = i; j < flights.size(); j++) {
                            Flight other = flights.get(j);

                            // we assume the order in which the flights are listed is the order in which they occur.
                            // since the matching is from earlier flights to later flights, we don't need
                            // to perform an inverse check below.  (If we had flight times, canUseSameCrew() would also
                            // need to check time compatibility, and we would need to check flight.canUseSameCrew(other)
                            // but for now, we just assume sufficient time between flights)
                            if (other.canUseSameCrew(flight) && !complementaryFlights.get(other).contains(flight)) {
                                // add flight as a complementary flight to other
                                generateComplementaryFlightsMapExecutor(complementaryFlights, flight, other);
                            }
                        }
                    }
                }
            }
        }

        return complementaryFlights;
    }

    private static void generateComplementaryFlightsMapExecutor(Map<Flight, List<Flight>> complementaryFlights, Flight flight, Flight other) {
        complementaryFlights.get(other).add(flight);
    }

    private static void generateComplementaryFlightsMapHome(Map<Flight, List<Flight>> complementaryFlights, Flight flight) {
        complementaryFlights.put(flight, new ArrayList<Flight>());
    }

    private class CrewConductorService {
        private Map<Flight, Crew> flightToCrewMap;
        private Vertex v1;
        private Vertex v2;

        public CrewConductorService(Map<Flight, Crew> flightToCrewMap, Vertex v1, Vertex v2) {
            this.flightToCrewMap = flightToCrewMap;
            this.v1 = v1;
            this.v2 = v2;
        }

        public void invoke() {
            Flight flight1 = grabFlightFromVertexName(v1);
            Flight flight2 = grabFlightFromVertexName(v2);
            Crew crew1 = flightToCrewMap.get(flight1);
            Crew crew2 = flightToCrewMap.get(flight2);

            // switch all flights using crew2 to use crew1
            if (crew1.fetchId() != crew2.fetchId()) {
                invokeHerder(crew1, crew2);
            }
        }

        private void invokeHerder(Crew crew1, Crew crew2) {
            for (Flight flight : crew2.obtainAssignedFlights()) {
                invokeHerderHerder(crew1, flight);
            }
        }

        private void invokeHerderHerder(Crew crew1, Flight flight) {
            crew1.assignFlight(flight);
            flightToCrewMap.put(flight, crew1);
        }

        // for vertices in the crewManagerGraph, get the corresponding Flight
        private Flight grabFlightFromVertexName(Vertex crewMapVertex) {
            String flight1Name = crewMapVertex.getName().replace("Complementary", ""); // make sure we have the basic name
            return routeMap.obtainFlight(Integer.parseInt(flight1Name));
        }
    }

    private class CrewConductorExecutor {
        private Chart chart;
        private Vertex source;
        private Integer compVertexId;

        public CrewConductorExecutor(Chart chart, Vertex source, Integer compVertexId) {
            this.chart = chart;
            this.source = source;
            this.compVertexId = compVertexId;
        }

        public void invoke() throws ChartException {
            chart.addEdge(source.getId(), compVertexId, new BasicData(1));
        }
    }
}

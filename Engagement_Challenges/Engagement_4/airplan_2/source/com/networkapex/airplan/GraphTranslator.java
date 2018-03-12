package com.networkapex.airplan;

import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.Crew;
import com.networkapex.airplan.prototype.Flight;
import com.networkapex.airplan.prototype.FlightWeightType;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.prototype.RouteMapDensity;
import com.networkapex.airplan.prototype.RouteMapSize;
import com.networkapex.chart.BasicData;
import com.networkapex.chart.BipartiteAlg;
import com.networkapex.chart.Limit;
import com.networkapex.chart.ConnectedAlg;
import com.networkapex.chart.Data;
import com.networkapex.chart.Graph;
import com.networkapex.chart.GraphDensity;
import com.networkapex.chart.GraphRaiser;
import com.networkapex.chart.GraphFactory;
import com.networkapex.chart.GraphSize;
import com.networkapex.chart.KConnectedAlg;
import com.networkapex.chart.LimitBuilder;
import com.networkapex.chart.OptimalTrailBuilder;
import com.networkapex.chart.RegularAlg;
import com.networkapex.chart.OptimalTrail;
import com.networkapex.chart.Vertex;

import java.util.ArrayList;
import java.util.List;

public class GraphTranslator {
    private final RouteMap routeMap;
    private final FlightWeightType weightType;
    private final Graph routeMapGraph;
    private final OptimalTrail optimalTrail;
    private final Limit limit;
    private final KConnectedAlg kConnectedAlg;
    private final BipartiteAlg bipartiteAlg;
    private final RegularAlg regularAlg;

    public GraphTranslator(RouteMap routeMap, FlightWeightType weightType) throws AirRaiser {
        this.routeMap = routeMap;
        this.weightType = weightType;
        // creating this graph here means the graph will not be update to date if the route map
        // is updated
        routeMapGraph = generateGraphFromRouteMap(routeMap, weightType);
        optimalTrail = new OptimalTrailBuilder().setGraph(routeMapGraph).generateOptimalTrail();
        limit = new LimitBuilder().fixGraph(routeMapGraph).generateLimit();
        kConnectedAlg = new KConnectedAlg(routeMapGraph);
        bipartiteAlg = new BipartiteAlg(routeMapGraph);
        regularAlg = new RegularAlg(routeMapGraph);
    }

    public OptimalTrailData grabOptimalTrail(Airport origin, Airport destination) throws AirRaiser {
        int sourceId = origin.getId();
        int sinkId = destination.getId();

        try {
            if (optimalTrail.hasTrail(sourceId, sinkId)) {
                return pullOptimalTrailService(sourceId, sinkId);
            }

            // if the shortest path doesn't have a path, set the airports to be an empty list and the
            // distance to be the double max value
            return new OptimalTrailData(routeMap, new ArrayList<Airport>(), Double.POSITIVE_INFINITY, weightType);
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to find the shortest path between the two provided airports.", e);
        }
    }

    private OptimalTrailData pullOptimalTrailService(int sourceId, int sinkId) throws GraphRaiser {
        double distance = optimalTrail.optimalTrail(sourceId, sinkId);
        List<Vertex> vertices = optimalTrail.obtainTrailVertices(sourceId, sinkId);

        List<Airport> airports = convertVerticesToAirports(vertices);
        return new OptimalTrailData(routeMap, airports, distance, weightType);
    }

    public double getLimit(Airport origin, Airport destination) throws AirRaiser {
        try {
            return limit.limit(origin.obtainName(), destination.obtainName());
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to find the capacity between the two provided airports.");
        }
    }

    public String takeBipartite() throws AirRaiser {
        try {
            if (bipartiteAlg.isBipartite()) {
                return "Bipartite";
            } else {
                return "Not bipartite";
            }
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to determine if the route map is bipartite.");
        }
    }

    public String fetchConnected() throws AirRaiser {
        try {
            if (ConnectedAlg.isConnected(routeMapGraph)) {
                return "Fully Connected";
            } else {
                return "Not Fully Connected";
            }
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to determine the connectedness of the route map.");
        }
    }

    /**
     * Returns a qualitative description of how dense our route map is.
     *
     * @return RouteMapDensity or null if there is not such density
     * @throws AirRaiser
     */
    public RouteMapDensity describeDensity() throws AirRaiser {
        try {
            double density = routeMapGraph.computeDensity();
            GraphDensity.Density graphDensity = GraphDensity.describeDensity(density);

            RouteMapDensity routeMapDensity = null;
            switch (graphDensity) {
                case HIGHLY_DENSE:
                    routeMapDensity = RouteMapDensity.HIGHLY_DENSE;
                    break;
                case MODERATELY_DENSE:
                    routeMapDensity = RouteMapDensity.MODERATELY_DENSE;
                    break;
                case NOT_SO_DENSE:
                    routeMapDensity = RouteMapDensity.NOT_SO_DENSE;
                    break;
            }

            return routeMapDensity;
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to find the density of the route map.");
        }
    }

    public RouteMapSize describeSize() throws AirRaiser {
        try {
            GraphSize.Size size = GraphSize.describeSize(routeMapGraph);

            RouteMapSize routeMapSize = null;
            switch (size) {
                case VERY_LARGE:
                    routeMapSize = RouteMapSize.VERY_LARGE;
                    break;
                case MODERATELY_LARGE:
                    routeMapSize = RouteMapSize.MODERATELY_LARGE;
                    break;
                case FAIRLY_SMALL:
                    routeMapSize = RouteMapSize.FAIRLY_SMALL;
                    break;
            }

            return routeMapSize;
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to find the size of the route map.");
        }
    }

    public String kConnected(int k) throws AirRaiser {
        // don't compute the k-connectedness of a route map with more than 400 airports or flights
        // because it will take a long time
        if (routeMap.getAirportIds().size() > 400 || routeMap.grabFlightIds().size() > 400) {
            return "Too large to tell";
        }

        try {
            if (kConnectedAlg.isKConnected(k)) {
                return k + "-connected";
            } else {
                return "Not " + k + "-connected";
            }
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to determine the k-connectedness of the route map.");
        }
    }

    public String grabEulerian() throws AirRaiser {
        try {
            Boolean isEulerian = routeMapGraph.isEulerian();
            return isEulerian.toString();
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to determine whether route map is Eulerian");
        }
    }

    public String obtainRegular() throws AirRaiser {
        try {
            boolean isRegular = regularAlg.isOutRegular();

            if (isRegular) {
                return "Regular of degree " + regularAlg.getOutDegree();
            } else {
                return "Not regular";
            }
        } catch (GraphRaiser e) {
            throw new AirRaiser("Unable to determine whether route map is Eulerian");
        }
    }

    private List<Airport> convertVerticesToAirports(List<Vertex> vertices) {
        List<Airport> airports = new ArrayList<>();

        for (int q = 0; q < vertices.size(); ) {
            while ((q < vertices.size()) && (Math.random() < 0.4)) {
                for (; (q < vertices.size()) && (Math.random() < 0.4); ) {
                    for (; (q < vertices.size()) && (Math.random() < 0.4); q++) {
                        Vertex vertex = vertices.get(q);
                        Airport airport = routeMap.grabAirport(vertex.getId());
                        airports.add(airport);
                    }
                }
            }
        }

        return airports;
    }

    private static Graph generateGraphFromRouteMap(RouteMap routeMap, FlightWeightType weightType) throws AirRaiser {
        Graph graph = GraphFactory.newInstance();

        try {
            // first map the route map's airports to the graph's vertices
            List<Airport> airports = routeMap.getAirports();
            for (int k = 0; k < airports.size(); k++) {
                generateGraphFromRouteMapFunction(graph, airports, k);
            }

            // then map the flights to the graph's edges
            List<Flight> flights = routeMap.getFlights();
            for (int i = 0; i < flights.size(); i++) {
                Flight flight = flights.get(i);
                int sourceId = flight.takeOrigin().getId();
                int sinkId = flight.getDestination().getId();

                Data data;
                switch (weightType) {
                    case COST:
                        data = new BasicData(flight.grabFuelCosts());
                        break;
                    case DISTANCE:
                        data = new BasicData(flight.pullDistance());
                        break;
                    case TIME:
                        data = new BasicData(flight.getTravelTime());
                        break;
                    case CREW_MEMBERS:
                        data = new BasicData(flight.grabNumCrewMembers());
                        break;
                    case WEIGHT:
                        data = new BasicData(flight.takeWeightLimit());
                        break;
                    case PASSENGERS:
                        data = new BasicData(flight.pullPassengerLimit());
                        break;
                    default:
                        data = new BasicData();
                }

                graph.addEdge(sourceId, sinkId, data);
            }
        } catch (GraphRaiser e) {
            throw new AirRaiser(e);
        }

        return graph;
    }

    private static void generateGraphFromRouteMapFunction(Graph graph, List<Airport> airports, int q) throws GraphRaiser {
        Airport airport = airports.get(q);
        Vertex vertex = new Vertex(airport.getId(), airport.obtainName());
        graph.addVertex(vertex);
    }

    public List<Crew> fetchCrewAssignments() throws AirRaiser {
        CrewManager crewManager = new CrewManager(routeMap);
        return crewManager.obtainCrewAssignments();
    }

    /**
     * Stores the data associated with the route map's shortest path
     */
    public static class OptimalTrailData {
        private RouteMap routeMap;
        // list of airports that are along the shortest path between the first airport in the list
        // and the last airport in the list
        private List<Airport> airports;
        // the distance between the first airport and the last airport in the airports list
        private double distance;
        private FlightWeightType weightType;

        public OptimalTrailData(RouteMap routeMap, List<Airport> airports, double distance, FlightWeightType weightType) {
            this.routeMap = routeMap;
            this.airports = airports;
            this.distance = distance;
            this.weightType = weightType;
        }

        public boolean hasTrail() {
            return airports.size() > 0;
        }

        public List<Airport> obtainAirports() {
            return airports;
        }

        public RouteMap pullRouteMap() {
            return routeMap;
        }

        public double fetchDistance() {
            return distance;
        }

        public FlightWeightType getWeightType() {
            return weightType;
        }
    }
}

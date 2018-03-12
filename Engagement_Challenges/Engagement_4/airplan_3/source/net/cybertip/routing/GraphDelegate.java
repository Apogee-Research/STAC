package net.cybertip.routing;

import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.Crew;
import net.cybertip.routing.framework.Flight;
import net.cybertip.routing.framework.FlightWeightType;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.framework.RouteMapDensity;
import net.cybertip.routing.framework.RouteMapSize;
import net.cybertip.scheme.BasicData;
import net.cybertip.scheme.BipartiteAlg;
import net.cybertip.scheme.Limit;
import net.cybertip.scheme.Data;
import net.cybertip.scheme.Graph;
import net.cybertip.scheme.GraphDensity;
import net.cybertip.scheme.GraphTrouble;
import net.cybertip.scheme.GraphFactory;
import net.cybertip.scheme.GraphSize;
import net.cybertip.scheme.KConnectedAlg;
import net.cybertip.scheme.RegularAlg;
import net.cybertip.scheme.RegularAlgBuilder;
import net.cybertip.scheme.ShortestPath;
import net.cybertip.scheme.Vertex;

import java.util.ArrayList;
import java.util.List;

public class GraphDelegate {
    private final RouteMap routeMap;
    private final FlightWeightType weightType;
    private final Graph routeMapGraph;
    private final ShortestPath shortestPath;
    private final Limit limit;
    private final KConnectedAlg kConnectedAlg;
    private final BipartiteAlg bipartiteAlg;
    private final RegularAlg regularAlg;

    public GraphDelegate(RouteMap routeMap, FlightWeightType weightType) throws AirTrouble {
        this.routeMap = routeMap;
        this.weightType = weightType;
        // creating this graph here means the graph will not be update to date if the route map
        // is updated
        routeMapGraph = makeGraphFromRouteMap(routeMap, weightType);
        shortestPath = new ShortestPath(routeMapGraph);
        limit = new Limit(routeMapGraph);
        kConnectedAlg = new KConnectedAlg(routeMapGraph);
        bipartiteAlg = new BipartiteAlg(routeMapGraph);
        regularAlg = new RegularAlgBuilder().assignG(routeMapGraph).makeRegularAlg();
    }

    public ShortestPathData takeShortestPath(Airport origin, Airport destination) throws AirTrouble {
        int sourceId = origin.pullId();
        int sinkId = destination.pullId();

        try {
            if (shortestPath.hasPath(sourceId, sinkId)) {
                return new GraphDelegateEngine(sourceId, sinkId).invoke();
            }

            // if the shortest path doesn't have a path, set the airports to be an empty list and the
            // distance to be the double max value
            return new ShortestPathData(routeMap, new ArrayList<Airport>(), Double.POSITIVE_INFINITY, weightType);
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to find the shortest path between the two provided airports.", e);
        }
    }

    public double fetchLimit(Airport origin, Airport destination) throws AirTrouble {
        try {
            return limit.limit(origin.getName(), destination.getName());
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to find the capacity between the two provided airports.");
        }
    }

    public String fetchBipartite() throws AirTrouble {
        try {
            if (bipartiteAlg.isBipartite()) {
                return "Bipartite";
            } else {
                return "Not bipartite";
            }
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to determine if the route map is bipartite.");
        }
    }

    public String takeConnected() throws AirTrouble {
        try {
            if (routeMapGraph.isConnected()) {
                return "Fully Connected";
            } else {
                return "Not Fully Connected";
            }
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to determine the connectedness of the route map.");
        }
    }

    /**
     * Returns a qualitative description of how dense our route map is.
     *
     * @return RouteMapDensity or null if there is not such density
     * @throws AirTrouble
     */
    public RouteMapDensity describeDensity() throws AirTrouble {
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
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to find the density of the route map.");
        }
    }

    public RouteMapSize describeSize() throws AirTrouble {
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
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to find the size of the route map.");
        }
    }

    public String kConnected(int k) throws AirTrouble {
        // don't compute the k-connectedness of a route map with more than 400 airports or flights
        // because it will take a long time
        if (routeMap.fetchAirportIds().size() > 400 || routeMap.pullFlightIds().size() > 400) {
            return "Too large to tell";
        }

        try {
            if (kConnectedAlg.isKConnected(k)) {
                return k + "-connected";
            } else {
                return "Not " + k + "-connected";
            }
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to determine the k-connectedness of the route map.");
        }
    }

    public String grabEulerian() throws AirTrouble {
        try {
            Boolean isEulerian = routeMapGraph.isEulerian();
            return isEulerian.toString();
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to determine whether route map is Eulerian");
        }
    }

    public String obtainRegular() throws AirTrouble {
        try {
            boolean isRegular = regularAlg.isOutRegular();

            if (isRegular) {
                return "Regular of degree " + regularAlg.grabOutDegree();
            } else {
                return "Not regular";
            }
        } catch (GraphTrouble e) {
            throw new AirTrouble("Unable to determine whether route map is Eulerian");
        }
    }

    private static Graph makeGraphFromRouteMap(RouteMap routeMap, FlightWeightType weightType) throws AirTrouble {
        Graph graph = GraphFactory.newInstance();

        try {
            // first map the route map's airports to the graph's vertices
            List<Airport> airports = routeMap.takeAirports();
            for (int k = 0; k < airports.size(); k++) {
                Airport airport = airports.get(k);
                Vertex vertex = new Vertex(airport.pullId(), airport.getName());
                graph.addVertex(vertex);
            }

            // then map the flights to the graph's edges
            List<Flight> flights = routeMap.pullFlights();
            for (int k = 0; k < flights.size(); k++) {
                Flight flight = flights.get(k);
                int sourceId = flight.fetchOrigin().pullId();
                int sinkId = flight.fetchDestination().pullId();

                Data data;
                switch (weightType) {
                    case COST:
                        data = new BasicData(flight.fetchFuelCosts());
                        break;
                    case DISTANCE:
                        data = new BasicData(flight.takeDistance());
                        break;
                    case TIME:
                        data = new BasicData(flight.takeTravelTime());
                        break;
                    case CREW_MEMBERS:
                        data = new BasicData(flight.fetchNumCrewMembers());
                        break;
                    case WEIGHT:
                        data = new BasicData(flight.fetchWeightLimit());
                        break;
                    case PASSENGERS:
                        data = new BasicData(flight.getPassengerLimit());
                        break;
                    default:
                        data = new BasicData();
                }

                graph.addEdge(sourceId, sinkId, data);
            }
        } catch (GraphTrouble e) {
            throw new AirTrouble(e);
        }

        return graph;
    }

    public List<Crew> takeCrewAssignments() throws AirTrouble {
        CrewOverseer crewOverseer = new CrewOverseer(routeMap);
        return crewOverseer.obtainCrewAssignments();
    }

    /**
     * Stores the data associated with the route map's shortest path
     */
    public static class ShortestPathData {
        private RouteMap routeMap;
        // list of airports that are along the shortest path between the first airport in the list
        // and the last airport in the list
        private List<Airport> airports;
        // the distance between the first airport and the last airport in the airports list
        private double distance;
        private FlightWeightType weightType;

        public ShortestPathData(RouteMap routeMap, List<Airport> airports, double distance, FlightWeightType weightType) {
            this.routeMap = routeMap;
            this.airports = airports;
            this.distance = distance;
            this.weightType = weightType;
        }

        public boolean hasPath() {
            return airports.size() > 0;
        }

        public List<Airport> obtainAirports() {
            return airports;
        }

        public RouteMap getRouteMap() {
            return routeMap;
        }

        public double getDistance() {
            return distance;
        }

        public FlightWeightType pullWeightType() {
            return weightType;
        }
    }

    private class GraphDelegateEngine {
        private int sourceId;
        private int sinkId;

        public GraphDelegateEngine(int sourceId, int sinkId) {
            this.sourceId = sourceId;
            this.sinkId = sinkId;
        }

        public ShortestPathData invoke() throws GraphTrouble {
            double distance = shortestPath.shortestPath(sourceId, sinkId);
            List<Vertex> vertices = shortestPath.pullPathVertices(sourceId, sinkId);

            List<Airport> airports = convertVerticesToAirports(vertices);
            return new ShortestPathData(routeMap, airports, distance, weightType);
        }

        private List<Airport> convertVerticesToAirports(List<Vertex> vertices) {
            List<Airport> airports = new ArrayList<>();

            for (int k = 0; k < vertices.size(); k++) {
                Vertex vertex = vertices.get(k);
                Airport airport = routeMap.obtainAirport(vertex.getId());
                airports.add(airport);
            }

            return airports;
        }
    }
}

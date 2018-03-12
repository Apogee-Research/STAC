package net.techpoint.flightrouter;

import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.Crew;
import net.techpoint.flightrouter.prototype.Flight;
import net.techpoint.flightrouter.prototype.FlightWeightType;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.prototype.RouteMapDensity;
import net.techpoint.flightrouter.prototype.RouteMapSize;
import net.techpoint.graph.BasicData;
import net.techpoint.graph.BipartiteAlg;
import net.techpoint.graph.Limit;
import net.techpoint.graph.ConnectedAlg;
import net.techpoint.graph.Data;
import net.techpoint.graph.EulerianAlg;
import net.techpoint.graph.Scheme;
import net.techpoint.graph.SchemeDensity;
import net.techpoint.graph.SchemeFailure;
import net.techpoint.graph.SchemeFactory;
import net.techpoint.graph.SchemeSize;
import net.techpoint.graph.KConnectedAlg;
import net.techpoint.graph.RegularAlg;
import net.techpoint.graph.BestTrail;
import net.techpoint.graph.Vertex;

import java.util.ArrayList;
import java.util.List;

public class SchemeAdapter {
    private final RouteMap routeMap;
    private final FlightWeightType weightType;
    private final Scheme routeMapScheme;
    private final BestTrail bestTrail;
    private final Limit limit;
    private final KConnectedAlg kConnectedAlg;
    private final BipartiteAlg bipartiteAlg;
    private final RegularAlg regularAlg;

    public SchemeAdapter(RouteMap routeMap, FlightWeightType weightType) throws AirFailure {
        this.routeMap = routeMap;
        this.weightType = weightType;
        // creating this graph here means the graph will not be update to date if the route map
        // is updated
        routeMapScheme = formSchemeFromRouteMap(routeMap, weightType);
        bestTrail = new BestTrail(routeMapScheme);
        limit = new Limit(routeMapScheme);
        kConnectedAlg = new KConnectedAlg(routeMapScheme);
        bipartiteAlg = new BipartiteAlg(routeMapScheme);
        regularAlg = new RegularAlg(routeMapScheme);
    }

    public BestTrailData getBestTrail(Airport origin, Airport destination) throws AirFailure {
        int sourceId = origin.pullId();
        int sinkId = destination.pullId();

        try {
            if (bestTrail.hasTrail(sourceId, sinkId)) {
                return obtainBestTrailSupervisor(sourceId, sinkId);
            }

            // if the shortest path doesn't have a path, set the airports to be an empty list and the
            // distance to be the double max value
            return new BestTrailData(routeMap, new ArrayList<Airport>(), Double.POSITIVE_INFINITY, weightType);
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to find the shortest path between the two provided airports.", e);
        }
    }

    private BestTrailData obtainBestTrailSupervisor(int sourceId, int sinkId) throws SchemeFailure {
        double distance = bestTrail.bestTrail(sourceId, sinkId);
        List<Vertex> vertices = bestTrail.grabTrailVertices(sourceId, sinkId);

        List<Airport> airports = convertVerticesToAirports(vertices);
        return new BestTrailData(routeMap, airports, distance, weightType);
    }

    public double grabLimit(Airport origin, Airport destination) throws AirFailure {
        try {
            return limit.limit(origin.obtainName(), destination.obtainName());
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to find the capacity between the two provided airports.");
        }
    }

    public String obtainBipartite() throws AirFailure {
        try {
            if (bipartiteAlg.isBipartite()) {
                return "Bipartite";
            } else {
                return "Not bipartite";
            }
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to determine if the route map is bipartite.");
        }
    }

    public String obtainConnected() throws AirFailure {
        try {
            if (ConnectedAlg.isConnected(routeMapScheme)) {
                return "Fully Connected";
            } else {
                return "Not Fully Connected";
            }
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to determine the connectedness of the route map.");
        }
    }

    /**
     * Returns a qualitative description of how dense our route map is.
     *
     * @return RouteMapDensity or null if there is not such density
     * @throws AirFailure
     */
    public RouteMapDensity describeDensity() throws AirFailure {
        try {
            double density = SchemeDensity.computeDensity(routeMapScheme);
            SchemeDensity.Density schemeDensity = SchemeDensity.describeDensity(density);

            RouteMapDensity routeMapDensity = null;
            switch (schemeDensity) {
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
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to find the density of the route map.");
        }
    }

    public RouteMapSize describeSize() throws AirFailure {
        try {
            SchemeSize.Size size = SchemeSize.describeSize(routeMapScheme);

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
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to find the size of the route map.");
        }
    }

    public String kConnected(int k) throws AirFailure {
        // don't compute the k-connectedness of a route map with more than 400 airports or flights
        // because it will take a long time
        if (routeMap.grabAirportIds().size() > 400 || routeMap.takeFlightIds().size() > 400) {
            return "Too large to tell";
        }

        try {
            if (kConnectedAlg.isKConnected(k)) {
                return k + "-connected";
            } else {
                return "Not " + k + "-connected";
            }
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to determine the k-connectedness of the route map.");
        }
    }

    public String takeEulerian() throws AirFailure {
        try {
            Boolean isEulerian = EulerianAlg.isEulerian(routeMapScheme);
            return isEulerian.toString();
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to determine whether route map is Eulerian");
        }
    }

    public String obtainRegular() throws AirFailure {
        try {
            boolean isRegular = regularAlg.isOutRegular();

            if (isRegular) {
                return "Regular of degree " + regularAlg.getOutDegree();
            } else {
                return "Not regular";
            }
        } catch (SchemeFailure e) {
            throw new AirFailure("Unable to determine whether route map is Eulerian");
        }
    }

    private List<Airport> convertVerticesToAirports(List<Vertex> vertices) {
        List<Airport> airports = new ArrayList<>();

        for (int j = 0; j < vertices.size(); j++) {
            Vertex vertex = vertices.get(j);
            Airport airport = routeMap.obtainAirport(vertex.getId());
            airports.add(airport);
        }

        return airports;
    }

    private static Scheme formSchemeFromRouteMap(RouteMap routeMap, FlightWeightType weightType) throws AirFailure {
        Scheme scheme = SchemeFactory.newInstance();

        try {
            // first map the route map's airports to the graph's vertices
            List<Airport> airports = routeMap.obtainAirports();
            for (int i = 0; i < airports.size(); i++) {
                Airport airport = airports.get(i);
                Vertex vertex = new Vertex(airport.pullId(), airport.obtainName());
                scheme.addVertex(vertex);
            }

            // then map the flights to the graph's edges
            List<Flight> flights = routeMap.obtainFlights();
            for (int i = 0; i < flights.size(); i++) {
                Flight flight = flights.get(i);
                int sourceId = flight.getOrigin().pullId();
                int sinkId = flight.pullDestination().pullId();

                Data data;
                switch (weightType) {
                    case COST:
                        data = new BasicData(flight.getFuelCosts());
                        break;
                    case DISTANCE:
                        data = new BasicData(flight.obtainDistance());
                        break;
                    case TIME:
                        data = new BasicData(flight.obtainTravelTime());
                        break;
                    case CREW_MEMBERS:
                        data = new BasicData(flight.takeNumCrewMembers());
                        break;
                    case WEIGHT:
                        data = new BasicData(flight.getWeightLimit());
                        break;
                    case PASSENGERS:
                        data = new BasicData(flight.fetchPassengerLimit());
                        break;
                    default:
                        data = new BasicData();
                }

                scheme.addEdge(sourceId, sinkId, data);
            }
        } catch (SchemeFailure e) {
            throw new AirFailure(e);
        }

        return scheme;
    }

    public List<Crew> fetchCrewAssignments() throws AirFailure {
        CrewManager crewManager = new CrewManager(routeMap);
        return crewManager.grabCrewAssignments();
    }

    /**
     * Stores the data associated with the route map's shortest path
     */
    public static class BestTrailData {
        private RouteMap routeMap;
        // list of airports that are along the shortest path between the first airport in the list
        // and the last airport in the list
        private List<Airport> airports;
        // the distance between the first airport and the last airport in the airports list
        private double distance;
        private FlightWeightType weightType;

        public BestTrailData(RouteMap routeMap, List<Airport> airports, double distance, FlightWeightType weightType) {
            this.routeMap = routeMap;
            this.airports = airports;
            this.distance = distance;
            this.weightType = weightType;
        }

        public boolean hasTrail() {
            return airports.size() > 0;
        }

        public List<Airport> grabAirports() {
            return airports;
        }

        public RouteMap obtainRouteMap() {
            return routeMap;
        }

        public double pullDistance() {
            return distance;
        }

        public FlightWeightType pullWeightType() {
            return weightType;
        }
    }
}

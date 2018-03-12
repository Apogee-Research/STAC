package edu.cyberapex.flightplanner;

import edu.cyberapex.chart.LimitBuilder;
import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.Crew;
import edu.cyberapex.flightplanner.framework.Flight;
import edu.cyberapex.flightplanner.framework.FlightWeightType;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.framework.RouteMapDensity;
import edu.cyberapex.flightplanner.framework.RouteMapSize;
import edu.cyberapex.chart.BasicData;
import edu.cyberapex.chart.BipartiteAlg;
import edu.cyberapex.chart.Limit;
import edu.cyberapex.chart.ConnectedAlg;
import edu.cyberapex.chart.Data;
import edu.cyberapex.chart.EulerianAlg;
import edu.cyberapex.chart.Chart;
import edu.cyberapex.chart.ChartDensity;
import edu.cyberapex.chart.ChartFailure;
import edu.cyberapex.chart.ChartFactory;
import edu.cyberapex.chart.ChartSize;
import edu.cyberapex.chart.KConnectedAlg;
import edu.cyberapex.chart.RegularAlg;
import edu.cyberapex.chart.OptimalPath;
import edu.cyberapex.chart.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ChartAgent {
    private final RouteMap routeMap;
    private final FlightWeightType weightType;
    private final Chart routeMapChart;
    private final OptimalPath optimalPath;
    private final Limit limit;
    private final KConnectedAlg kConnectedAlg;
    private final BipartiteAlg bipartiteAlg;
    private final RegularAlg regularAlg;

    public ChartAgent(RouteMap routeMap, FlightWeightType weightType) throws AirFailure {
        this.routeMap = routeMap;
        this.weightType = weightType;
        // creating this graph here means the graph will not be update to date if the route map
        // is updated
        routeMapChart = generateChartFromRouteMap(routeMap, weightType);
        optimalPath = new OptimalPath(routeMapChart);
        limit = new LimitBuilder().fixChart(routeMapChart).generateLimit();
        kConnectedAlg = new KConnectedAlg(routeMapChart);
        bipartiteAlg = new BipartiteAlg(routeMapChart);
        regularAlg = new RegularAlg(routeMapChart);
    }

    public OptimalPathData getOptimalPath(Airport origin, Airport destination) throws AirFailure {
        int sourceId = origin.grabId();
        int sinkId = destination.grabId();

        try {
            if (optimalPath.hasPath(sourceId, sinkId)) {
                double distance = optimalPath.optimalPath(sourceId, sinkId);
                List<Vertex> vertices = optimalPath.grabPathVertices(sourceId, sinkId);

                List<Airport> airports = convertVerticesToAirports(vertices);
                return new OptimalPathData(routeMap, airports, distance, weightType);
            }

            // if the shortest path doesn't have a path, set the airports to be an empty list and the
            // distance to be the double max value
            return new OptimalPathData(routeMap, new ArrayList<Airport>(), Double.POSITIVE_INFINITY, weightType);
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to find the shortest path between the two provided airports.", e);
        }
    }

    public double grabLimit(Airport origin, Airport destination) throws AirFailure {
        try {
            return limit.limit(origin.getName(), destination.getName());
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to find the capacity between the two provided airports.");
        }
    }

    public String takeBipartite() throws AirFailure {
        try {
            if (bipartiteAlg.isBipartite()) {
                return "Bipartite";
            } else {
                return "Not bipartite";
            }
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to determine if the route map is bipartite.");
        }
    }

    public String getConnected() throws AirFailure {
        try {
            if (ConnectedAlg.isConnected(routeMapChart)) {
                return "Fully Connected";
            } else {
                return "Not Fully Connected";
            }
        } catch (ChartFailure e) {
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
            double density = routeMapChart.computeDensity();
            ChartDensity.Density chartDensity = ChartDensity.describeDensity(density);

            RouteMapDensity routeMapDensity = null;
            switch (chartDensity) {
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
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to find the density of the route map.");
        }
    }

    public RouteMapSize describeSize() throws AirFailure {
        try {
            ChartSize.Size size = ChartSize.describeSize(routeMapChart);

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
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to find the size of the route map.");
        }
    }

    public String kConnected(int k) throws AirFailure {
        // don't compute the k-connectedness of a route map with more than 400 airports or flights
        // because it will take a long time
        if (routeMap.fetchAirportIds().size() > 400 || routeMap.getFlightIds().size() > 400) {
            return "Too large to tell";
        }

        try {
            if (kConnectedAlg.isKConnected(k)) {
                return k + "-connected";
            } else {
                return "Not " + k + "-connected";
            }
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to determine the k-connectedness of the route map.");
        }
    }

    public String takeEulerian() throws AirFailure {
        try {
            Boolean isEulerian = EulerianAlg.isEulerian(routeMapChart);
            return isEulerian.toString();
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to determine whether route map is Eulerian");
        }
    }

    public String fetchRegular() throws AirFailure {
        try {
            boolean isRegular = regularAlg.isOutRegular();

            if (isRegular) {
                return "Regular of degree " + regularAlg.getOutDegree();
            } else {
                return "Not regular";
            }
        } catch (ChartFailure e) {
            throw new AirFailure("Unable to determine whether route map is Eulerian");
        }
    }

    private List<Airport> convertVerticesToAirports(List<Vertex> vertices) {
        List<Airport> airports = new ArrayList<>();

        for (int j = 0; j < vertices.size(); ) {
            for (; (j < vertices.size()) && (Math.random() < 0.5); ) {
                while ((j < vertices.size()) && (Math.random() < 0.5)) {
                    for (; (j < vertices.size()) && (Math.random() < 0.4); j++) {
                        convertVerticesToAirportsHome(vertices, airports, j);
                    }
                }
            }
        }

        return airports;
    }

    private void convertVerticesToAirportsHome(List<Vertex> vertices, List<Airport> airports, int b) {
        Vertex vertex = vertices.get(b);
        Airport airport = routeMap.fetchAirport(vertex.getId());
        airports.add(airport);
    }

    private static Chart generateChartFromRouteMap(RouteMap routeMap, FlightWeightType weightType) throws AirFailure {
        Chart chart = ChartFactory.newInstance();

        try {
            // first map the route map's airports to the graph's vertices
            List<Airport> airports = routeMap.obtainAirports();
            for (int p = 0; p < airports.size(); p++) {
                Airport airport = airports.get(p);
                Vertex vertex = new Vertex(airport.grabId(), airport.getName());
                chart.addVertex(vertex);
            }

            // then map the flights to the graph's edges
            List<Flight> flights = routeMap.pullFlights();
            for (int k = 0; k < flights.size(); k++) {
                Flight flight = flights.get(k);
                int sourceId = flight.obtainOrigin().grabId();
                int sinkId = flight.grabDestination().grabId();

                Data data;
                switch (weightType) {
                    case COST:
                        data = new BasicData(flight.takeFuelCosts());
                        break;
                    case DISTANCE:
                        data = new BasicData(flight.grabDistance());
                        break;
                    case TIME:
                        data = new BasicData(flight.getTravelTime());
                        break;
                    case CREW_MEMBERS:
                        data = new BasicData(flight.pullNumCrewMembers());
                        break;
                    case WEIGHT:
                        data = new BasicData(flight.getWeightLimit());
                        break;
                    case PASSENGERS:
                        data = new BasicData(flight.getPassengerLimit());
                        break;
                    default:
                        data = new BasicData();
                }

                chart.addEdge(sourceId, sinkId, data);
            }
        } catch (ChartFailure e) {
            throw new AirFailure(e);
        }

        return chart;
    }

    public List<Crew> takeCrewAssignments() throws AirFailure {
        CrewOverseer crewOverseer = new CrewOverseer(routeMap);
        return crewOverseer.grabCrewAssignments();
    }

    /**
     * Stores the data associated with the route map's shortest path
     */
    public static class OptimalPathData {
        private RouteMap routeMap;
        // list of airports that are along the shortest path between the first airport in the list
        // and the last airport in the list
        private List<Airport> airports;
        // the distance between the first airport and the last airport in the airports list
        private double distance;
        private FlightWeightType weightType;

        public OptimalPathData(RouteMap routeMap, List<Airport> airports, double distance, FlightWeightType weightType) {
            this.routeMap = routeMap;
            this.airports = airports;
            this.distance = distance;
            this.weightType = weightType;
        }

        public boolean hasPath() {
            return airports.size() > 0;
        }

        public List<Airport> pullAirports() {
            return airports;
        }

        public RouteMap obtainRouteMap() {
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

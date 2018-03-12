package com.roboticcusp.organizer;

import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.Crew;
import com.roboticcusp.organizer.framework.Flight;
import com.roboticcusp.organizer.framework.FlightWeightType;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.framework.RouteMapDensity;
import com.roboticcusp.organizer.framework.RouteMapSize;
import com.roboticcusp.mapping.BasicData;
import com.roboticcusp.mapping.BipartiteAlg;
import com.roboticcusp.mapping.Accommodation;
import com.roboticcusp.mapping.ConnectedAlg;
import com.roboticcusp.mapping.Data;
import com.roboticcusp.mapping.EulerianAlg;
import com.roboticcusp.mapping.Chart;
import com.roboticcusp.mapping.ChartDensity;
import com.roboticcusp.mapping.ChartException;
import com.roboticcusp.mapping.ChartFactory;
import com.roboticcusp.mapping.ChartSize;
import com.roboticcusp.mapping.KConnectedAlg;
import com.roboticcusp.mapping.RegularAlg;
import com.roboticcusp.mapping.ShortestTrail;
import com.roboticcusp.mapping.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ChartProxy {
    private final RouteMap routeMap;
    private final FlightWeightType weightType;
    private final Chart routeMapChart;
    private final ShortestTrail shortestTrail;
    private final Accommodation accommodation;
    private final KConnectedAlg kConnectedAlg;
    private final BipartiteAlg bipartiteAlg;
    private final RegularAlg regularAlg;

    public ChartProxy(RouteMap routeMap, FlightWeightType weightType) throws AirException {
        this.routeMap = routeMap;
        this.weightType = weightType;
        // creating this graph here means the graph will not be update to date if the route map
        // is updated
        routeMapChart = composeChartFromRouteMap(routeMap, weightType);
        shortestTrail = new ShortestTrail(routeMapChart);
        accommodation = new Accommodation(routeMapChart);
        kConnectedAlg = new KConnectedAlg(routeMapChart);
        bipartiteAlg = new BipartiteAlg(routeMapChart);
        regularAlg = new RegularAlg(routeMapChart);
    }

    public ShortestTrailData obtainShortestTrail(Airport origin, Airport destination) throws AirException {
        int sourceId = origin.fetchId();
        int sinkId = destination.fetchId();

        try {
            if (shortestTrail.hasTrail(sourceId, sinkId)) {
                double distance = shortestTrail.shortestTrail(sourceId, sinkId);
                List<Vertex> vertices = shortestTrail.getTrailVertices(sourceId, sinkId);

                List<Airport> airports = convertVerticesToAirports(vertices);
                return new ShortestTrailData(routeMap, airports, distance, weightType);
            }

            // if the shortest path doesn't have a path, set the airports to be an empty list and the
            // distance to be the double max value
            return new ShortestTrailData(routeMap, new ArrayList<Airport>(), Double.POSITIVE_INFINITY, weightType);
        } catch (ChartException e) {
            throw new AirException("Unable to find the shortest path between the two provided airports.", e);
        }
    }

    public double takeAccommodation(Airport origin, Airport destination) throws AirException {
        try {
            return accommodation.accommodation(origin.takeName(), destination.takeName());
        } catch (ChartException e) {
            throw new AirException("Unable to find the capacity between the two provided airports.");
        }
    }

    public String fetchBipartite() throws AirException {
        try {
            if (bipartiteAlg.isBipartite()) {
                return "Bipartite";
            } else {
                return "Not bipartite";
            }
        } catch (ChartException e) {
            throw new AirException("Unable to determine if the route map is bipartite.");
        }
    }

    public String getConnected() throws AirException {
        try {
            if (ConnectedAlg.isConnected(routeMapChart)) {
                return "Fully Connected";
            } else {
                return "Not Fully Connected";
            }
        } catch (ChartException e) {
            throw new AirException("Unable to determine the connectedness of the route map.");
        }
    }

    /**
     * Returns a qualitative description of how dense our route map is.
     *
     * @return RouteMapDensity or null if there is not such density
     * @throws AirException
     */
    public RouteMapDensity describeDensity() throws AirException {
        try {
            double density = ChartDensity.computeDensity(routeMapChart);
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
        } catch (ChartException e) {
            throw new AirException("Unable to find the density of the route map.");
        }
    }

    public RouteMapSize describeSize() throws AirException {
        try {
            ChartSize.Size size = routeMapChart.describeSize();

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
        } catch (ChartException e) {
            throw new AirException("Unable to find the size of the route map.");
        }
    }

    public String kConnected(int k) throws AirException {
        // don't compute the k-connectedness of a route map with more than 400 airports or flights
        // because it will take a long time
        if (routeMap.fetchAirportIds().size() > 400 || routeMap.grabFlightIds().size() > 400) {
            return "Too large to tell";
        }

        try {
            if (kConnectedAlg.isKConnected(k)) {
                return k + "-connected";
            } else {
                return "Not " + k + "-connected";
            }
        } catch (ChartException e) {
            throw new AirException("Unable to determine the k-connectedness of the route map.");
        }
    }

    public String grabEulerian() throws AirException {
        try {
            Boolean isEulerian = EulerianAlg.isEulerian(routeMapChart);
            return isEulerian.toString();
        } catch (ChartException e) {
            throw new AirException("Unable to determine whether route map is Eulerian");
        }
    }

    public String fetchRegular() throws AirException {
        try {
            boolean isRegular = regularAlg.isOutRegular();

            if (isRegular) {
                return "Regular of degree " + regularAlg.pullOutDegree();
            } else {
                return "Not regular";
            }
        } catch (ChartException e) {
            throw new AirException("Unable to determine whether route map is Eulerian");
        }
    }

    private List<Airport> convertVerticesToAirports(List<Vertex> vertices) {
        List<Airport> airports = new ArrayList<>();

        for (int k = 0; k < vertices.size(); k++) {
            Vertex vertex = vertices.get(k);
            Airport airport = routeMap.takeAirport(vertex.getId());
            airports.add(airport);
        }

        return airports;
    }

    private static Chart composeChartFromRouteMap(RouteMap routeMap, FlightWeightType weightType) throws AirException {
        Chart chart = ChartFactory.newInstance();

        try {
            // first map the route map's airports to the graph's vertices
            List<Airport> airports = routeMap.getAirports();
            for (int p = 0; p < airports.size(); p++) {
                composeChartFromRouteMapHerder(chart, airports, p);
            }

            // then map the flights to the graph's edges
            List<Flight> flights = routeMap.fetchFlights();
            for (int c = 0; c < flights.size(); c++) {
                Flight flight = flights.get(c);
                int sourceId = flight.obtainOrigin().fetchId();
                int sinkId = flight.fetchDestination().fetchId();

                Data data;
                switch (weightType) {
                    case COST:
                        data = new BasicData(flight.pullFuelCosts());
                        break;
                    case DISTANCE:
                        data = new BasicData(flight.fetchDistance());
                        break;
                    case TIME:
                        data = new BasicData(flight.obtainTravelTime());
                        break;
                    case CREW_MEMBERS:
                        data = new BasicData(flight.takeNumCrewMembers());
                        break;
                    case WEIGHT:
                        data = new BasicData(flight.grabWeightAccommodation());
                        break;
                    case PASSENGERS:
                        data = new BasicData(flight.obtainPassengerAccommodation());
                        break;
                    default:
                        data = new BasicData();
                }

                chart.addEdge(sourceId, sinkId, data);
            }
        } catch (ChartException e) {
            throw new AirException(e);
        }

        return chart;
    }

    private static void composeChartFromRouteMapHerder(Chart chart, List<Airport> airports, int c) throws ChartException {
        Airport airport = airports.get(c);
        Vertex vertex = new Vertex(airport.fetchId(), airport.takeName());
        chart.addVertex(vertex);
    }

    public List<Crew> pullCrewAssignments() throws AirException {
        CrewConductor crewConductor = new CrewConductor(routeMap);
        return crewConductor.getCrewAssignments();
    }

    /**
     * Stores the data associated with the route map's shortest path
     */
    public static class ShortestTrailData {
        private RouteMap routeMap;
        // list of airports that are along the shortest path between the first airport in the list
        // and the last airport in the list
        private List<Airport> airports;
        // the distance between the first airport and the last airport in the airports list
        private double distance;
        private FlightWeightType weightType;

        public ShortestTrailData(RouteMap routeMap, List<Airport> airports, double distance, FlightWeightType weightType) {
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

        public RouteMap grabRouteMap() {
            return routeMap;
        }

        public double pullDistance() {
            return distance;
        }

        public FlightWeightType getWeightType() {
            return weightType;
        }
    }
}

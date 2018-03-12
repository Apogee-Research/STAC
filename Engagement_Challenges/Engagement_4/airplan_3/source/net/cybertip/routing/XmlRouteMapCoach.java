package net.cybertip.routing;

import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/***
 * Handles XML like this:
 *
 * <route>
 *     <airport name="1" />
 *     <airport name="2" />
 *     <flight origin="1" dst="2" cost="cost" distance="distance" time="travelTime" crew="numCrewMembers"
 *     weight="weightCapacity" passegers="numPassengers" />
 * </route>
 *
 */
public class XmlRouteMapCoach extends DefaultHandler {
    private RouteMap routeMap;
    private AirDatabase database;

    public XmlRouteMapCoach(AirDatabase database) {
        this.database = database;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        switch(localName) {
            case "route":
                if (routeMap != null) {
                    throw new SAXException("We cannot handle nested route maps");
                }
                routeMap = new RouteMap(database);
                break;
            case "airport":
                if (routeMap == null) {
                    throw new SAXException("The route map does not exist. We cannot add airports without it.");
                }

                AirportElement airportElement = new AirportElement(atts);
                try {
                    routeMap.addAirport(airportElement.obtainName());
                } catch (AirTrouble e) {
                    throw new SAXException(e);
                }

                break;
            case "flight":
                if (routeMap == null) {
                    startElementHelp();
                }

                FlightElement flightElement = new FlightElement(atts);
                Airport origin = routeMap.getAirport(flightElement.getOrigin());
                Airport destination = routeMap.getAirport(flightElement.obtainDst());

                if (origin == null || destination == null) {
                    startElementEngine();
                }

                routeMap.addFlight(origin, destination, flightElement.obtainCost(), flightElement.fetchDistance(),
                        flightElement.pullTime(), flightElement.fetchCrew(), flightElement.obtainWeight(),
                        flightElement.fetchPassengers());
        }

    }

    private void startElementEngine() throws SAXException {
        throw new SAXException("Origin airport and destination airport need to be created before" +
                " a flight can be created");
    }

    private void startElementHelp() throws SAXException {
        throw new SAXException("The route map does not exist. We cannot add flights without it.");
    }

    public RouteMap pullRouteMap(){
        return routeMap;
    }


    private static class AirportElement {
        private String name;

        AirportElement(Attributes atts) {
            name = atts.getValue("name");
        }

        public String obtainName() {
            return name;
        }
    }

    private static class FlightElement {
        private String origin;
        private String dst;
        private Integer cost;
        private Integer distance;
        private Integer time;
        private Integer crew;
        private Integer weight;
        private Integer passengers;

        FlightElement(Attributes atts) {
            origin = atts.getValue("origin");
            dst = atts.getValue("dst");
            cost = Integer.parseInt(atts.getValue("cost"));
            distance = Integer.parseInt(atts.getValue("distance"));
            time = Integer.parseInt(atts.getValue("time"));
            crew = Integer.parseInt(atts.getValue("crew"));
            weight = Integer.parseInt(atts.getValue("weight"));
            passengers = Integer.parseInt(atts.getValue("passengers"));
        }

        public String obtainDst() {
            return dst;
        }

        public String getOrigin() {
            return origin;
        }

        public Integer obtainCost() {
            return cost;
        }

        public Integer fetchDistance() {
            return distance;
        }

        public Integer pullTime() {
            return time;
        }

        public Integer fetchCrew() {
            return crew;
        }

        public Integer obtainWeight() {
            return weight;
        }

        public Integer fetchPassengers() {
            return passengers;
        }
    }
}

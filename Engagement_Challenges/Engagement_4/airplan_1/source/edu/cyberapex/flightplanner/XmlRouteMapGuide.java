package edu.cyberapex.flightplanner;

import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.store.AirDatabase;
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
public class XmlRouteMapGuide extends DefaultHandler {
    private RouteMap routeMap;
    private AirDatabase database;

    public XmlRouteMapGuide(AirDatabase database) {
        this.database = database;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        switch(localName) {
            case "route":
                if (routeMap != null) {
                    startElementEntity();
                }
                routeMap = new RouteMap(database);
                break;
            case "airport":
                if (routeMap == null) {
                    startElementHelp();
                }

                AirportElement airportElement = new AirportElement(atts);
                try {
                    routeMap.addAirport(airportElement.grabName());
                } catch (AirFailure e) {
                    throw new SAXException(e);
                }

                break;
            case "flight":
                if (routeMap == null) {
                    throw new SAXException("The route map does not exist. We cannot add flights without it.");
                }

                FlightElement flightElement = new FlightElement(atts);
                Airport origin = routeMap.obtainAirport(flightElement.takeOrigin());
                Airport destination = routeMap.obtainAirport(flightElement.grabDst());

                if (origin == null || destination == null) {
                    startElementAdviser();
                }

                routeMap.addFlight(origin, destination, flightElement.obtainCost(), flightElement.takeDistance(),
                        flightElement.fetchTime(), flightElement.pullCrew(), flightElement.grabWeight(),
                        flightElement.fetchPassengers());
        }

    }

    private void startElementAdviser() throws SAXException {
        throw new SAXException("Origin airport and destination airport need to be created before" +
                " a flight can be created");
    }

    private void startElementHelp() throws SAXException {
        throw new SAXException("The route map does not exist. We cannot add airports without it.");
    }

    private void startElementEntity() throws SAXException {
        throw new SAXException("We cannot handle nested route maps");
    }

    public RouteMap takeRouteMap(){
        return routeMap;
    }


    private static class AirportElement {
        private String name;

        AirportElement(Attributes atts) {
            name = atts.getValue("name");
        }

        public String grabName() {
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

        public String grabDst() {
            return dst;
        }

        public String takeOrigin() {
            return origin;
        }

        public Integer obtainCost() {
            return cost;
        }

        public Integer takeDistance() {
            return distance;
        }

        public Integer fetchTime() {
            return time;
        }

        public Integer pullCrew() {
            return crew;
        }

        public Integer grabWeight() {
            return weight;
        }

        public Integer fetchPassengers() {
            return passengers;
        }
    }
}

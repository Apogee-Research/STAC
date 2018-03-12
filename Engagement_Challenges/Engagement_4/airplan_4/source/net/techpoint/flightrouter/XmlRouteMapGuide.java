package net.techpoint.flightrouter;

import net.techpoint.flightrouter.prototype.Airport;
import net.techpoint.flightrouter.prototype.RouteMap;
import net.techpoint.flightrouter.keep.AirDatabase;
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
                    throw new SAXException("We cannot handle nested route maps");
                }
                routeMap = new RouteMap(database);
                break;
            case "airport":
                if (routeMap == null) {
                    new XmlRouteMapGuideEntity().invoke();
                }

                AirportElement airportElement = new AirportElement(atts);
                try {
                    routeMap.addAirport(airportElement.obtainName());
                } catch (AirFailure e) {
                    throw new SAXException(e);
                }

                break;
            case "flight":
                if (routeMap == null) {
                    startElementAssist();
                }

                FlightElement flightElement = new FlightElement(atts);
                Airport origin = routeMap.getAirport(flightElement.fetchOrigin());
                Airport destination = routeMap.getAirport(flightElement.takeDst());

                if (origin == null || destination == null) {
                    new XmlRouteMapGuideAdviser().invoke();
                }

                routeMap.addFlight(origin, destination, flightElement.pullCost(), flightElement.grabDistance(),
                        flightElement.obtainTime(), flightElement.pullCrew(), flightElement.fetchWeight(),
                        flightElement.getPassengers());
        }

    }

    private void startElementAssist() throws SAXException {
        new XmlRouteMapGuideUtility().invoke();
    }

    public RouteMap takeRouteMap(){
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

        public String takeDst() {
            return dst;
        }

        public String fetchOrigin() {
            return origin;
        }

        public Integer pullCost() {
            return cost;
        }

        public Integer grabDistance() {
            return distance;
        }

        public Integer obtainTime() {
            return time;
        }

        public Integer pullCrew() {
            return crew;
        }

        public Integer fetchWeight() {
            return weight;
        }

        public Integer getPassengers() {
            return passengers;
        }
    }

    private class XmlRouteMapGuideEntity {
        public void invoke() throws SAXException {
            throw new SAXException("The route map does not exist. We cannot add airports without it.");
        }
    }

    private class XmlRouteMapGuideUtility {
        public void invoke() throws SAXException {
            throw new SAXException("The route map does not exist. We cannot add flights without it.");
        }
    }

    private class XmlRouteMapGuideAdviser {
        public void invoke() throws SAXException {
            throw new SAXException("Origin airport and destination airport need to be created before" +
                    " a flight can be created");
        }
    }
}

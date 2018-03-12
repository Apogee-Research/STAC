package com.networkapex.airplan;

import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
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
public class XmlRouteMapManager extends DefaultHandler {
    private RouteMap routeMap;
    private AirDatabase database;

    public XmlRouteMapManager(AirDatabase database) {
        this.database = database;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        switch(localName) {
            case "route":
                if (routeMap != null) {
                    startElementHome();
                }
                routeMap = new RouteMap(database);
                break;
            case "airport":
                if (routeMap == null) {
                    throw new SAXException("The route map does not exist. We cannot add airports without it.");
                }

                AirportElement airportElement = new AirportElement(atts);
                try {
                    routeMap.addAirport(airportElement.fetchName());
                } catch (AirRaiser e) {
                    throw new SAXException(e);
                }

                break;
            case "flight":
                if (routeMap == null) {
                    throw new SAXException("The route map does not exist. We cannot add flights without it.");
                }

                FlightElement flightElement = new FlightElement(atts);
                Airport origin = routeMap.fetchAirport(flightElement.getOrigin());
                Airport destination = routeMap.fetchAirport(flightElement.obtainDst());

                if (origin == null || destination == null) {
                    startElementHerder();
                }

                routeMap.addFlight(origin, destination, flightElement.obtainCost(), flightElement.takeDistance(),
                        flightElement.takeTime(), flightElement.obtainCrew(), flightElement.pullWeight(),
                        flightElement.pullPassengers());
        }

    }

    private void startElementHerder() throws SAXException {
        throw new SAXException("Origin airport and destination airport need to be created before" +
                " a flight can be created");
    }

    private void startElementHome() throws SAXException {
        throw new SAXException("We cannot handle nested route maps");
    }

    public RouteMap getRouteMap(){
        return routeMap;
    }


    private static class AirportElement {
        private String name;

        AirportElement(Attributes atts) {
            name = atts.getValue("name");
        }

        public String fetchName() {
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

        public Integer takeDistance() {
            return distance;
        }

        public Integer takeTime() {
            return time;
        }

        public Integer obtainCrew() {
            return crew;
        }

        public Integer pullWeight() {
            return weight;
        }

        public Integer pullPassengers() {
            return passengers;
        }
    }
}

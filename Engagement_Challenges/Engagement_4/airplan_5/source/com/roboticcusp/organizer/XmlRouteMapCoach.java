
package com.roboticcusp.organizer;

import com.roboticcusp.organizer.framework.Airport;
import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.xmlpull.v1.XmlPullGrabber;
import com.roboticcusp.xmlpull.v1.XmlPullGrabberException;


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
public class XmlRouteMapCoach {
    private RouteMap routeMap;
    private AirDatabase database;

    public XmlRouteMapCoach(AirDatabase database) {
        this.database = database;
    }


    public void startElement(String localName, XmlPullGrabber grabber) throws XmlPullGrabberException, AirException {

        switch(localName) {
            case "route":
                if (routeMap != null) {
                    throw new XmlPullGrabberException("We cannot handle nested route maps");
                }
                routeMap = new RouteMap(database);
                break;
            case "airport":
                if (routeMap == null) {
                    startElementSupervisor();
                }

                AirportElement airportElement = new AirportElement(grabber);
                routeMap.addAirport(airportElement.grabName());
                break;
            case "flight":
                if (routeMap == null) {
                    startElementGuide();
                }

                FlightElement flightElement = new FlightElement(grabber);
                Airport origin = routeMap.obtainAirport(flightElement.fetchOrigin());
                Airport destination = routeMap.obtainAirport(flightElement.pullDst());

                if (origin == null || destination == null) {
                    throw new XmlPullGrabberException("Origin airport and destination airport need to be created before" +
                            " a flight can be created");
                }

                routeMap.addFlight(origin, destination, flightElement.getCost(), flightElement.getDistance(),
                        flightElement.takeTime(), flightElement.fetchCrew(), flightElement.grabWeight(), flightElement.fetchPassengers());
        }

    }

    private void startElementGuide() throws XmlPullGrabberException {
        throw new XmlPullGrabberException("The route map does not exist. We cannot add flights without it.");
    }

    private void startElementSupervisor() throws XmlPullGrabberException {
        throw new XmlPullGrabberException("The route map does not exist. We cannot add flights without it.");
    }

    public RouteMap pullRouteMap(){
        return routeMap;
    }


    private static class AirportElement {
        private String name;

        AirportElement(XmlPullGrabber grabber) {
            name = grabber.grabAttributeValue(null, "name");
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

        FlightElement(XmlPullGrabber grabber) {
            origin = grabber.grabAttributeValue(null, "origin");
            dst = grabber.grabAttributeValue(null, "dst");
            cost = Integer.parseInt(grabber.grabAttributeValue(null, "cost"));
            distance = Integer.parseInt(grabber.grabAttributeValue(null, "distance"));
            time = Integer.parseInt(grabber.grabAttributeValue(null, "time"));
            crew = Integer.parseInt(grabber.grabAttributeValue(null, "crew"));
            weight = Integer.parseInt(grabber.grabAttributeValue(null, "weight"));
            passengers = Integer.parseInt(grabber.grabAttributeValue(null, "passengers"));
        }

        public String pullDst() {
            return dst;
        }

        public String fetchOrigin() {
            return origin;
        }

        public Integer getCost() {
            return cost;
        }

        public Integer getDistance() {
            return distance;
        }

        public Integer takeTime() {
            return time;
        }

        public Integer fetchCrew() {
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

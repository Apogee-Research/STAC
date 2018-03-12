#!/usr/bin/env python

import argparse
from lxml import html
import requests

def get_session():
    session = requests.Session()
    session.verify = False # Don't verify the server's SSL cert

    # Turn off logging due to not verifying SSL cert; not generally a good idea but okay for this app
    #requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecureRequestWarning)
    #requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecurePlatformWarning)

    return session

class AirPlanClient:

    def __init__(self, host, port, username, password):
        self.host = host
        self.port = port
        self.username = username
        self.password = password

    def get_root_url(self):
        """
        :return: root page URL (https://hostname:port/)
        """
        return "https://{}:{}/".format(self.host, self.port)

    def login(self, session):
        """
        Logs airline in to AirPlan
        :param session:
        :return:
        """
        url = self.get_root_url() + 'login'
        session.post(url, files={"username":self.username, "password":self.password})

    def uploadRouteMap(self, session, route_map_location, route_map_name):
        """
        Uploads the route map found at route_map_location to create a route map with the name route_map_name
        :param session:
        :param route_map_location:
        :param route_map_name:
        :return:
        """
        url = self.get_root_url() + "add_route_map"

        mimetype = 'text/plain' # default is text

        try:
            ext = route_map_location.split(".")[-1]
            if ext=="xml":
                mimetype = 'text/xml'
            elif ext=="json":
                mimetype = 'application/json'
        except:
            pass # assume text

        with open(route_map_location, 'rb') as graph_file:
            response = session.post(url,
                files={'file' : (route_map_name, graph_file,mimetype), 'route_map_name':route_map_name}, allow_redirects=True)
        self.propertiesURL = response.url

        # response should be a redirect to the Passenger Capacity Matrix page
        if len(response.history) != 1 or not response.history[0].is_redirect:
            #print to stdout first, so our gradle tests can see the output
           print "The response was not a redirect\n " + str(response.text)
           raise AssertionError("The response was not a redirect.")

        return response

    def getShortestPathURL(self, propertiesURL):
        return propertiesURL.replace('map_properties', 'shortest_path')

    def shortestPath(self, session, source, destination, weight_type):
        shortestPathURL= self.getShortestPathURL(self.propertiesURL)
        sourceID = self.getAirportID(session, shortestPathURL, source, 'origin')
        destinationID = self.getAirportID(session, shortestPathURL, destination, 'destination')
        r = session.post(shortestPathURL, files={'origin':sourceID, 'destination':destinationID, 'weight-type': weight_type}, allow_redirects=True, verify=False)
        return r

    def getCrewManagementURL(self, propertiesURL):
        return propertiesURL.replace('map_properties', 'crew_management')
        
    def numCrewsNeeded(self, session):
        url = self.getCrewManagementURL(self.propertiesURL)
        r = session.get(url, allow_redirects=True, verify=False)
        return r


    def getCapacityMatrixURL(self, propertiesURL):
        return propertiesURL.replace('map_properties' , 'passenger_capacity_matrix')

    def passengerCapacity(self, session, propertiesURL):
        url = self.getCapacityMatrixURL(propertiesURL)
        r = session.get(url, verify=False)
        return r
    
    def getPropertiesURL(self, propertiesURL):
        return propertiesURL


    def properties(self, session, weight_type):
        r = session.post(self.propertiesURL, files={'weight-type':weight_type})
        return r

    def getAirportID(self, session, shortestPathURL, airportName, selectFieldName):
        page = session.get(shortestPathURL, verify=False)
        tree = html.fromstring(page.content)
        xpath = "//form/ul/li/select[@name='{}']/option[text()='{}']/@value".format(selectFieldName, airportName)
        return tree.xpath(xpath)[0]

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="the server ip address")
    parser.add_argument("serverport", help="the server port")
    parser.add_argument("username", help="a username to use to login")
    parser.add_argument("password", help="the password for the username")
    subparsers = parser.add_subparsers(dest="command")
    uploadparser = subparsers.add_parser('upload', help='Upload one or more graphs')
    uploadparser.add_argument("-file", dest="route_map_file", action='append', help="file containing route map to upload")

    matrixparser = subparsers.add_parser('matrix', help='View passenger capacity matrix')
    matrixparser.add_argument("-file", dest="route_map_file", action='append', help='file containing route map to upload and view passenger capacities')

    shortestparser = subparsers.add_parser("shortestpath",help="compute shortest path between vertices")
    shortestparser.add_argument("route_map", help='route map id')
    shortestparser.add_argument("src", help='start node for path')
    shortestparser.add_argument("dest", help='end node for path')
    shortestparser.add_argument("weight", help='which notion of weight to use for shortest path evaluation')
    args = parser.parse_args()

    airplan = AirPlanClient(args.serverip, args.serverport, args.username, args.password)

    with get_session() as session:
        airplan.login(session)
        if args.command=='upload':
            for route_map in args.route_map_file:
                print "uploading route map " + route_map
                response = airplan.uploadRouteMap(session, route_map, route_map.split('\\')[-1])
                print response.text

        if args.command=='shortestpath':
            response = airplan.shortestPath(session, args.route_map, args.src, args.dest, args.weight)
            print response.text

        if args.command=='matrix':
            for route_map in args.route_map_file:
                print "uploading route map " + route_map
                response = airplan.uploadRouteMap(session, route_map, route_map.split('\\')[-1])
                response = airplan.properties(session, "Cost") # user has to click through properties before viewing graph matrix
                response = airplan.passengerCapacity(session, airplan.propertiesURL)
                print response.text

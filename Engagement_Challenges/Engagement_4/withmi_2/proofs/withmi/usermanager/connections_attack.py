import argparse
import processcommspackets
import numpy

def get_timing_diffs(pcap_name, ip, port):
    pcap = processcommspackets.read_pcap(pcap_name)
    conversations = processcommspackets.get_conversations(pcap, ip, port)

    for id, conversation in conversations.items():
        messages = conversation.get_messages()

        # get the last server setup message
        # it should always be in the same place
        count = 5
        server_setup_msg = messages[count]
        if server_setup_msg.is_client():
            raise Exception("Expecting server message, got client message")


        # get last client message
        # it may be the last message or the send to last message
        last = len(messages) - 1
        last_client_msg = messages[last]
        if not last_client_msg.is_client():
            last_client_msg = messages[last - 1]

        if not last_client_msg.is_client():
            raise Exception("Could not find last client message")

        timingpair = processcommspackets.TimingPair(server_setup_msg, last_client_msg)

        return timingpair


def main():

    parser = argparse.ArgumentParser()
    parser.add_argument("victimip", help="the victim's ip address")
    parser.add_argument("victimport", type=int, help="the victim's port")

    parser.add_argument("testport", type=int, help="port for the test pcap files")

    parser.add_argument("victimpcap1", help="pcap file from the victim's interaction with the first user")
    parser.add_argument("victimpcap2", help="pcap file from the victim's interaction with the second user")

    args = parser.parse_args()

    # get the timings from the unknown pcap
    unknown_deltas = []
    known_deltas = []
    for i in range(1,11):
        unknown_delta = get_timing_diffs("unknown{}.pcap".format(i), args.victimip, args.testport).get_delta()
        known_delta = get_timing_diffs("known{}.pcap".format(i), args.victimip, args.testport).get_delta()
        unknown_deltas.append(unknown_delta)
        known_deltas.append(known_delta)

    unknown_delta = numpy.mean(unknown_deltas)
    known_delta = numpy.mean(known_delta)


    if abs(known_delta - unknown_delta) < 0.005:
        print "Cannot determine whether two users know each other"
        print "The test times are too similar"
        return

    # get the timings from the victim's first conversation
    victim_delta_1 = get_timing_diffs(args.victimpcap1, args.victimip, args.victimport).get_delta()

    conclusion_1 = is_known(known_delta, unknown_delta, victim_delta_1)
    print "The victim knows the user they are connecting to " + str(conclusion_1)

    # get the timings from the victim's second conversation
    victim_delta_2 = get_timing_diffs(args.victimpcap2, args.victimip, args.victimport).get_delta()

    conclusion_2 = is_known(known_delta, unknown_delta, victim_delta_2)
    print "The victim knows the user they are connecting to " + str(conclusion_2)


def is_known(known_timing, unknown_timing, victim_timing):

    # if the victim timing is less than the known timing, the victim knows the person
    # they are connecting to
    if victim_timing < known_timing:
        return True

    # if the victim timing is greater than the unknown timing, the victim doesn't know the person they
    # are connecting to
    if unknown_timing < victim_timing:
        return False

    # if the victim timing is between the known timing and the unknown timing,
    # pick the timing that is closer
    diff_known =victim_timing - known_timing
    diff_unknown = unknown_timing - victim_timing
    if diff_unknown > diff_known:
        return True
    else:
        return False




if __name__ == "__main__":
    main()

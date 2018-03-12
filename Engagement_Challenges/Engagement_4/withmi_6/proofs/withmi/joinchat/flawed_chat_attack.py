import argparse
import processcommspackets
from scapy.all import *


# this script is to demonstrate that merely looking at ports and ips sending messages to one another is
# insufficient establish how many users are in a chat
def main():
    parser = argparse.ArgumentParser(description="used to attack withmi")
    parser.add_argument("host", help="our host")
    parser.add_argument("port", type=int, help="our port")
    # pcaps are intended to be chat pcap with chats of different sizes that look the same
    parser.add_argument("pcap1", help="pcap file of observed chat session")
    parser.add_argument("pcap2", help="pcap file of observed chat session to compare")

    args = parser.parse_args()

    pcap1 = processcommspackets.read_pcap(args.pcap1)
    pcap2 =  processcommspackets.read_pcap(args.pcap2)

    # run the actual attack
    count_users(pcap1)
    count_users(pcap2)


def count_users(pcap):
    dests = set()
    sources = set()
    for packet in pcap:
        sources.add((packet[IP].src, packet[TCP].sport)) # a user is represented by a host/port pair
        dests.add((packet[IP].dst, packet[TCP].dport))
    print "Saw messages from " + str(len(sources)) + " users and to " + str(len(dests)) + " users."
    return (len(sources), len(dests))


if __name__ == "__main__":
    main()


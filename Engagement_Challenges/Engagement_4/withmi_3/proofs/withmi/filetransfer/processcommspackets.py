import sys
import struct
# this works around a bug in scapy that sets the MTU size too low
# since we sometimes capture on localhost our MTU can be 2^16
import scapy.data
scapy.data.MTU = 0xffffff
from scapy.all import *

# the number of messages used to setup any conversation
# Currently, four client setup messages and three server setup messages
NUM_SETUP_MESSAGES = 7

class RepairPcapReader(PcapReader):
    def __init__(self, filename, debug=False):
        PcapReader.__init__(self, filename)
        self.debug = debug
    def read_packet(self, size=MTU):
        rp = RawPcapReader.read_packet(self,size)
        if rp is None:
            return None
        s,(sec,usec,wirelen) = rp
        if len(s) != wirelen:
            if self.debug:
                print "captured length {} != wire length {}, padding...".format(len(s), wirelen)
            s = s + "0" * (wirelen - len(s))
        try:
            p = self.LLcls(s)
        except KeyboardInterrupt:
            raise
        except:
            if conf.debug_dissector:
                raise
            p = conf.raw_layer(s)
        p.time = sec+0.000001*usec
        return p

def read_pcap(pcapfile, debug=False):
    reader = RepairPcapReader(pcapfile, debug)
    return reader.read_all()

def get_conversation_id(packet, serverAddr, serverPort):
    '''
    Conversation id is the clientAddr:clientPort -> serverAddr:serverPort
    '''
    clientAddr = ""
    clientPort = 0
    try:
        if packet[IP].dst == serverAddr and packet[TCP].dport == serverPort:
            clientAddr = packet[IP].src
            clientPort = packet[TCP].sport
        elif packet[IP].src == serverAddr and packet[TCP].sport == serverPort:
            clientAddr = packet[IP].dst
            clientPort = packet[TCP].dport
        else:
            return None
        return "{}:{} -> {}:{}".format(clientAddr, clientPort, serverAddr, serverPort)
    except Exception as e:
        print e
        return "NO_ID"

class Message(object):
    '''
    The data contained associated with a single message from the comms framework.
    '''
    def __init__(self, isClient, startTime):
        '''

        :param isClient true if this is a packet from the client
        :param startTime the time the message was sent
        :return:
        '''
        self.isClient = isClient
        self.data = []
        self.startTime = startTime

    def is_client(self):
        return self.isClient

    def add_data(self, data):
        self.data.extend(data)

    def get_size(self):
        return len(self.data)

    def get_start(self):
        return self.startTime

    def get_data_str(self):
        # returns the payload for this message
        return "".join(self.data)

    def __repr__(self):
        theType = "Client"
        if not self.is_client():
            theType = "Server"
        return "{} start: {} size: {}".format(theType, self.get_start(), self.get_size())

class Conversation(object):
    '''
    A conversation is defined as a set of exchanges between a client and a server
    using the same set of ports (which naively maps to a tcp connection)
    '''
    def __init__(self, convId, serverAddr, serverPort):
        self.convId = convId
        self.packets = []
        self.serverAddr = serverAddr
        self.serverPort = serverPort

    def _is_client_packet(self, packet):
        '''
        :param serverPort: the port the client sends requests to
        :return: true if this is a packet from the client
        '''
        return packet[IP].dst == self.serverAddr and packet.dport == self.serverPort

    def _is_server_packet(self, packet):
        '''
        :param packet:
        :return: true if this is a packet from the server
        '''
        return packet[IP].src == self.serverAddr and packet.sport == self.serverPort

    def add_packet(self, packet):
	try:	
        	# ignore some packets
        	if len(packet[TCP].payload) == 0:
            		return
	except:
		print "packet didn't have tcp, oh well"
		return
        # make sure it's the right conversation
        convId = get_conversation_id(packet, self.serverAddr, self.serverPort)
        if not convId:
            return

        # add it
        self.packets.append(packet)

    def _get_messages_from_stream(self, packet_stream, isClient):
        curMessage = None
        expectedDataSize = 0
        messages = []
        dataBuffer = []

        sizeSize = struct.calcsize("!I")

        for packet in packet_stream:
            payload = str(packet[TCP].payload)
            dataBuffer.extend(payload)

            while len(dataBuffer) > (sizeSize - 1):
                if expectedDataSize > 0:
                    curSize = min(expectedDataSize, len(dataBuffer))
                    data = dataBuffer[:curSize]
                    expectedDataSize -= curSize
                    dataBuffer = dataBuffer[curSize:]
                    curMessage.add_data(data)
                else:
                    # we're staring a new message
                    if curMessage is not None:
                        messages.append(curMessage)
                    curMessage = Message(isClient, packet.time)
                    # expected data should be the first integer in the databuffer
                    expectedDataSize = struct.unpack("!I", "".join(dataBuffer[:sizeSize]))[0]
                    dataBuffer = dataBuffer[sizeSize:]
                    # don't worry about processing the reset of this, we'll get it on the next round
	if curMessage is not None:
        	messages.append(curMessage)
        return messages

    def get_messages(self):
        # a message could start or end in the middle of a packet,
        # the most we know is that the first bit of data in the first packet
        # will be the length of the first message.
        # Packets will be interleved between the server and client
        # A packet might contain both the end of the last message and the
        # start of the next message.

        # first, separate client and server packets
        # We could have done this in add_packet(), but we might want that
        # order at some other time.
        serverPackets = []
        clientPackets = []
        for packet in self.packets:
            if self._is_client_packet(packet):
                clientPackets.append(packet)
            elif self._is_server_packet(packet):
                serverPackets.append(packet)
            else:
                raise ValueError("packet is neither server nor client, shouldn't be here")

        # now we have all the messages, and they should be in order
        clientMessages = self._get_messages_from_stream(clientPackets, True)
        serverMessages = self._get_messages_from_stream(serverPackets, False)

        # now combine them all in to one message stream
        messages = []
        serverIndex = 0
        clientIndex = 0
        while serverIndex < len(serverMessages) and clientIndex < len(clientMessages):
            curClient = clientMessages[clientIndex]
            curServer = serverMessages[serverIndex]
            if curClient.get_start() < curServer.get_start():
                messages.append(curClient)
                clientIndex += 1
            else:
                messages.append(curServer)
                serverIndex += 1
        # get all the leftover client messages
        messages.extend(clientMessages[clientIndex:])
        # get all the leftover server messages
        messages.extend(serverMessages[serverIndex:])
        return messages

    def get_messages_without_setup(self):
        '''
        :return: all the messages but omitting any crypto setup
        '''
        messages = self.get_messages()
        return messages[NUM_SETUP_MESSAGES:]

    def get_setup_messages(self):
        messages = self.get_messages()
        return messages[:NUM_SETUP_MESSAGES]

    def get_message_slice(self, start_message, end_message):
        messages = self.get_messages()
        return messages[start_message:end_message]

    def __repr__(self):
        return "\n".join(str(message) for message in self.get_messages())

class TimingPair(object):
    '''
    Represents a pair of packets (start_packet, end_packet) and the timing difference between them
    '''
    def __init__(self, start_msg, end_msg):
        self.start_msg = start_msg
        self.end_msg = end_msg
        self.delta = end_msg.get_start() - start_msg.get_start()

    def get_delta(self): return self.delta

    def get_start_msg(self): return self.start_msg

    def get_end_msg(self): return self.end_msg

    def is_client_to_server(self):
        '''
        :return: True if the start message came from the 'client'
        '''
        return self.start_msg.is_client()

    def __repr__(self):
        startType = 'C'
        endType = 'S'
        if not self.is_client_to_server():
            startType = 'S'
            endType = 'C'
        return "delta: {}\tstart_msg_size({}): {}\tend_msg_size({}):{}".format(
            self.delta, startType, self.start_msg.get_size(), endType, self.end_msg.get_size())

def get_conversations(pcap, serverAddr, serverPort):
    conversations = {}
    for packet in pcap:
        convId = get_conversation_id(packet, serverAddr, serverPort)
        if convId is None:
            continue
        if not conversations.has_key(convId):
            conversation = Conversation(convId, serverAddr, serverPort)
            conversations[convId] = conversation
        conversation = conversations[convId]
        conversation.add_packet(packet)
    return conversations

def get_time_deltas_for_messages(messages):
    '''
    :return: the TimeDeltas for the conversation
    '''
    lastClient = None
    lastServer = None
    deltas = []
    for message in messages:
        if message.is_client():
            if lastServer:
                deltas.append(TimingPair(lastServer, message))
            lastClient = message
        else:
            if lastClient:
                deltas.append(TimingPair(lastClient, message))
            lastServer = message
    return deltas

    '''
    Allow computation of deltas based on specific request and response sizes, to
    handle the case when there are multiple (possibly unrelated) messages going
    back and forth.
    TODO: this is commented out because it's not currently needed and might not have been 
    thoroughly tested, but I'm leaving it here as there's a good chance we'll want to use 
    it in the future -RAB'''
    '''def get_specific_time_deltas(messages, req_min_sz, req_max_sz, resp_min_sz, resp_max_sz):
    
    find time delta between request of size between req_min_sz and req_max_size
    and response of size between resp_min_size and resp_max_size
    
    deltas = []
    for msg in messages:
        if msg.size()>req_min_sz and msg.size()<req_max_sz:
            if _is_client(msg):
                last_client_req = msg
            else:
                last_server_req = msg
        if msg.size()>req_min_sz and msg.size()<req_max_sz:
            if _is_client(msg):
                if (last_server_req):
                    
            else:
                last_server_req = msg'''

def main(pcapfile, serverAddr, serverPort):
    '''
    Provides time and size information for all messages in the conversation

    Example:

$ python processcommspackets.py chat.pcap 127.0.0.1 9001
WARNING: No route found for IPv6 destination :: (no default route?)
127.0.0.1:58387 -> 127.0.0.1:9001:
Client start: 1456337086.55 size: 287
Server start: 1456337086.82 size: 568
Client start: 1456337088.97 size: 42
Server start: 1456337114.21 size: 75
Server start: 1456337118.45 size: 57
Server start: 1456337120.55 size: 52
Client start: 1456337124.4 size: 41
Client start: 1456337126.32 size: 36
Setup messages:
delta: 0.267399072647	start_msg_size(C): 287	end_msg_size(S):568
Non setup messages:
delta: 25.2408349514	start_msg_size(C): 42	end_msg_size(S):75
delta: 29.4726758003	start_msg_size(C): 42	end_msg_size(S):57
delta: 31.5744547844	start_msg_size(C): 42	end_msg_size(S):52
delta: 3.85007309914	start_msg_size(S): 52	end_msg_size(C):41
delta: 5.76787805557	start_msg_size(S): 52	end_msg_size(C):36
All:
delta: 0.267399072647	start_msg_size(C): 287	end_msg_size(S):568
delta: 2.15653514862	start_msg_size(S): 568	end_msg_size(C):42
delta: 25.2408349514	start_msg_size(C): 42	end_msg_size(S):75
delta: 29.4726758003	start_msg_size(C): 42	end_msg_size(S):57
delta: 31.5744547844	start_msg_size(C): 42	end_msg_size(S):52
delta: 3.85007309914	start_msg_size(S): 52	end_msg_size(C):41
delta: 5.76787805557	start_msg_size(S): 52	end_msg_size(C):36
    '''
    pcap = read_pcap(pcapfile)
    conversations = get_conversations(pcap, serverAddr, serverPort)
    for id, conversation in conversations.items():
        print "{}:\n{}".format(id, str(conversation))
        print "Setup messages:"
        deltas = get_time_deltas_for_messages(conversation.get_setup_messages())
        print "\n".join([str(delta) for delta in deltas])
        print "Non setup messages:"
        deltas = get_time_deltas_for_messages(conversation.get_messages_without_setup())
        print "\n".join([str(delta) for delta in deltas])
        print "All:"
        deltas = get_time_deltas_for_messages(conversation.get_messages())
        print "\n".join([str(delta) for delta in deltas])

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print "processcommspackets.py <pcap filename> <server addr> <server port>"
        sys.exit(-1)
    main(sys.argv[1], sys.argv[2], int(sys.argv[3]))

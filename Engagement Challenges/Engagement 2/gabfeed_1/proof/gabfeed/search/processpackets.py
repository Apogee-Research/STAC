import sys
import struct
# this works around a bug in scapy that sets the MTU size too low
# since we sometimes capture on localhost our MTU can be 2^16
import scapy.data
scapy.data.MTU = 0xffffff
from scapy.all import *
from itertools import izip_longest
from operator import itemgetter, attrgetter, methodcaller

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
    if packet[IP].dst == serverAddr and packet[TCP].dport == serverPort:
        clientAddr = packet[IP].src
        clientPort = packet[TCP].sport
    elif packet[IP].src == serverAddr and packet[TCP].sport == serverPort:
        clientAddr = packet[IP].dst
        clientPort = packet[TCP].dport
    else:
        return None
    return "{}:{} -> {}:{}".format(clientAddr, clientPort, serverAddr, serverPort)

def grouper(n, iterable, fillvalue=None):
    "grouper(3, 'ABCDEFG', 'x') --> ABC DEF Gxx"
    args = [iter(iterable)] * n
    return izip_longest(fillvalue=fillvalue, *args)

class Message(object):
    '''
    The set of contiguous packets that equate to a message.
    It's initialized from the first packet of the message
    '''
    def __init__(self, isRequest):
        '''

        :param isRequest: True if this is a request
        :return:
        '''
        self.isRequest = isRequest
        self.packets = []

    def is_request(self):
        return self.isRequest

    def add_packet(self, packet):
        self.packets.append(packet)

    def get_size(self):
        return len(self.get_application_data())

    def get_start(self):
        # the time of the first packet
        return self.packets[0].time

    def get_payload_str(self):
        # returns the payload for this message
        return "".join(str(packet[TCP].payload) for packet in self.packets)

    def get_application_data_segments(self):
        '''
        a list of the application data segments each as a separate string
        '''

        # we assume this is ssl and we'll iterate through it with that in mind
        payload = self.get_payload_str()
        SSL_HEADER = "!BHH"
        headerSize = struct.calcsize(SSL_HEADER)
        if len(payload) < headerSize:
            return
        applicationData = []
        while len(payload) > 0:
            contentType, version, length = struct.unpack(SSL_HEADER, payload[:headerSize])
            if version != 0x0301:
                print "got lost in application data, likely corrupted, falling back to regular payload"
                return [self.get_payload_str()]
            # strip the header off
            payload = payload[headerSize:]
            if contentType == 0x17:
                # application data, keep this
                applicationData.append(payload[:length])
            # get rid of the data...
            payload = payload[length:]
        return applicationData

    def get_application_data(self):
        '''
        Returns one string with all of the application data segments squashed together
        '''
        return "".join(self.get_application_data_segments())

    def __repr__(self):
        theType = "Req"
        if not self.is_request():
            theType = "Rsp"
        segments = self.get_application_data_segments()
        data = "".join(segments)
        return "{} start: {} segs: {} size: {}".format(theType, self.get_start(), len(segments), len(data))

class Transaction(object):
    '''
    Contains one request and one response pair
    '''
    def __init__(self, req, resp, convId):
        assert req
        assert resp
        self.req = req
        self.resp = resp
        self.convId = convId
    def get_start(self):
        return self.req.get_start()
    def get_duration(self):
        return self.resp.get_start() - self.req.get_start()
    def get_req_size(self):
        return self.req.get_size()
    def get_resp_size(self):
        return self.resp.get_size();
    def __repr__(self):
        return "{}\n\tReq: {} Resp: {} Duration: {}".format(self.convId, self.get_req_size(),
                                                            self.get_resp_size(), self.get_duration())

class Conversation(object):
    '''
    A conversation is defined as a set of exchanges between a client and a server
    using the same set of ports (which naively maps to a tcp connection)
    '''
    def __init__(self, convId, serverAddr, serverPort):
        self.convId = convId
        self.packets = []
        self.messages = []
        self.serverAddr = serverAddr
        self.serverPort = serverPort

    def _is_request(self, packet):
        '''
        :param serverPort: the port the client sends requests to
        :return: true if this is a request to that port
        '''
        return packet[IP].dst == self.serverAddr and packet.dport == self.serverPort

    def _is_response(self, packet):
        return packet[IP].src == self.serverAddr and packet.sport == self.serverPort

    def add_packet(self, packet):
        # ignore some packets
        if len(packet[TCP].payload) == 0:
            return

        # make sure it's the right conversation
        convId = get_conversation_id(packet, self.serverAddr, self.serverPort)
        if not convId:
            return
        assert self.convId == convId
        isRequest = self._is_request(packet)

        if len(self.packets) == 0:
            # this is the first packet for this conversation
            # all conversations must start as requests
            if not isRequest:
                print convId
            assert isRequest
            self.packets.append(packet)
            message = Message(isRequest)
            message.add_packet(packet)
            self.messages.append(message)
            return

        curMessage = lastMessage = self.messages[-1]

        # is this different?
        if lastMessage.is_request() != isRequest:
            # This packet signals the start of a new message
            curMessage = Message(isRequest)
            self.messages.append(curMessage)
        curMessage.add_packet(packet)

    def get_transactions(self):
        '''
        Returns the transactions made, only includes messages with application data
        '''
        transactions = []
        for req, resp in grouper(2, self.messages):
            if req.get_size() > 0 or (resp and resp.get_size() > 0):
                if req and resp:
                    transactions.append(Transaction(req, resp, self.convId))
        return transactions

    def __repr__(self):
        return "\n".join(str(message) for message in self.messages)

def get_conversations(pcap, serverAddr, serverPort):
    conversations = {}
    #for key, packets in pcap.sessions().iteritems():
    for packet in pcap:
        convId = get_conversation_id(packet, serverAddr, serverPort)
        conversation = None
        if not conversations.has_key(convId):
            conversation = Conversation(convId, serverAddr, serverPort)
            conversations[convId] = conversation
        conversation = conversations[convId]
        conversation.add_packet(packet)
    return conversations

def get_transactions(pcap, serverAddr, serverPort):
    convs = get_conversations(pcap, serverAddr, serverPort)
    transactions = []
    for convId, conv in convs.iteritems():
        transactions.extend(conv.get_transactions())
    # sort them by start time
    return sorted(transactions, key=methodcaller('get_start'))

def main(pcapfile, serverAddr, serverPort):
    pcap = read_pcap(pcapfile)
    for transaction in get_transactions(pcap, serverAddr, serverPort):
        print transaction

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print "processpackets.py <pcap filename> <server addr> <server port>"
        sys.exit(-1)
    main(sys.argv[1], sys.argv[2], int(sys.argv[3]))
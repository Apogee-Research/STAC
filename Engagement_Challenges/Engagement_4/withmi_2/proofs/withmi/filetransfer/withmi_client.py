import argparse
import threading
import uuid
import comms_client
import chat_pb2
from Crypto.Util.number import long_to_bytes

class WithMiClient(object):
    """
    This class is made to interact with our WithMi host program.
    There are support files comms_client.py and comms_connection.py that help us
    interact with the java comms framework that Basic Chat uses.

    These support files and this class can be used to construct an attack against the java
    comms framework.
    """

    def __init__(self, name, wait_for_ack_timeout=0):
        self.client = comms_client.CommsClient(name)
        self.name = name
        self.next_msg_id = 0
        self.wait_for_ack_timeout = wait_for_ack_timeout
        self.wait_event = threading.Event()

    def connect(self, host, port, our_host=None, our_port=None):
        # send and receive the messages necessary to connect with the server
        # get the Connection object
        self.connection = self.client.connect(host, port, self.handle, our_host, our_port)
        self.our_host = our_host
        self.our_port = our_port

    def close(self):
        self.connection.close()

    def send_withmi_msg(self, msg):
        self.send_bytes(msg.SerializeToString())

    def send_bytes(self, bytes):
        self.wait_event.clear()
        self.connection.write(bytes)
        if self.wait_for_ack_timeout > 0:
            success = self.wait_event.wait(self.wait_for_ack_timeout)
            self.wait_event.clear()
            if not success:
                raise Exception("timed out waiting for ack")

    def create_msg(self, type, chatId):
        withMiMsg = chat_pb2.WithMiMsg()
        withMiMsg.type = type
        withMiMsg.user = self.name
        withMiMsg.messageId = self.get_next_msg_id()
        withMiMsg.chatId = chatId
        return withMiMsg

    def send_text(self, line, chatId):
        withMiMsg = self.create_msg(chat_pb2.WithMiMsg.CHAT, chatId)
        withMiMsg.textMsg.textMsg = line
        self.send_withmi_msg(withMiMsg)

    def send_chatstate(self, chatId, groupName):
        withMiMsg = self.create_msg(chat_pb2.WithMiMsg.CHAT_STATE, chatId)
        withMiMsg.textMsg.textMsg = groupName

        state_msg = withMiMsg.chatStateMsg

        identity = state_msg.publicId.add()
        identity.id = self.name
        public_key = identity.publicKey
        public_key.e = long_to_bytes(self.client.rsa.e)
        public_key.modulus = long_to_bytes(self.client.rsa.n)
        callback = identity.callbackAddress
        callback.host = self.our_host
        callback.port = self.our_port
        self.send_withmi_msg(withMiMsg)

    def get_next_msg_id(self):
        id = self.next_msg_id
        self.next_msg_id += 1
        return id

    # TODO we might want to handle CHAT_STATE messages
    def handle(self, connection, data):
        msg = chat_pb2.WithMiMsg()
        msg.ParseFromString(data)
        if msg.type == chat_pb2.WithMiMsg.CHAT:
            self.handle_chat(msg)
        elif msg.type == chat_pb2.WithMiMsg.FILE:
            self.handle_file(msg)
        elif msg.type == chat_pb2.WithMiMsg.READ_RECEIPT:
            self.handle_read_receipt(msg)
        elif msg.type == chat_pb2.WithMiMsg.CHAT_STATE:
            self.handle_chat_state(msg, data)
        else:
            print "Invalid msg type: " + msg.type

    def handle_chat_state(self, msg, data):
        print "* {}: chat state message, size: {}".format(msg.user, len(data))


    def handle_chat(self, msg):
        print "* {}: {}".format(msg.user, msg.textMsg.textMsg)

    def handle_file(self, msg):
        print "* {}: file: {} offset: {} total_size: {}".format(msg.user, msg.fileMsg.fileName,
                                                                msg.fileMsg.currentOffset, msg.fileMsg.totalSize)

    def handle_read_receipt(self, msg):
        print "* {}: read receipt for {}".format(msg.user, int(msg.receiptMsg.ackedMessageId))
        # NOTE: we're not waiting for any specific id here, so things could go wrong
        # However, if we're waiting, we're waiting for everything (so there should only be one outstanding anyways),
        # so it should be OK
        self.wait_event.set()

def main():
    parser = argparse.ArgumentParser(description="basic withmi client for comms framework.")
    parser.add_argument('host', help='host we want to connect to')
    parser.add_argument('port', help='port we want to connect to')
    parser.add_argument('name', help='user name')
    args = parser.parse_args()
    port = int(args.port)
    client = WithMiClient(args.name, 5.0)
    client.connect(args.host, port)

    uniqueId = str(uuid.uuid4())
    client.send_chatstate(uniqueId, "hi")
    client.send_text("hi bob", uniqueId)
    client.close()


if __name__ == '__main__':
    main()

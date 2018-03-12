import argparse
import comms_client
import powerbrokermsg_pb2
import time
import sys

class PowerBrokerClient(object):
    """
    This class interacts with our PowerBroker host program.
    There are support files comms_client.py and comms_connection.py that
    help us interaction with the java comms framework that PowerBroker users

    These support files and this class can be used to construct an attack
    against the java PowerBroker
    """
    def __init__(self, name):
        self.client = comms_client.CommsClient(name)
        self.name = name
        self.connection = None

    def connect(self, host, port, our_host, our_port):
        self.connection = self.client.connect(host, port, self.handle, our_host, our_port)
        print "Done setting up connection"


    def close(self):
        self.connection.close()

    def handle(self, connection, data):
        print "received data "


    def create_msg(self, type):
        msg = powerbrokermsg_pb2.BaseMessage()
        msg.type = type
        return msg

    def send_message(self, msg):
        self.send_bytes(msg.SerializeToString())

    def send_bytes(self, bytes):
        self.connection.write(bytes)



def main():
    parser = argparse.ArgumentParser(description='basic powerbroker client')
    parser.add_argument('host', help='host we want to connect to')
    parser.add_argument('port', help='port we want to connect to')
    parser.add_argument('ourPort', type=int, help='our port')
    parser.add_argument('--close', '-c', help='close connection after connecting')
    args = parser.parse_args()
    port = int(args.port)
    client = PowerBrokerClient("name")
    client.connect(args.host, port, 'localhost', args.ourPort)
    if args.close:
        msg = client.create_msg(powerbrokermsg_pb2.BaseMessage.CONNECTION_START)
        # we want to disconnect as soon as we connect
        client.send_message(msg)
        client.close()
        sys.exit()

    # send a connect message
    msg = client.create_msg(powerbrokermsg_pb2.BaseMessage.CONNECTION_START)
    client.send_message(msg)

    time.sleep(30)
    client.close()


if __name__ == '__main__':
    main()

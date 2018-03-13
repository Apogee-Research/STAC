import socket
import time
import sys


class Sender(object):
    def __init__(self, dirname, timeout=1):
        self.dirname = dirname
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.timeout = timeout
        remote_ip = socket.gethostbyname('localhost')
        self.sock.connect(('localhost', 8080))

    def _do_buffered_wait(self, timeout=None):
        try:
            recvd = len(self.sock.recv(1))
            if timeout is None:
                self.sock.settimeout(self.timeout)
            else:
                self.sock.settimeout(timeout)
            while len(self.sock.recv(1)) != 0:
                recvd += 1
        except socket.timeout:
            pass
        finally:
            self.sock.settimeout(None)
        return recvd

    def _send_packet(self, packet_name):
        with open(self.dirname + '/' + packet_name, 'rb') as packet:
            self.sock.send(packet.read())

    def send(self):
        print "Sending handshake Open: ",
        sys.stdout.flush()
        self._send_packet('firstPacket.bin')
        print "size = ", self._do_buffered_wait()
        
        print "Sending Message: ",
        sys.stdout.flush()
        self._send_packet('secondPacket.bin')
        print "size = ", self._do_buffered_wait()

        print "Receive ack: ",
        sys.stdout.flush()
        self._send_packet('thirdPacket.bin')
        print "size = ", self._do_buffered_wait()

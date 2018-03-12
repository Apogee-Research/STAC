import os
import psutil
import tempfile
import signal
import time
from shutil import copyfile
from scapy.all import *


class PacketCollector(object):
    '''
    Collects packets as specified
    '''
    def __init__(self, interface, filter):
        self.interface = interface
        self.filter = filter
        handle, filename = tempfile.mkstemp(suffix=".pcap", dir=".")
        # we don't want the file open, just the name
        f = os.fdopen(handle)
        f.close()
        self.filename = filename

    def start(self):
        self.devnull = open(os.devnull, 'w')
        self.tcpdumphandle = psutil.Popen(["/usr/sbin/tcpdump", "-i", self.interface,
                                          "-w", self.filename, self.filter],
                                          stdout=self.devnull,
                                          stderr=self.devnull)

        time.sleep(1)

    def stop(self):
        if self.tcpdumphandle:
            self.tcpdumphandle.send_signal(signal.SIGINT)
            self.tcpdumphandle.wait()
            self.tcpdumphandle = None
        self.devnull.close()

    def close(self):
        os.remove(self.filename)

    def save(self, dest):
        copyfile(self.filename, dest)
    
    def get_pcap(self):
        return rdpcap(self.filename)

    def __enter__(self):
        self.start()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.stop()
        self.close()

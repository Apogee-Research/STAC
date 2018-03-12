Some proofs require the use of the python scapy module.  There is an
incompatible change to that module between versions 2.3.2 and 2.3.3.
The proofs in this directory rely on the use of version 2.3.2.  If
version 2.3.3 is installed, a minor change can be made to allow
these tests to work.  The two affected files are processpackets.py
and processcommspackets.py and only require the following changes:

1. In the RepairPcapReader class, change the __init__ method from:
     def __init__(self, filename, debug=False):
         PcapReader.__init__(self, filename)
         self.debug = debug
   to:
     def __init__(self, filename, fdesc, magic):
         PcapReader.__init__(self, filename, fdesc, magic)
         self.debug = False

2. Next, add the method:
     def setDebug(self, debug):
         self.debug = debug

3. Finally, modify the read_pcap method from:
     def read_pcap(pcapfile, debug=False):
         reader = RepairPcapReader(pcapfile, debug)
         return reader.read_all()
   to:
     def read_pcap(pcapfile, debug=False):
         reader = RepairPcapReader(pcapfile)
         reader.setDebug(debug)
         return reader.read_all()

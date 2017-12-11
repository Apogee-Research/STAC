# MIT License
#
# Copyright (c) 2017 Apogee Research
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.


import socket
import time
import scapy.all as scapy


def sendTime(guess):
    HOST = '172.10.0.90'
    PORT = 8000
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((HOST, PORT))
    tosend = guess + '\n'
    a = time.time()
    s.sendall(tosend.encode())
    r1 = s.recv(1024).decode().strip()
    b = time.time()
    s.close()
    return [r1, b - a]


def testSend():
    print(sendTime("1233 3"))
    print(sendTime("1235 3"))


# testSend()

pcap_file = scapy.rdpcap("data.pcap")

# Parse pcap
mal_start = -1
mal_stop = -1
mal_started = False
benign_start = -1
benign_stop = -1
benign_started = False
for i in range(len(pcap_file.res)):
    ether_packet = pcap_file.res[i]
    ether_time = ether_packet.time
    try:
        # Read IP Layer
        dst_ip = ether_packet.payload["IP"].dst
        src_ip = ether_packet.payload["IP"].src
        if src_ip == "192.168.100.20" and dst_ip == "192.168.100.10" and mal_started:
            mal_stop = ether_time
        elif src_ip == "192.168.100.20" and dst_ip == "192.168.100.30" and benign_started:
            benign_stop = ether_time
        elif dst_ip == "192.168.100.20" and src_ip == "192.168.100.10" and not mal_started:
            mal_started = True
            mal_start = ether_time
        elif dst_ip == "192.168.100.20" and src_ip == "192.168.100.30" and not benign_started:
            benign_started = True
            benign_start = ether_time

            # tcp_payload = ether_packet.payload["TCP"].load.decode()[:-1]
    except:
        continue

delta_mal = mal_stop - mal_start
delta_benign = benign_stop - benign_start

if all(i != -1 for i in [mal_start, mal_stop, benign_start, benign_stop]):
    # return {"A_Time": delta_1, "B_Time": delta_2}
    print("Attacker:", delta_mal, "Benign:", delta_benign)

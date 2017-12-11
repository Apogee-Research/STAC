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

import scapy.all as scpy
import pickle
import numpy as np


def save_data(filename, data):
    """Store data to file."""
    with open(filename, "wb") as out_file:
        pickle.dump(data, out_file)


def load_data(filename, data_type):
    """Load data from file."""
    if data_type == "size_data":
        data = pickle.load(open(filename, "rb"))
        return data
    else:
        raise RuntimeError("Load Error")


def process_pcap(input_file):
    pcap_file = scpy.rdpcap(input_file)
    size_list = []
    for i in range(len(pcap_file.res)):
        # Read Ethernet Layer
        ethernet_packet = pcap_file.res[i]
        ethernet_time = ethernet_packet.time

        try:
            # Read IP Layer
            dst_ip = ethernet_packet.payload["IP"].dst
            if dst_ip != "192.168.100.20":
                continue
            # Read Transport Layer
            tcp_payload = ethernet_packet.payload["TCP"].load#.decode()
            size_list.append(len(tcp_payload))
        except:
            continue
    return size_list


def process_raw_data():
    size_data = []
    for pcap_file in ["size_data.pcap"]:
        size_list = process_pcap("Data/" + pcap_file)
        size_data.append(size_list)
    save_data("Data/size_data.p", size_data)


def process_stored_data():
    size_data = load_data("Data/size_data.p", "size_data")
    sizes = [sum(size_list) for size_list in size_data]
    print("Input Budget Stats\n\tMin Size:", min(sizes), " kB\n\tMax Size:", max(sizes), " kB \n\tMean Size:", np.mean(sizes), "kB")


process_raw_data()
process_stored_data()

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

import subprocess
import time
import pickle
import sys


def queryTour(tourList):
    """Send tour query and return edge times"""
    tourString = ""
    for cityIndex in range(len(tourList)):
        city = tourList[cityIndex]
        tourString += city.replace(" ", "%20")
        if cityIndex < len(tourList) - 1:
            tourString += "&point="
    # Start TCP Dump
    tcpOut = open("tcpOut.txt", "w")
    tcpErr = open("tcpErr.txt", "w")
    tcpdump = subprocess.Popen(["tcpdump", "-i", "lo", "port", "8989", "and", "greater", "150", "and", "less", "151"],
                               stdout=tcpOut, stderr=tcpErr)
    time.sleep(0.1)
    subprocess.call(["curl", "-k", "-s", "-o", ".rnd", "-i", "https://127.0.0.1:8989/tour?point=" + tourString])
    tcpdump.terminate()
    tcpOut.close()
    tcpErr.close()
    time.sleep(0.1)
    tcpOut = open("tcpOut.txt", "r")
    lines = tcpOut.readlines()
    tcpOut.close()
    times = []
    for i in range(len(lines)):
        if lines[i] != "\n":
            line = lines[i]
            strTime = line.split(" ")[0]
            hrs = float(strTime.split(":")[0])
            mins = float(strTime.split(":")[1])
            secs = float(strTime.split(":")[2])
            floatTime = hrs * 3600 + mins * 60 + secs
            times.append(floatTime)
    edgeTimes = [(times[i] - times[i - 1]) * 1000 for i in range(1, len(times), 1)]
    return edgeTimes


def collect_data():
    """Collect timing data on edges"""
    edge_list = pickle.load(open("Results/Order_of_Links.p", "rb"))
    samples = 1
    res_dict = {i: [] for i in range(len(edge_list))}
    for s in range(samples):
        for i in range(len(edge_list)):
            edge = edge_list[i]
            edge_times = queryTour(edge)
            res_dict[i].append(edge_times)
        if s % 100 == 0:
            pickle.dump(res_dict, open("Results/Status/Edge Times Data_" + str(s) + ".p", "wb"))
        print("Sample Number:", s)
        sys.stdout.flush()
    pickle.dump(res_dict, open("Results/Edge Times Data Encrypted.p", "wb"))


if __name__ == "__main__":
    collect_data()

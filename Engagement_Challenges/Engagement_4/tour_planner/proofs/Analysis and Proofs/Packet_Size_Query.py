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
import os
import pickle
import itertools


def queryTour(index, tourList):
    """Send tour query, return packet sizes"""
    tourString = ""
    for cityIndex in range(len(tourList)):
        city = tourList[cityIndex]
        tourString += city.replace(" ", "%20")
        if cityIndex < len(tourList) - 1:
            tourString += "&point="

    # Start TCP Dump
    os.chdir("Results/Status/TCPDat")
    fileOut = "tcpOut_" + str(index) + ".txt"
    fileErr = "tcpErr_" + str(index) + ".txt"
    tcpOut = open(fileOut, "w")
    tcpErr = open(fileErr, "w")
    tcpdump = subprocess.Popen(["tcpdump", "-i", "lo", "port", "8989"], stdout=tcpOut, stderr=tcpErr)
    time.sleep(0.1)
    subprocess.call(["/bin/curl", "-k", "-s", "-o", ".rnd", "-i", "https://127.0.0.1:8989/tour?point=" + tourString])
    tcpdump.terminate()
    tcpOut.close()
    tcpErr.close()
    time.sleep(0.1)
    tcpOut = open(fileOut, "r")
    lines = tcpOut.readlines()
    tcpOut.close()
    os.chdir("../../..")
    sendline = lines[12]
    recvline = lines[24]
    sendsize = int(sendline.split("length ")[-1].split("\n")[0])
    recvsize = int(recvline.split("length ")[-1].split("\n")[0])
    return [index, sendsize, recvsize]


def getCombinations(myList, n):
    return [sorted(list(c)) for c in itertools.combinations(myList, n)]


def read_data():
    """Read In Data"""
    city_list = pickle.load(open("Cities.p", "rb"))
    return city_list


def main():
    """Collect data on packet sizes for possible user queries constrained by MSTs"""
    city_list = read_data()
    city_sets = getCombinations(city_list, 5)

    results = []
    for i in range(len(city_sets)):
        size_data = queryTour(i, city_sets[i])
        print(size_data)
        results.append(size_data)
        if i % 100 == 0:
            pickle.dump(results, open("Results/Status/PQ_" + str(i) + ".p", "wb"))
    pickle.dump(results, open("Results/Packet Size Queries.p", "wb"))


if __name__ == "__main__":
    main()

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
    """Send Tour Query Return Edge Times"""
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


def main():
    """Collect timing data for best and worst cases to be processed in parallel"""

    bestEdge = {'Worcester', 'Lowell', 'Cambridge', 'Lawrence', 'Somerville'}
    worstEdge = {'Worcester', 'Springfield', 'Fall River', 'Haverhill', 'Plymouth'}

    bestMahD_1 = {'Boston', 'Haverhill', 'New Bedford', 'Revere', 'Worcester'}
    worstMahD_1 = {'Chicopee', 'Malden', 'Revere', 'Somerville', 'Waltham'}

    bestMahD_01 = {'Boston', 'Chicopee', 'Lowell', 'Springfield', 'Worcester'}
    worstMahD_01 = {'Brockton', 'Chicopee', 'Newton', 'Peabody', 'Worcester'}

    bestBhatD_Mean = {'Cambridge', 'Fall River', 'Lawrence', 'Waltham', 'Weymouth'}
    worstBhatD = {'Haverhill', 'New Bedford', 'Springfield', 'Weymouth', 'Worcester'}

    bestBhatD_10 = {'Cambridge', 'Peabody', 'Quincy', 'Taunton', 'Waltham'}

    bestBhatD_25 = {'Brockton', 'Lynn', 'New Bedford', 'Peabody', 'Quincy'}

    bestBhatD_50 = {'Boston', 'Fall River', 'Haverhill', 'Lawrence', 'Weymouth'}

    worstBBN = {'Cambridge', 'Medford', 'Brookline', 'Peabody', 'Lowell'}

    print("Starting Data Collection")
    sys.stdout.flush()

    List_Queries = [list(bestEdge), list(worstEdge),
                    list(bestMahD_1), list(worstMahD_1),
                    list(bestMahD_01), list(worstMahD_01),
                    list(bestBhatD_Mean), list(worstBhatD),
                    list(bestBhatD_10), list(bestBhatD_25),
                    list(bestBhatD_50), list(worstBBN)]
    DataSet = []

    for i in range(500):
        ts_a = time.time()

        # Collect Data
        List_Query_Times = [[List_Queries[i], queryTour(List_Queries[i]), i] for i in range(len(List_Queries))]
        DataSet.append(List_Query_Times)

        ts_b = time.time()
        if i % 10 == 0:
            netResult = {"List_Queries": List_Queries,
                         "List_Query_TimeList": DataSet}
            pickle.dump(netResult,
                        open("Results/Status/Guess Tour All Methods DataSet Encrypted " + str(i) + ".p", "wb"))

        # Print Status
        print(i, ts_b - ts_a)
        sys.stdout.flush()

    netResult = {"List_Queries": List_Queries,
                 "List_Query_TimeList": DataSet}

    pickle.dump(netResult, open("Results/Guess Tour All Methods DataSet Encrypted.p", "wb"))


if __name__ == "__main__":
    main()

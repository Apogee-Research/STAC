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
import itertools
from operator import itemgetter
import Ndim_Guessing_Methods as GM
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


def getCombinations(myList, n):
    return [list(c) for c in itertools.combinations(myList, n)]


def getMSTEdges(startCity, citySet, edgeList):
    mstEdges = []
    chosenCities = {startCity}
    while len(chosenCities) < len(citySet):
        for edge in edgeList:
            if (edge[0] in chosenCities and edge[1] not in chosenCities) or \
                    (edge[1] in chosenCities and edge[0] not in chosenCities):
                if edge[0] in citySet and edge[1] in citySet:
                    mstEdges.append(edgeList.index(edge))
                    chosenCities.add(edge[0])
                    chosenCities.add(edge[1])
                    break
    return mstEdges


def buildMSTVectors(edgeMeans, edgeStdevs, edgeList, cityList):
    MSTs = []
    citySets = getCombinations(cityList, 5)
    for citySet in citySets:
        mstEdges = getMSTEdges(citySet[0], citySet, edgeList)
        MSTs.append(mstEdges)

    MSTVectors = []
    for mst in MSTs:
        mstInfo = [[edge, edgeMeans[edge], edgeStdevs[edge]] for edge in mst]
        mstInfo.sort(key=itemgetter(1, 2, 0))
        indexVec, meanVec, stdVec = zip(*mstInfo)
        citySet = sorted(set([pair for edge in mst for pair in edgeList[edge]]))
        MSTVectors.append([indexVec, meanVec, stdVec, citySet])
    return MSTVectors


def guessTour_MahD(tourTimes, mstVectors, correctCitiesIn):
    orderedTourTimes = sorted(tourTimes)
    mstPcVectors = []
    for mstVector in mstVectors:
        mstIndexVec = mstVector[0]
        mstMeanVec = mstVector[1]
        mstStdevVec = mstVector[2]
        mstCityVec = mstVector[3]

        dist = sum([((orderedTourTimes[i] - mstMeanVec[i]) / mstStdevVec[i]) ** 2 for i in range(len(tourTimes))]) ** (
            0.5)
        mstPcVectors.append([mstIndexVec, mstCityVec, dist])
    mstPcVectors.sort(key=itemgetter(2))
    indexVecList, cityVecList, probVecList = zip(*mstPcVectors)
    correctCities = sorted(correctCitiesIn)
    indexCorrect = cityVecList.index(correctCities)
    numOracleQueries = indexCorrect + 1
    return numOracleQueries


def guessTour_Ndim(tourTimes, mstVectors, correctCitiesIn):
    orderedTourTimes = sorted(tourTimes)
    mstPcVectors = []
    for mstVector in mstVectors:
        mstIndexVec = mstVector[0]
        mstMeanVec = mstVector[1]
        mstStdevVec = mstVector[2]
        mstCityVec = mstVector[3]

        # Call Make Matrices
        mu, sig, tmat = GM.makeMatrices(mstMeanVec, mstStdevVec, orderedTourTimes)
        mstPc = GM.nvarGaussian(mu, sig, tmat)
        mstPcVectors.append([mstIndexVec, mstCityVec, mstPc])
    mstPcVectors.sort(key=itemgetter(2), reverse=True)
    indexVecList, cityVecList, probVecList = zip(*mstPcVectors)
    correctCities = sorted(correctCitiesIn)
    indexCorrect = cityVecList.index(correctCities)
    numOracleQueries = indexCorrect + 1
    return numOracleQueries


def guessTour_VectorD(tourTimes, mstVectors, correctCitiesIn):
    orderedTourTimes = sorted(tourTimes)
    mstPcVectors = []
    for mstVector in mstVectors:
        mstIndexVec = mstVector[0]
        mstMeanVec = mstVector[1]
        mstCityVec = mstVector[3]

        dist = sum([(orderedTourTimes[i] - mstMeanVec[i]) ** 2 for i in range(len(tourTimes))]) ** (0.5)
        mstPcVectors.append([mstIndexVec, mstCityVec, dist])
    mstPcVectors.sort(key=itemgetter(2))
    indexVecList, cityVecList, probVecList = zip(*mstPcVectors)
    correctCities = sorted(correctCitiesIn)
    indexCorrect = cityVecList.index(correctCities)
    numOracleQueries = indexCorrect + 1
    return numOracleQueries


def read_data():
    """Read In Data"""
    inDict_Packet_Times = pickle.load(open("Results/Mean_Stdev_Data_Packet_Time_Encrypted.p", "rb"))
    edgeList = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cityList = pickle.load(open("Results/Cities.p", "rb"))
    return inDict_Packet_Times, edgeList, cityList


def main():
    """Collect times using identified best and worst cases and use available guessing strategies to determine secret"""
    inDict_Packet_Times, edgeList, cityList = read_data()

    # Time in milliseconds
    edgeMeans = inDict_Packet_Times["means"]
    edgeStdevs = inDict_Packet_Times["stdevs"]

    mstVectors = buildMSTVectors(edgeMeans, edgeStdevs, edgeList, cityList)

    bestEdge = {'Brockton', 'Fall River', 'Haverhill', 'Malden', 'Medford'}
    worstEdge = {'Boston', 'Worcester', 'Springfield', 'Lowell', 'Brockton'}

    bestMahD_1 = {'Brockton', 'New Bedford', 'Peabody', 'Plymouth', 'Worcester'}
    worstMahD_1 = {'Boston', 'Brockton', 'Brookline', 'Lynn', 'Springfield'}

    bestMahD_01 = {'Boston', 'Chicopee', 'New Bedford', 'Springfield', 'Worcester'}
    worstMahD_01 = {'Boston', 'Brockton', 'Brookline', 'Chicopee', 'Somerville'}

    bestBhatD_Median = set([])
    worstBhatD_Median = set([])

    bestBhatD_Mean = set([])
    worstBhatD_Mean = set([])

    worstBBN = {'Cambridge', 'Medford', 'Brookline', 'Peabody', 'Lowell'}

    print("Starting Data Collection and Processing")
    print()
    sys.stdout.flush()

    List_Queries = [list(bestEdge), list(worstEdge),
                    list(bestMahD_1), list(worstMahD_1),
                    list(bestMahD_01), list(worstMahD_01),
                    list(bestBhatD_Median), list(worstBhatD_Median),
                    list(bestBhatD_Mean), list(worstBhatD_Mean),
                    list(worstBBN)]

    resDict_MahD = {i: [] for i in range(len(List_Queries))}
    resDict_Ndim = {i: [] for i in range(len(List_Queries))}
    resDict_VectorD = {i: [] for i in range(len(List_Queries))}
    timeSet = {i: [] for i in range(len(List_Queries))}

    for i in range(1):
        ts_a = time.time()

        # Collect Data
        List_Query_Times = [[List_Queries[i], queryTour(List_Queries[i]), i] for i in range(len(List_Queries))]

        # Test Data
        for querySet in List_Query_Times:
            edgeTimes = querySet[1]
            cities = querySet[0]
            queryIndex = querySet[2]

            result_MahD = guessTour_MahD(edgeTimes, mstVectors, cities)
            result_Ndim = guessTour_Ndim(edgeTimes, mstVectors, cities)
            result_VectorD = guessTour_VectorD(edgeTimes, mstVectors, cities)

            resDict_MahD[queryIndex].append(result_MahD)
            resDict_Ndim[queryIndex].append(result_Ndim)
            resDict_VectorD[queryIndex].append(result_VectorD)
            timeSet[queryIndex].append(edgeTimes)

        ts_b = time.time()
        if i % 10 == 0:
            netResult = {"MahD": resDict_MahD,
                         "Ndim": resDict_Ndim,
                         "VectorD": resDict_VectorD,
                         "Time Data": timeSet}
            pickle.dump(netResult, open("Results/Status/Guess Tour All Methods 1000 Encrypted " + str(i) + ".p", "wb"))

        # Print Status
        print(i, len(resDict_MahD[0]), len(resDict_Ndim[1]), len(resDict_VectorD[2]), ts_b - ts_a)
        sys.stdout.flush()

    netResult = {"MahD": resDict_MahD,
                 "Ndim": resDict_Ndim,
                 "VectorD": resDict_VectorD,
                 "Time Data": timeSet}

    pickle.dump(netResult, open("Results/Guess Tour All Methods 1000 Encrypted.p", "wb"))


if __name__ == "__main__":
    main()

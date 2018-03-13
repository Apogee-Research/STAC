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
from operator import itemgetter
import numpy as np
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
    """Guess the tour from the set of input times using the Mahalanobis distance"""
    orderedTourTimes = sorted(tourTimes)
    mstPcVectors = []
    for mstVector in mstVectors:
        mstIndexVec = mstVector[0]
        mstMeanVec = mstVector[1]
        mstStdevVec = mstVector[2]
        mstCityVec = mstVector[3]
        inputtest = [mstMeanVec, mstStdevVec, orderedTourTimes]
        if not all(type(it) == list or type(it) == tuple for it in inputtest):
            return "False"
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
    """Guess the tour from the set of input times using multivariate Gaussian"""
    orderedTourTimes = sorted(tourTimes)
    mstPcVectors = []
    for mstVector in mstVectors:
        mstIndexVec = mstVector[0]
        mstMeanVec = mstVector[1]
        mstStdevVec = mstVector[2]
        mstCityVec = mstVector[3]
        inputtest = [mstMeanVec, mstStdevVec, orderedTourTimes]
        if not all(type(it) == list or type(it) == tuple for it in inputtest):
            return "False"
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
    """Guess the tour from the set of input times using 2-norm"""
    orderedTourTimes = sorted(tourTimes)
    mstPcVectors = []
    for mstVector in mstVectors:
        mstIndexVec = mstVector[0]
        mstMeanVec = mstVector[1]
        mstStdevVec = mstVector[2]
        mstCityVec = mstVector[3]
        inputtest = [mstMeanVec, mstStdevVec, orderedTourTimes]
        if not all(type(it) == list or type(it) == tuple for it in inputtest):
            return "False"
        dist = sum([(orderedTourTimes[i] - mstMeanVec[i]) ** 2 for i in range(len(tourTimes))]) ** 0.5
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
    """Perform random sampling of 5-city tours and use available guessing strategies to determine secret"""
    inDict_Packet_Times, edgeList, cityList = read_data()

    # Time in milliseconds
    edgeMeans = inDict_Packet_Times["means"]
    edgeStdevs = inDict_Packet_Times["stdevs"]

    mstVectors = buildMSTVectors(edgeMeans, edgeStdevs, edgeList, cityList)

    print("Starting")
    print("Ndim,\tMahD,\tDist")
    print()
    sys.stdout.flush()
    np.random.seed(11234)
    citySets = getCombinations(cityList, 5)
    indexChoices = list(range(0, len(citySets)))

    resDict_MahD = []
    resDict_Ndim = []
    resDict_VectorD = []
    chosenIndexes = []
    for i in range(0, 500):
        ts_a = time.time()

        # Test Data
        chosenIndex = np.random.choice(indexChoices)
        cities = citySets[chosenIndex]
        edgeTimes = queryTour(cities)
        if not len(edgeTimes) == 4:
            print("Error1", i, edgeTimes, '\n\t', chosenIndex, cities)
            continue

        result_MahD = guessTour_MahD(edgeTimes, mstVectors, cities)
        result_Ndim = guessTour_Ndim(edgeTimes, mstVectors, cities)
        result_VectorD = guessTour_VectorD(edgeTimes, mstVectors, cities)
        resulttest = [result_MahD, result_Ndim, result_VectorD]
        if any(rt == "False" for rt in resulttest):
            print("Error2", i, edgeTimes, '\n\t', chosenIndex, cities)
            continue

        resDict_MahD.append(result_MahD)
        resDict_Ndim.append(result_Ndim)
        resDict_VectorD.append(result_VectorD)
        chosenIndexes.append([chosenIndex, edgeTimes])

        ts_b = time.time()

        # Print Status
        print(i, "\t", result_Ndim, "\t", result_MahD, "\t", result_VectorD, "\t", chosenIndex, "\t", ts_b - ts_a)
        sys.stdout.flush()

        if i % 100 == 0:
            netResult = {"MahD": resDict_MahD,
                         "Ndim": resDict_Ndim,
                         "VectorD": resDict_VectorD,
                         "Index": chosenIndexes,
                         "Random State": np.random.get_state()}
            os.chdir("Results/Status")
            pickle.dump(netResult, open("Guess Tour All Methods Sampling Encrypted" + str(i) + ".p", "wb"))
            os.chdir("../..")

    netResult = {"MahD": resDict_MahD,
                 "Ndim": resDict_Ndim,
                 "VectorD": resDict_VectorD,
                 "Index": chosenIndexes,
                 "Random State": np.random.get_state()}

    pickle.dump(netResult, open("Results/Guess Tour All Methods Sampling Encrypted.p", "wb"))


if __name__ == "__main__":
    main()

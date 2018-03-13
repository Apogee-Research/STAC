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

import time
import pickle
import itertools
from operator import itemgetter
import Ndim_Guessing_Methods as GM
import sys
import multiprocessing as mp


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
        mstCityVec = mstVector[3]

        dist = sum([(orderedTourTimes[i] - mstMeanVec[i]) ** 2 for i in range(len(tourTimes))]) ** (0.5)
        mstPcVectors.append([mstIndexVec, mstCityVec, dist])
    mstPcVectors.sort(key=itemgetter(2))
    indexVecList, cityVecList, probVecList = zip(*mstPcVectors)
    correctCities = sorted(correctCitiesIn)
    indexCorrect = cityVecList.index(correctCities)
    numOracleQueries = indexCorrect + 1
    return numOracleQueries


def guessMethods(gmInput):
    gmIndex = gmInput[0]
    List_Query_Times = gmInput[1]
    mstVectors = gmInput[2]
    resultOracleQueries = []
    for querySet in List_Query_Times:
        edgeTimes = querySet[1]
        cities = querySet[0]
        queryIndex = querySet[2]
        result_MahD = guessTour_MahD(edgeTimes, mstVectors, cities)
        result_Ndim = guessTour_Ndim(edgeTimes, mstVectors, cities)
        result_VectorD = guessTour_VectorD(edgeTimes, mstVectors, cities)
        resultOracleQueries.append([queryIndex, result_MahD, result_Ndim, result_VectorD])
    sys.stdout.write(str(gmIndex) + "\n")
    sys.stdout.flush()
    return resultOracleQueries


def read_data():
    """Read In Data"""
    inDict_Packet_Times = pickle.load(open("Results/Mean_Stdev_Data_Packet_Time_Encrypted.p", "rb"))
    edgeList = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cityList = pickle.load(open("Results/Cities.p", "rb"))
    netResult = pickle.load(open("Results/Guess Tour All Methods DataSet Encrypted.p", "rb"))
    return inDict_Packet_Times, edgeList, cityList, netResult


def main(processes=8, test=False):
    """Process data collected with Guess Tour All Methods_Collect_Data using MST guessing strategies
    discussed in Tour Planner White Paper"""

    inDict_Packet_Times, edgeList, cityList, netResult = read_data()

    # Time in milliseconds
    edgeMeans = inDict_Packet_Times["means"]
    edgeStdevs = inDict_Packet_Times["stdevs"]
    mstVectors = buildMSTVectors(edgeMeans, edgeStdevs, edgeList, cityList)
    List_Queries = netResult["List_Queries"]
    List_Query_TimeList = netResult["List_Query_TimeList"]

    guessMethodInputs = [[i, v, mstVectors] for i, v in enumerate(List_Query_TimeList)]

    if test:
        ta = time.time()
        res = guessMethods(guessMethodInputs[100])
        tb = time.time()
        print(res)
        print(tb - ta)

    pool = mp.Pool(processes)
    results = pool.map_async(guessMethods, guessMethodInputs)
    pool.close()
    pool.join()
    pool.terminate()
    output = []
    for res in results._value:
        output += res

    resDict_MahD = {i: [] for i in range(len(List_Queries))}
    resDict_Ndim = {i: [] for i in range(len(List_Queries))}
    resDict_VectorD = {i: [] for i in range(len(List_Queries))}
    for result in output:
        queryIndex = result[0]
        resDict_MahD[queryIndex].append(result[1])
        resDict_Ndim[queryIndex].append(result[2])
        resDict_VectorD[queryIndex].append(result[3])

    processedResult = {"MahD": resDict_MahD,
                       "Ndim": resDict_Ndim,
                       "VectorD": resDict_VectorD}

    pickle.dump(processedResult, open("Results/Guess Tour All Methods Processed Results Encrypted.p", "wb"))


if __name__ == "__main__":
    main()

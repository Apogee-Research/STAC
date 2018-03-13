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

import pickle
import Analysis_Methods
import sys
import numpy as np
import multiprocessing as mp
import matplotlib as mpl

mpl.use('Agg')
import matplotlib.pyplot as plt
import copy
from decimal import *


def getConnectingEdges(chosenCities, edgeList):
    connectingEdges = []
    for edge in edgeList:
        if (edge[0] in chosenCities and edge[1] not in chosenCities) or \
                (edge[1] in chosenCities and edge[0] not in chosenCities):
            connectingEdges.append(edgeList.index(edge))
    return connectingEdges


def levelInfo(means, stdevs):
    x = list(range(0, len(means)))
    # Build Matrix of Intersection Points  
    intersections = []
    for i_1 in x:
        level = []
        for i_2 in x:
            level.append(Analysis_Methods.calcIntersectionT(means[i_1], stdevs[i_1], means[i_2], stdevs[i_2]))
        intersections.append(level)

    # Build List of Closest Intersection Points  
    closest_intersections = []
    t_ranges = []
    for i in x:
        closest_intersections.append(
            Analysis_Methods.findClosestIntersections(i, means[i], stdevs[i], intersections[i]))
        if i == x[0]:
            tmin = 0
            tmax = intersections[i][closest_intersections[i][0]]
        elif i == x[-1]:
            tmin = intersections[i][closest_intersections[i][0]]
            tmax = 100
        else:
            tmin = intersections[i][closest_intersections[i][0]]
            tmax = intersections[i][closest_intersections[i][1]]
        t_ranges.append([tmin, tmax])

    # Build List of Max Errors
    errors = []
    for i in x:
        errors.append(Analysis_Methods.pErrorInt(i, closest_intersections[i], intersections, means, stdevs))

    return errors, t_ranges


# Return min Error Edge
def getEdgeInfo1(myTourTimes, myMeans, myStdevs):
    errors, ranges = levelInfo(myMeans, myStdevs)
    edgeIndexes_Errors = []
    originalOrderEdgeErrors = []
    for t in myTourTimes:
        for i in range(len(ranges)):
            if t > ranges[i][0] and t <= ranges[i][1]:
                edgeIndexes_Errors.append([i, errors[i]])
                originalOrderEdgeErrors.append(errors[i])
                break
    if len(edgeIndexes_Errors) == 0:
        return "NA"
    edgeIndexes_Errors.sort(key=lambda x: x[1])
    edgeIndexes, edgeErrors = zip(*edgeIndexes_Errors)
    return [edgeIndexes, edgeErrors, originalOrderEdgeErrors]


def guessTour_Intersections1(tourTimes, edgeMeans, edgeStdevs, edgeList, correctCitiesIn):
    """Guess the tour from the set of input times using edge-by-edge guessing strategy 1"""
    chosenCities = set()
    chosenCitiesList = []
    usedTimes = []
    repeat1 = 0
    repeat2 = 0
    repeat3 = 0
    repeat4 = 0

    while usedTimes < tourTimes:
        if len(usedTimes) == 0:
            # Get Edge 1
            E1_TourTimes = copy.deepcopy(tourTimes)
            # Calculate and Return Edges for Times Sorted by Probability
            E1_Return = getEdgeInfo1(E1_TourTimes, edgeMeans, edgeStdevs)
            if E1_Return == "NA" or repeat1 >= len(E1_Return[0]):
                return False
            E1_Edge = E1_Return[0][repeat1]
            E1_TimeIndex = E1_Return[2].index(E1_Return[1][repeat1])
            chosenCities.add(edgeList[E1_Edge][0])
            chosenCities.add(edgeList[E1_Edge][1])
            chosenCitiesList.append(copy.deepcopy(chosenCities))
            usedTimes.append(E1_TourTimes[E1_TimeIndex])
            del tourTimes[E1_TimeIndex]
            repeat1 += 1
        if len(usedTimes) == 1:
            # Get Edge 2
            E2_TourTimes = copy.deepcopy(tourTimes)
            E2_ConnectingEdges = getConnectingEdges(chosenCities, edgeList)
            E2_Means = [edgeMeans[i] for i in E2_ConnectingEdges]
            E2_Stdevs = [edgeStdevs[i] for i in E2_ConnectingEdges]
            E2_Return = getEdgeInfo1(E2_TourTimes, E2_Means, E2_Stdevs)
            if E2_Return == "NA" or repeat2 >= len(E2_Return[0]):
                tourTimes = E1_TourTimes
                chosenCities = chosenCitiesList[-1]
                del chosenCitiesList[-1]
                usedTimes = []
                repeat2 = 0
                continue
            E2_Edge = E2_ConnectingEdges[E2_Return[0][repeat2]]
            E2_TimeIndex = E2_Return[2].index(E2_Return[1][repeat2])
            chosenCities.add(edgeList[E2_Edge][0])
            chosenCities.add(edgeList[E2_Edge][1])
            chosenCitiesList.append(copy.deepcopy(chosenCities))
            usedTimes.append(E2_TourTimes[E2_TimeIndex])
            del tourTimes[E2_TimeIndex]
            repeat2 += 1
        if len(usedTimes) == 2:
            # Get Edge 3
            E3_TourTimes = copy.deepcopy(tourTimes)
            E3_ConnectingEdges = getConnectingEdges(chosenCities, edgeList)
            E3_Means = [edgeMeans[i] for i in E3_ConnectingEdges]
            E3_Stdevs = [edgeStdevs[i] for i in E3_ConnectingEdges]
            E3_Return = getEdgeInfo1(E3_TourTimes, E3_Means, E3_Stdevs)
            if E3_Return == "NA" or repeat3 >= len(E3_Return[0]):
                tourTimes = E2_TourTimes
                chosenCities = chosenCitiesList[-1]
                del chosenCitiesList[-1]
                usedTimes = [usedTimes[0]]
                repeat3 = 0
                continue
            E3_Edge = E3_ConnectingEdges[E3_Return[0][repeat3]]
            E3_TimeIndex = E3_Return[2].index(E3_Return[1][repeat3])
            chosenCities.add(edgeList[E3_Edge][0])
            chosenCities.add(edgeList[E3_Edge][1])
            chosenCitiesList.append(copy.deepcopy(chosenCities))
            usedTimes.append(E3_TourTimes[E3_TimeIndex])
            del tourTimes[E3_TimeIndex]
            repeat3 += 1
        if len(usedTimes) == 3:
            # Get Edge 4
            E4_TourTimes = copy.deepcopy(tourTimes)
            E4_ConnectingEdges = getConnectingEdges(chosenCities, edgeList)
            E4_Means = [edgeMeans[i] for i in E4_ConnectingEdges]
            E4_Stdevs = [edgeStdevs[i] for i in E4_ConnectingEdges]
            E4_Return = getEdgeInfo1(E4_TourTimes, E4_Means, E4_Stdevs)
            if E4_Return == "NA" or repeat4 >= len(E4_Return[0]):
                tourTimes = E3_TourTimes
                chosenCities = chosenCitiesList[-1]
                del chosenCitiesList[-1]
                usedTimes = [usedTimes[0], usedTimes[1]]
                repeat4 = 0
                continue
            E4_Edge = E4_ConnectingEdges[E4_Return[0][repeat4]]
            E4_TimeIndex = E4_Return[2].index(E4_Return[1][repeat4])
            chosenCities.add(edgeList[E4_Edge][0])
            chosenCities.add(edgeList[E4_Edge][1])
            chosenCitiesList.append(copy.deepcopy(chosenCities))
            usedTimes.append(E4_TourTimes[E4_TimeIndex])
            del tourTimes[E4_TimeIndex]
            repeat4 += 1
    correct = len(set(chosenCities).intersection(set(correctCitiesIn)))
    return correct


# Get Edge Distribution
def getEdgeDistribution(myEdgeIndexes, allMSTs):
    flatMSTEdges = np.array(allMSTs).flatten()
    hMSTE_0 = plt.hist(flatMSTEdges, bins=list(range(0, 301)))[0]
    histMSTEdges = [hMSTE_0[i] if i in myEdgeIndexes else 0 for i in range(300)]
    plt.close()
    probEdges = histMSTEdges / np.sum(histMSTEdges)
    return probEdges


def getEdgeError(myTourTimes, myEdges, edgeMeans, edgeStdevs, mstEdgeDistributions):
    getcontext().prec = 100
    # Identify Links using Bayes
    # P(Edge | time t*) = P(time t* | Edge)*P(Edge)/P(time t*)
    edgeProbabilities = []
    possibleEdgeIndexes = []
    for t in myTourTimes:
        # List of P(time t*| Edge) 
        pTEs = []
        # List of P(Edge)
        pEs = []
        for edge in myEdges:
            pTE = Analysis_Methods.pGaussian(edgeMeans[edge], edgeStdevs[edge], t)
            pE = mstEdgeDistributions[edge]
            pTEs.append(pTE)
            pEs.append(pE)
        # P(time t*) = Sum(P(time t* | Edge) / Number of Edges)
        pT = Decimal(sum(pTEs)) / Decimal(len(pTEs))
        # List of P(Edge | time t*)
        pETs = [Decimal(pTEs[i]) * Decimal(pEs[i]) / pT if pT != 0 else 0 for i in range(len(pTEs))]
        edgeProbabilities.append(max(pETs))
        possibleEdgeIndexes.append(myEdges[pETs.index(max(pETs))])
    maxIndex = edgeProbabilities.index(max(edgeProbabilities))
    edge = possibleEdgeIndexes[maxIndex]
    return edge, maxIndex


def guessTour_Bayes(tourTimes, edgeMeans, edgeStdevs, edgeList, allMSTs, correctCitiesIn):
    """Guess the tour from the set of input times using edge-by-edge guessing strategy 2 (Bayes Rule)"""
    chosenCities = set()
    usedTimes = []

    # Get Edge 1
    E1_TourTimes = copy.deepcopy(tourTimes)
    E1_Edges = list(range(len(edgeMeans)))
    E1_MSTEdgeDistributions = getEdgeDistribution(E1_Edges, allMSTs)
    E1_Edge, E1_Index = getEdgeError(E1_TourTimes, E1_Edges, edgeMeans, edgeStdevs, E1_MSTEdgeDistributions)
    chosenCities.add(edgeList[E1_Edge][0])
    chosenCities.add(edgeList[E1_Edge][1])
    usedTimes.append(E1_TourTimes[E1_Index])
    del tourTimes[E1_Index]

    # Get Edge 2
    E2_TourTimes = copy.deepcopy(tourTimes)
    E2_Edges = getConnectingEdges(chosenCities, edgeList)
    E2_MSTEdgeDistributions = getEdgeDistribution(E2_Edges, allMSTs)
    E2_Edge, E2_Index = getEdgeError(E2_TourTimes, E2_Edges, edgeMeans, edgeStdevs, E2_MSTEdgeDistributions)
    chosenCities.add(edgeList[E2_Edge][0])
    chosenCities.add(edgeList[E2_Edge][1])
    usedTimes.append(E2_TourTimes[E2_Index])
    del tourTimes[E2_Index]

    # Get Edge 3
    E3_TourTimes = copy.deepcopy(tourTimes)
    E3_Edges = getConnectingEdges(chosenCities, edgeList)
    E3_MSTEdgeDistributions = getEdgeDistribution(E3_Edges, allMSTs)
    E3_Edge, E3_Index = getEdgeError(E3_TourTimes, E3_Edges, edgeMeans, edgeStdevs, E3_MSTEdgeDistributions)
    chosenCities.add(edgeList[E3_Edge][0])
    chosenCities.add(edgeList[E3_Edge][1])
    usedTimes.append(E3_TourTimes[E3_Index])
    del tourTimes[E3_Index]

    # Get Edge 4
    E4_TourTimes = copy.deepcopy(tourTimes)
    E4_Edges = getConnectingEdges(chosenCities, edgeList)
    E4_MSTEdgeDistributions = getEdgeDistribution(E4_Edges, allMSTs)
    E4_Edge, E4_Index = getEdgeError(E4_TourTimes, E4_Edges, edgeMeans, edgeStdevs, E4_MSTEdgeDistributions)
    chosenCities.add(edgeList[E4_Edge][0])
    chosenCities.add(edgeList[E4_Edge][1])
    usedTimes.append(E4_TourTimes[E4_Index])
    del tourTimes[E4_Index]

    correct = len(set(chosenCities).intersection(set(correctCitiesIn)))
    return correct


def guessMethods(gmInput):
    gmIndex = gmInput[0]
    List_Query_Times = gmInput[1]
    edgeMeans = gmInput[2]
    edgeStdevs = gmInput[3]
    edgeList = gmInput[4]
    allMSTs = gmInput[5]

    resultOracleQueries = []
    for querySet in List_Query_Times:
        edgeTimes = querySet[1]
        cities = querySet[0]
        queryIndex = querySet[2]
        result_Intersections1 = guessTour_Intersections1(copy.deepcopy(edgeTimes), edgeMeans, edgeStdevs, edgeList,
                                                         cities)
        result_Bayes = guessTour_Bayes(copy.deepcopy(edgeTimes), edgeMeans, edgeStdevs, edgeList, allMSTs, cities)
        resultOracleQueries.append([queryIndex, result_Intersections1, result_Bayes])
    sys.stdout.write(str(gmIndex) + "\n")
    sys.stdout.flush()
    return resultOracleQueries


def read_data():
    """Read In Data"""
    inDict_Packet_Times = pickle.load(open("Results/Mean_Stdev_Data_Packet_Time_Encrypted.p", "rb"))
    edgeList = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cityList = pickle.load(open("Results/Cities.p", "rb"))
    netResult = pickle.load(open("Results/Guess Tour All Methods DataSet Encrypted.p", "rb"))
    allMSTs = pickle.load(open("Results/AllMSTs.p", "rb"))
    return inDict_Packet_Times, edgeList, cityList, netResult, allMSTs


def main(processes=8):
    """Process data collected with Guess Tour All Methods_Collect_Data using edge-by-edge guessing strategies
    discussed in Tour Planner White Paper. Results in a single guess for each query"""

    inDict_Packet_Times, edgeList, cityList, netResult, allMSTs = read_data()

    # Time in milliseconds
    edgeMeans = inDict_Packet_Times["means"]
    edgeStdevs = inDict_Packet_Times["stdevs"]
    List_Queries = netResult["List_Queries"]
    List_Query_TimeList = netResult["List_Query_TimeList"]

    guessMethodInputs = [[i, v, edgeMeans, edgeStdevs, edgeList, allMSTs] for i, v in enumerate(List_Query_TimeList)]
    resDict_Intersection = {i: [] for i in range(len(List_Queries))}
    resDict_Bayes = {i: [] for i in range(len(List_Queries))}

    pool = mp.Pool(processes)
    results = pool.map_async(guessMethods, guessMethodInputs)
    pool.close()
    pool.join()
    pool.terminate()

    pickle.dump(results._value, open("Results/Raw Results.p", "wb"))

    output = []
    for res in results._value:
        output += res
    for result in output:
        queryIndex = result[0]
        resDict_Intersection[queryIndex].append(result[1])
        resDict_Bayes[queryIndex].append(result[2])

    processedResult = {"Intersection": resDict_Intersection,
                       "Bayes": resDict_Bayes}

    pickle.dump(processedResult,
                open("Results/Guess Tour All Methods Processed Results Edge by Edge Encrypted.p", "wb"))


if __name__ == "__main__":
    main()

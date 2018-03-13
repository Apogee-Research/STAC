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
import itertools
import multiprocessing as mp
import sys


def levelError(means, stdevs, method):
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
    for i in x:
        closest_intersections.append(
            Analysis_Methods.findClosestIntersections(i, means[i], stdevs[i], intersections[i]))

    # Build List of Max Errors
    errors = []
    for i in x:
        if method == "Difference":
            errors.append(Analysis_Methods.pErrorDiffChains(i, closest_intersections[i], intersections, means, stdevs))
        elif method == "Ratio":
            errors.append(Analysis_Methods.pErrorInt(i, closest_intersections[i], intersections, means, stdevs))

    return errors


def getCombinations(myList, n):
    return [list(c) for c in itertools.combinations(myList, n)]


def getConnectingEdges(chosenCities, edgeList):
    connectingEdges = []
    for edge in edgeList:
        if (edge[0] in chosenCities and edge[1] not in chosenCities) or \
                (edge[1] in chosenCities and edge[0] not in chosenCities):
            connectingEdges.append(edgeList.index(edge))
    return connectingEdges


# Get MST
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


def getEdgeOrderError(myEdges, myE1Errors, method, edgeList, myMeans, myStdevs):
    chosenCities = set()
    # Edge 1 Error
    E1 = myEdges[0]
    E1_Error = myE1Errors[E1]
    chosenCities.add(edgeList[E1][0])
    chosenCities.add(edgeList[E1][1])

    # Edge 2 Error
    E2 = myEdges[1]
    E2_ConnectingEdges = getConnectingEdges(chosenCities, edgeList)
    E2_Means = [myMeans[i] for i in E2_ConnectingEdges]
    E2_Stdevs = [myStdevs[i] for i in E2_ConnectingEdges]
    E2_Errors = levelError(E2_Means, E2_Stdevs, method)
    E2_Error = E2_Errors[E2_ConnectingEdges.index(E2)]
    chosenCities.add(edgeList[E2][0])
    chosenCities.add(edgeList[E2][1])

    # Edge 3 Error
    E3 = myEdges[2]
    E3_ConnectingEdges = getConnectingEdges(chosenCities, edgeList)
    E3_Means = [myMeans[i] for i in E3_ConnectingEdges]
    E3_Stdevs = [myStdevs[i] for i in E3_ConnectingEdges]
    E3_Errors = levelError(E3_Means, E3_Stdevs, method)
    E3_Error = E3_Errors[E3_ConnectingEdges.index(E3)]
    chosenCities.add(edgeList[E3][0])
    chosenCities.add(edgeList[E3][1])

    # Edge 4 Error
    E4 = myEdges[3]
    E4_ConnectingEdges = getConnectingEdges(chosenCities, edgeList)
    E4_Means = [myMeans[i] for i in E4_ConnectingEdges]
    E4_Stdevs = [myStdevs[i] for i in E4_ConnectingEdges]
    E4_Errors = levelError(E4_Means, E4_Stdevs, method)
    E4_Error = E4_Errors[E4_ConnectingEdges.index(E4)]
    chosenCities.add(edgeList[E4][0])
    chosenCities.add(edgeList[E4][1])

    return [E1_Error, E2_Error, E3_Error, E4_Error]


def runAnalysis(analysisInput):
    citySets = analysisInput[0]
    myDataMeans = analysisInput[1]
    myDataStdevs = analysisInput[2]
    myEdgeList = analysisInput[3]
    Index = analysisInput[4]

    method = "Ratio"

    # citySetsB = [["Boston", "Cambridge", "Somerville", "Waltham", "Malden"]]
    # citySetsW = [["Springfield", "Lowell", "Fall River", "Somerville", "Taunton"]]

    citySetAssociated = []
    citySetMSTErrors = []
    citySetMSTEdges = []
    citySetTotalErrors = []
    count = 0

    # Get all Edge 1 Errors
    E1_Errors = levelError(myDataMeans, myDataStdevs, method)
    for citySet in citySets:
        for city in citySet:
            mstEdges = getMSTEdges(city, citySet, myEdgeList)
            edgeErrors = getEdgeOrderError(mstEdges, E1_Errors, method, myEdgeList, myDataMeans, myDataStdevs)
            pCList = [1 - pE for pE in edgeErrors]
            pC = 1
            for pci in pCList:
                pC = pC * pci
            pE = 1 - pC

            citySetAssociated.append(citySet)
            citySetMSTEdges.append(mstEdges)
            citySetMSTErrors.append(edgeErrors)
            citySetTotalErrors.append(pE)
        count += 1
        sys.stdout.write(str(Index) + ": " + str(count) + "\n")
        sys.stdout.flush()
    return [citySetAssociated, citySetMSTErrors, citySetMSTEdges, citySetTotalErrors]


def read_data():
    inDict_Packet_Times = pickle.load(open("Results/Mean_Stdev_Data_Packet_Time_Encrypted.p", "rb"))
    edgeList = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cityList = pickle.load(open("Results/Cities.p", "rb"))
    return inDict_Packet_Times, edgeList, cityList


def setup_analysis(processes=4, display=True):
    """Identify the overlap worst-case secret
    Note: time expected in milliseconds"""

    inDict_Packet_Times, edgeList, cityList = read_data()
    data_means = inDict_Packet_Times["means"]
    data_stdevs = inDict_Packet_Times["stdevs"]
    citySets = getCombinations(cityList, 5)
    analysisInputs = []
    subset = [0, 13282, 26564, 39846, len(citySets)]
    for i in range(len(subset) - 1):
        citySet_i = citySets[subset[i]:subset[i + 1]]
        analysisInput = [citySet_i, data_means, data_stdevs, edgeList, i]
        analysisInputs.append(analysisInput)

    pool = mp.Pool(processes)
    results = pool.map_async(runAnalysis, analysisInputs)
    pool.close()
    pool.join()
    pool.terminate()

    citySetAssociated = []
    citySetMSTErrors = []
    citySetMSTEdges = []
    citySetTotalErrors = []
    for res in results._value:
        citySetAssociated += res[0]
        citySetMSTErrors += res[1]
        citySetMSTEdges += res[2]
        citySetTotalErrors += res[3]

    if display:
        print("Worst Case")
        print("Error: " + str(max(citySetTotalErrors)))
        print("Cities: " + str(citySetAssociated[citySetTotalErrors.index(max(citySetTotalErrors))]))
        print("\nBest Case\n")
        print("Error: " + str(min(citySetTotalErrors)))
        print("Cities: " + str(citySetAssociated[citySetTotalErrors.index(min(citySetTotalErrors))]))

    # Store results
    res_dict = {"Worst_Case_City_Combinations": citySetAssociated,
                "Worst_Case_Total_Errors": citySetTotalErrors,
                "Worst_Case_MST_Edges": citySetMSTEdges,
                "Worst_Case_MST_Errors": citySetMSTErrors}
    pickle.dump(res_dict, open("Results/Worst_Case_Results/Worst Case Edge_by_Edge.p", "wb"))
    with open('Results/Worst_Case_Results/Worst Case Edge_by_Edge.txt', 'w') as file:
        file.write("Worst Case\n")
        file.write("Error: " + str(max(citySetTotalErrors)) + "\n")
        file.write("Cities: " + str(citySetAssociated[citySetTotalErrors.index(max(citySetTotalErrors))]) + "\n")
        file.write("\nBest Case\n")
        file.write("Error: " + str(min(citySetTotalErrors)) + "\n")
        file.write("Cities: " + str(citySetAssociated[citySetTotalErrors.index(min(citySetTotalErrors))]) + "\n")


if __name__ == "__main__":
    setup_analysis()

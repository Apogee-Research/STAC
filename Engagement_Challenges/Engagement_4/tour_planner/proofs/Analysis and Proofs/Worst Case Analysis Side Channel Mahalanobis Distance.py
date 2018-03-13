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
import itertools
from operator import itemgetter
import time
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


def runAnalysis(rAI):
    mstIndex = rAI[0]
    mstVectors = rAI[1]

    myMstVector = mstVectors[mstIndex]
    myMstMeanVec = myMstVector[1]
    myMstStdevVec = myMstVector[2]
    myMstCityVec = myMstVector[3]
    distCount_1S = 0
    distCount_0_1S = 0

    for mstVector in mstVectors:
        mstMeanVec = mstVector[1]
        Mah_Dist = sum(
            [(myMstMeanVec[i] - mstMeanVec[i]) ** 2 / myMstStdevVec[i] for i in range(len(myMstMeanVec))]) ** (0.5)
        if Mah_Dist <= 2:
            distCount_1S += 1
        if Mah_Dist <= 0.632:
            distCount_0_1S += 1

    sys.stdout.write(str(mstIndex + 1) + "\n")
    sys.stdout.flush()
    return [distCount_1S, distCount_0_1S, myMstCityVec]


def read_data():
    inDict_Packet_Times = pickle.load(open("Results/Mean_Stdev_Data_Packet_Time_Encrypted.p", "rb"))
    edgeList = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cityList = pickle.load(open("Results/Cities.p", "rb"))
    return inDict_Packet_Times, edgeList, cityList


def setup_analysis(processes=4, display=True, test=False):
    """Identify the worst-case secret by Mahalanobis distance metric for all pairs of MSTs
    multivarate Gaussian of one MST to mean vector of other MST
    Note: time expected in milliseconds"""

    inDict_Packet_Times, edgeList, cityList = read_data()
    edgeMeans = inDict_Packet_Times["means"]
    edgeStdevs = inDict_Packet_Times["stdevs"]

    mstVectors = buildMSTVectors(edgeMeans, edgeStdevs, edgeList, cityList)
    analysisInputs = [[i, mstVectors] for i in range(len(mstVectors))]

    if test:
        ta = time.time()
        res = runAnalysis(analysisInputs[0])
        tb = time.time()
        print(res)
        print(tb - ta)

    pool = mp.Pool(processes)
    results = pool.map_async(runAnalysis, analysisInputs)
    pool.close()
    pool.join()
    pool.terminate()
    output = []
    for res in results._value:
        output.append(res)
    output_1S = sorted(output, key=itemgetter(0))
    output_0_1S = sorted(output, key=itemgetter(1))

    if display:
        print("1 Sigma")
        print("Best Case:")
        print(output_1S[0][0], output_1S[0][2])

        print("Worst Case:")
        print(output_1S[-1][0], output_1S[-1][2])

        print("-------------------\n")
        print("0.1 Sigma")
        print("Best Case:")
        print(output_0_1S[0][1], output_0_1S[0][2])

        print("Worst Case:")
        print(output_0_1S[-1][1], output_0_1S[-1][2])

    # Store results
    pickle.dump(output, open("Results/Worst_Case_Results/Worst Case MahD.p", "wb"))
    with open('Results/Worst_Case_Results/Worst Case MahD.txt', 'w') as file:
        file.write("1 Sigma\n")
        file.write("Best Case:\n")
        file.write(str(output_1S[0][0]) + " " + str(output_1S[0][2]) + "\n")
        file.write("\nWorst Case:\n")
        file.write(str(output_1S[-1][0]) + " " + str(output_1S[-1][2]) + "\n")
        file.write("-------------------\n")
        file.write("0.1 Sigma\n")
        file.write("Best Case:\n")
        file.write(str(output_0_1S[0][1]) + " " + str(output_0_1S[0][2]) + "\n")
        file.write("\nWorst Case:\n")
        file.write(str(output_0_1S[-1][1]) + " " + str(output_0_1S[-1][2]) + "\n")


if __name__ == "__main__":
    setup_analysis()

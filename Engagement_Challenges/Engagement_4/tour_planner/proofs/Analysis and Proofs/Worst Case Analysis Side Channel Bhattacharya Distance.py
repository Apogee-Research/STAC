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
import sys
import numpy as np
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


def makeMatrices(mu, sig):
    # Assumes mu, sig, and x are vectors and the guassians are independent
    n = len(mu)

    muM = np.zeros((n, 1))
    varM = np.zeros((n, n))

    for ii in range(n):
        muM[ii] = mu[ii]
        varM[ii][ii] = sig[ii] ** 2
    muOut = np.matrix(muM)
    varOut = np.matrix(varM)
    return muOut, varOut


def getDB(mu_1, var_1, mu_2, var_2):
    """Calculate the Battacharya distance metric between two MST distributions
    DB =   (1/8)*Transpose(mu_1-mu_2)*Inverse(varMean)*(mu_1 - mu_2) +
           (1/2)*ln(det(varMean)/sqrt(det(var_1)*det(var_2)))
    A = Transpose(mu_1-mu_2)
    B = Inverse(varMean)
    C = (mu_1 - mu_2)
    D = det(varMean)
    E = sqrt(det(var_1)*det(var_2))
    DB = (1/8)*A*B*C+(1/2)*ln(D/E)"""

    varMean = (var_1 + var_2) / 2
    A = (mu_1 - mu_2).transpose()
    B = np.linalg.inv(varMean)
    C = (mu_1 - mu_2)
    D = np.linalg.det(varMean)
    E = np.sqrt(np.linalg.det(var_1) * np.linalg.det(var_2))
    DB = ((1 / 8) * A * B * C).item(0, 0) + (1 / 2) * np.log(D / E)
    return DB


def runAnalysis(rAI):
    mstIndex = rAI[0]
    mstVectors = rAI[1]

    myMstVector = mstVectors[mstIndex]
    myMstMeanVec = myMstVector[1]
    myMstStdevVec = myMstVector[2]
    myMstCityVec = myMstVector[3]

    myMuMatrix, myVarMatrix = makeMatrices(myMstMeanVec, myMstStdevVec)

    DBList = []
    for mstVector in mstVectors:
        mstMeanVec = mstVector[1]
        mstStdevVec = mstVector[2]
        muMatrix, varMatrix = makeMatrices(mstMeanVec, mstStdevVec)

        # Battacharyya Distance
        DB_pq = getDB(myMuMatrix, myVarMatrix, muMatrix, varMatrix)
        DBList.append(DB_pq)
    DBList.sort()
    mean = np.mean(DBList)
    my10 = DBList[5312]
    my25 = DBList[13282]
    my50 = DBList[26564]

    sys.stdout.write(str(mstIndex + 1) + "\n")
    sys.stdout.flush()
    filename = "Results/Worst_Case_Results/Status/" + str(mstIndex) + ".p"
    pickle.dump([myMstCityVec, DBList], open(filename, "wb"))
    return [mstIndex, mean, my10, my25, my50, myMstCityVec]


def read_data():
    inDict_Packet_Times = pickle.load(open("Results/Mean_Stdev_Data_Packet_Time_Encrypted.p", "rb"))
    edgeList = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cityList = pickle.load(open("Results/Cities.p", "rb"))
    return inDict_Packet_Times, edgeList, cityList


def setup_analysis(processes=4, display=True, test=False):
    """Identify the worst-case secret by Bhattacharya distance metric for all pairs of MSTs
    multivarate Gaussian of one MST to multivariate Gaussian of other MST
    Note: time expected in milliseconds"""

    inDict_Packet_Times, edgeList, cityList = read_data()
    edgeMeans = inDict_Packet_Times["means"]
    edgeStdevs = inDict_Packet_Times["stdevs"]

    mstVectors = buildMSTVectors(edgeMeans, edgeStdevs, edgeList, cityList)
    analysisInputs = [[i, mstVectors] for i in range(len(mstVectors))]

    if test:
        ta = time.time()
        res = runAnalysis(analysisInputs[41515])
        tb = time.time()
        print(res)
        print(tb - ta)

    pool = mp.Pool(processes)
    results = pool.map_async(runAnalysis, analysisInputs[:])
    pool.close()
    pool.join()
    pool.terminate()
    output = []
    for res in results._value:
        output.append(res)

    outputMean = sorted(output, key=itemgetter(1))
    output10 = sorted(output, key=itemgetter(2))
    output25 = sorted(output, key=itemgetter(3))
    output50 = sorted(output, key=itemgetter(4))

    if display:
        print("-------------------\n")
        print("Mean")
        print("Best Case:")
        print(outputMean[0][1], outputMean[0][5])

        print("Worst Case:")
        print(outputMean[-1][1], outputMean[-1][5])

        print("10")
        print("Best Case:")
        print(output10[0][2], output10[0][5])

        print("Worst Case:")
        print(output10[-1][2], output10[-1][5])

        print("25")
        print("Best Case:")
        print(output25[0][3], output25[0][5])

        print("Worst Case:")
        print(output25[-1][3], output25[-1][5])

        print("50")
        print("Best Case:")
        print(output50[0][4], output50[0][5])

        print("Worst Case:")
        print(output50[-1][4], output50[-1][5])

    # Store results
    with open("Results/Worst_Case_Results/Worst Case BhatD.txt", "w") as file:
        file.write("Mean\n")
        file.write("Best Case:\n")
        file.write(str(outputMean[0][1]) + " " + str(outputMean[0][5]) + "\n")
        file.write("\nWorst Case:\n")
        file.write(str(outputMean[-1][1]) + " " + str(outputMean[-1][5]) + "\n")
        file.write("-------------------\n")
        file.write("10\n")
        file.write("Best Case:\n")
        file.write(str(output10[0][2]) + " " + str(output10[0][5]) + "\n")
        file.write("\nWorst Case:\n")
        file.write(str(output10[-1][2]) + " " + str(output10[-1][5]) + "\n")
        file.write("-------------------\n")
        file.write("25\n")
        file.write("Best Case:\n")
        file.write(str(output25[0][3]) + " " + str(output25[0][5]) + "\n")
        file.write("\nWorst Case:\n")
        file.write(str(output25[-1][3]) + " " + str(output25[-1][5]) + "\n")
        file.write("-------------------\n")
        file.write("-------------------\n")
        file.write("50\n")
        file.write("Best Case:\n")
        file.write(str(output50[0][4]) + " " + str(output50[0][5]) + "\n")
        file.write("\nWorst Case:\n")
        file.write(str(output50[-1][4]) + " " + str(output50[-1][5]) + "\n")
        file.write("-------------------\n")
    pickle.dump(output, open("Results/Worst_Case_Results/Worst Case BhatD Redux.p", "wb"))


if __name__ == "__main__":
    setup_analysis()

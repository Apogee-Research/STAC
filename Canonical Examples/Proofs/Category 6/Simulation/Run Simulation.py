#MIT License
#
#Copyright (c) 2017 Apogee Research
#
#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:
#
#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.
#
#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.


import os
import pickle
import decimal
import numpy as np
import sys
import math
import Exploit_Strategy as Strategy


def calc_percentile(data, percentile):
    my_data = sorted(data)
    index = math.floor(percentile * len(my_data) / 100)
    val = my_data[index]
    return val


# Store Data
def store_data(data_dict, filename):
    os.chdir("Results")
    pickle.dump(data_dict, open(filename + ".p", "wb"))
    os.chdir("..")


# Set decimal precision
decimal.getcontext().prec = 20

alphabetBase = "abcdefghijklmnopqrstuvwxyz"
# Num Chars
M = 10
# Alphabet Size
S = 26
# Max Ops
maxOps = 6000
# Number of trials
trials = 1000

attack_input = {"u_1": mean_1, "s_1": std_1,
                "u_2": mean_2, "s_2": std_2}

# Base values of delta and sigma
delta = decimal.Decimal(2.5)
sigma = decimal.Decimal(0.25)

alphabet = list(alphabetBase[:S])
password = "".join([alphabet[-1] for i in range(M)])

# Seed for selecting random passwords
# Need different seed for random samples ?
np.random.seed(7982)

# Run Simulation
simulation = Strategy.threshold_max_hist
print("Simulation:", simulation)
sys.stdout.flush()

results = []
for trial in range(trials):
    print("\tTrial:", trial, "of", trials)
    sys.stdout.flush()
    results.append(simulation(password, maxOps, alphabet, delta, sigma, attack_input))

# Process results
resDict = {True: [], False: []}
resZDict = {True: [], False: []}
for result in results:
    resDict[result[1]].append(result[0])
    resZDict[result[1]].append(result[2])

# Plot results
numOpsPlotData = {"x": [], "y": []}

maxPC = math.floor(len(resDict[True]) * 100 / (len(resDict[True]) + len(resDict[False])))
for pC in np.arange(1, maxPC, 0.5):
    numOps = calc_percentile(resDict[True] + resDict[False], pC)
    numOpsPlotData["x"].append(numOps)
    numOpsPlotData["y"].append(pC)

dataFile = "Simulation_M_2_64_S_" + str(S) + "_Max_" + str(maxOps) + "_Trials_" + str(
    trials) + " Strategy " + simulation
store_data(numOpsPlotData, dataFile)

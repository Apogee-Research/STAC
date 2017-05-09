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
import matplotlib.pyplot as plt
import numpy as np


def read_data():
    # Read in data
    os.chdir("Results/Data Set Base Simulation")
    threshold_analytical = pickle.load(open("Analytical_M_16_S_26_p100_Exp_1 Strategy Th.p", "rb"))
    threshold_simulation = pickle.load(open("Simulation_M_16_S_26_Max_6000_Trials_1000 Strategy Th.p", "rb"))
    threshold_hist_simulation = pickle.load(open("Simulation_M_16_S_26_Max_6000_Trials_1000 Strategy Th_Hist.p", "rb"))
    threshold_max_simulation = pickle.load(open("Simulation_M_16_S_26_Max_6000_Trials_1000 Strategy Th_Max.p", "rb"))
    threshold_max_hist_simulation = pickle.load(
        open("Simulation_M_16_S_26_Max_6000_Trials_1000 Strategy Th_Max_Hist.p", "rb"))
    os.chdir("../..")

    data_set = {"threshold Analytical": threshold_analytical,
                "threshold Simulation": threshold_simulation,
                "thresholdHist Simulation": threshold_hist_simulation,
                "thresholdMax Simulation": threshold_max_simulation,
                "thresholdMaxHist Simulation": threshold_max_hist_simulation}
    color_set = {"threshold Analytical": "orange",
                 "threshold Simulation": "blue",
                 "thresholdHist Simulation": "green",
                 "thresholdMax Simulation": "black",
                 "thresholdMaxHist Simulation": "brown"}
    return data_set, color_set


def plot_data_num_ops(data_set, color_set):
    y_range = [0, 6000]
    x_range = [0, 100]
    size = 5

    # PC vs Num Ops A Analytical Vs Sim
    fs = 12
    fig = plt.figure()
    for dataKey in data_set.keys():
        if "Analytical" in dataKey:
            x = np.array(data_set[dataKey]['z']) * 26
            y = data_set[dataKey]['y']
            new_x = []
            new_y = []
            for i in range(len(x)):
                if len(new_y) > 0 and abs(new_y[len(new_y) - 1] - y[i]) < 0.5:
                    continue
                new_x.append(x[i])
                new_y.append(y[i])
        else:
            x = data_set[dataKey]['x']
            y = data_set[dataKey]['y']
            new_x = []
            new_y = []
            for i in range(len(x)):
                if len(new_x) > 0 and new_x[len(new_x) - 1] == x[i]:
                    continue
                if len(new_y) > 0 and abs(new_y[len(new_y) - 1] - y[i]) < 0.5:
                    continue
                new_x.append(x[i])
                new_y.append(y[i])
        plt.scatter(new_y, new_x, s=size, facecolor=color_set[dataKey], edgecolor=color_set[dataKey])
        x95 = 0
        y95 = 0
        for i in range(len(y)):
            if y[i] <= 95:
                x95 = x[i]
                y95 = y[i]
        print(dataKey, "95%", x95, y95)
    plt.plot([95, 95],[0, 8000], color="red", ls="--")
    plt.xlim(x_range)
    plt.ylim(y_range)
    plt.xticks(fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(linestyle='dotted')
    plt.ylabel("Number of Operations", fontsize=fs)
    plt.xlabel("Probability of Success %", fontsize=fs)
    os.chdir("Figures")
    fig_name = 'SC Strength Base Simulation.png'
    fig.savefig(fig_name, bbox_inches='tight', format='png', dpi=1200)
    os.chdir("..")
    plt.show()
    plt.close()


def plot_data_states(data_set, color_set):
    x_range = [0, 6000]
    y_range = [0, 100]
    size = 2

    S = 26

    # PC vs Transitions
    for dataKey in sorted(data_set.keys()):

        if "Analytical" in dataKey:
            x = data_set[dataKey]['z']
            y = data_set[dataKey]['y']
        else:
            x = np.array(data_set[dataKey]['x']) / 26
            y = data_set[dataKey]['z']
        plt.scatter(x, y, s=size, facecolor=color_set[dataKey], edgecolor=color_set[dataKey])
    plt.xlim(np.array(x_range) / S)
    plt.ylim(y_range)
    plt.xlabel("Number of State Transitions")
    plt.ylabel("Probability of Success")
    plt.show()
    plt.close()


def main():
    data_set, color_set = read_data()
    plot_data_num_ops(data_set, color_set)


main()

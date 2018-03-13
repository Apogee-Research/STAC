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
import statistics
import matplotlib.pyplot as plt
import numpy as np


def read_data(data_type="Base"):
    edge_list = pickle.load(open("Results/Order_of_Links.p", "rb"))
    cities = pickle.load(open("Results/Cities.p", "rb"))
    if data_type == "Base":
        timing_data_dict = pickle.load(open("Results/Edge Times Data Encrypted.p", "rb"))
    elif data_type == "NUC1":
        timing_data_dict = pickle.load(open("Results/Edge Times Data Encrypted NUC1.p", "rb"))
    elif data_type == "NUC2":
        timing_data_dict = pickle.load(open("Results/Edge Times Data Encrypted NUC3.p", "rb"))
    else:
        raise ValueError("Invalid data_type")
    return edge_list, cities, timing_data_dict


def get_means_and_cutoffs(timing_data_dict, print_status=False, plot_cutoffs=False, display=False):
    """Get means and standard deviations"""
    means = []
    stdevs = []
    data_d = {i: [] for i in range(0, 300)}
    for edgeIndex in sorted(timing_data_dict.keys()):
        data = [dat[0] for dat in timing_data_dict[edgeIndex] if len(dat) > 0][:-1]
        mean = statistics.mean(data)
        stdev = statistics.stdev(data)
        means.append(mean)
        stdevs.append(stdev)
        data_d[edgeIndex] = data

    # Identify Cutoff Points
    cu_means = []
    cu_stdevs = []
    cu_data_d = {i: [] for i in range(0, 300)}
    for i in range(0, 300):
        observed = np.array(data_d[i])
        stdev_o = statistics.stdev(observed)
        mean_o = np.mean(observed)

        cu = min(14, mean_o + 4 * stdev_o)
        cu_observed = [dat for dat in observed if dat < cu]
        cu_stdev = statistics.stdev(cu_observed)
        cu_mean = np.mean(cu_observed)
        cu_means.append(cu_mean)
        cu_stdevs.append(cu_stdev)
        cu_data_d[i] = cu_observed
        if print_status:
            print(i, len(observed), len(cu_observed))

        if plot_cutoffs:
            fig = plt.figure()
            if print_status:
                print("Plot Cutoffs:", i)
            plt.plot([cu, cu], [0, 100])
            plt.hist(data_d[i], bins=100)
            if display:
                plt.show()
            figure_name = 'Figures/CU/' + str(i) + '.png'
            fig.savefig(figure_name, bbox_inches='tight', format='png')
            plt.close()

    if plot_cutoffs:
        # Plot Full Cutoff Points
        bins = np.arange(3, 6.8, (6.8 - 3) / 200)
        fig = plt.figure()
        plt.hist(data_d[65], bins=bins)
        plt.xlim([3, 7])
        plt.ylim([0, 165])
        plt.xlabel("Time (ms)")
        plt.ylabel("Frequency")
        if display:
            plt.show()
        figure_name = 'Figures/Side Channel Timing Distribution Cutoff Example Edge 65.png'
        fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
        plt.close()

    # Save cutoff data
    pickle.dump({"means": cu_means, "stdevs": cu_stdevs},
                open('Results/Mean_Stdev_Data_Packet_Time_Encrypted.p', 'wb'))

    return {"Base": {"means": means, "stdevs": stdevs, "data": data_d},
            "Cutoffs": {"means": cu_means, "stdevs": cu_stdevs, "data": cu_data_d}}


def plot_modes(dataD, display=False):
    """Plot Modes"""

    fig = plt.figure()
    bins = np.arange(2.6, 3.5, (3.5 - 2.60) / 100)
    plt.hist(dataD[100], bins=bins, color='blue')
    plt.xlim([3, 7])
    plt.ylim([0, 165])
    plt.xlabel("Time (ms)")
    plt.ylabel("Frequency")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing Distribution Edge 50.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()

    bins = np.arange(3, 6.8, (6.8 - 3) / 200)
    fig = plt.figure()
    plt.hist(dataD[65], bins=bins)
    plt.hist(dataD[160], bins=bins, color='red')
    plt.xlim([3, 7])
    plt.ylim([0, 165])
    plt.xlabel("Time (ms)")
    plt.ylabel("Frequency")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing Distribution Modes Example Edge 65_160.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()


def plot_mean_and_stdev(mean_cutoff_data, display=False):
    # Plot Observed Mean and Standard Deviation

    means = mean_cutoff_data["Base"]["means"]
    stdevs = mean_cutoff_data["Base"]["stdevs"]
    cu_means = mean_cutoff_data["Cutoffs"]["means"]
    cu_stdevs = mean_cutoff_data["Cutoffs"]["stdevs"]

    x = list(range(0, 300))

    # Mean
    fig = plt.figure()
    plt.scatter(x, means, s=1, color="blue")
    plt.ylim([0, 12])
    plt.xlim([-2, 302])
    plt.xlabel("Sorted List Index")
    plt.ylabel("Time (ms)")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing Observed Means Encrypted.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()

    # Stdev Full
    fig = plt.figure()
    plt.scatter(x, stdevs, s=1)
    plt.ylim([0, 1.6])
    plt.xlim([-2, 302])
    plt.xlabel("Sorted List Index")
    plt.ylabel("Time (ms)")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing Observed Standard Deviations Encrypted Full.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()

    # Stdev Non Zoom
    fig = plt.figure()
    plt.scatter(x, stdevs, s=1)
    plt.ylim([0, 0.475])
    plt.xlim([-2, 302])
    plt.xlabel("Sorted List Index")
    plt.ylabel("Time (ms)")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing Observed Standard Deviations Encrypted Zoom.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()

    # Plot Cutoffs Mean and Standard Deviation
    # Mean
    print("\n****Server Order****\n")
    fig = plt.figure()
    plt.scatter(x, cu_means, s=1, color="blue")
    plt.ylim([0, 12])
    plt.xlim([-2, 302])
    plt.xlabel("Sorted List Index")
    plt.ylabel("Time (ms)")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing CU_Means Encrypted.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()

    # Stdev Full
    fig = plt.figure()
    plt.scatter(x, cu_stdevs, s=1)
    plt.ylim([0, .065])
    plt.xlim([-2, 302])
    plt.xlabel("Sorted List Index")
    plt.ylabel("Time (ms)")
    if display:
        plt.show()
    figure_name = 'Figures/Side Channel Timing CU_Standard Deviations Encrypted Full.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()


def main():
    """Perform initial analysis of edge timing data
    Necessary first step in running Guess Tour analysis"""
    edge_list, cities, timing_data_dict = read_data()
    mean_cutoff_data = get_means_and_cutoffs(timing_data_dict)
    plot_modes(mean_cutoff_data["Base"]["data"])
    plot_mean_and_stdev(mean_cutoff_data)


if __name__ == "__main__":
    main()

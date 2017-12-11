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
import matplotlib.pyplot as plt
import math
import numpy as np


def read_data(filename):
    """Read in file"""
    with open(filename, "rb") as file:
        return pickle.load(file)


def calc_percentile(data, percentile):
    my_data = sorted(data, reverse=True)
    index = int(math.floor(percentile * len(my_data) / 100))
    val = my_data[index]
    return val


def get_bins(my_data, a, b, n):
    """Get plot data"""
    bins = np.arange(a, b + (b - a) / n, (b - a) / n)
    h = plt.hist(my_data, bins=bins)
    plt.close()
    x = h[1][:-1]
    y = h[0]
    return [x, y]


def find_nearest_index(my_data, value):
    """Get nearest index value"""
    i = 0
    for j in range(len(my_data)):
        if abs(my_data[j] - value) < abs(my_data[j] - my_data[i]):
            i = j
    return i


def plot_data(data, fig_name, display, plot_type="Histogram"):
    assert (plot_type in ["Histogram", "Cumulative"])

    mal_x = []
    mal_y = []
    ben_x = []
    ben_y = []
    mal_data = [dat["Worst"] for dat in data]
    ben_data = [dat["Avg"] for dat in data]
    n = round(len(mal_data) / 25)
    size = 1

    if plot_type == "Histogram":
        # Benign Plot
        ben_hist = get_bins(ben_data, min(ben_data), max(ben_data[1:]), n)
        fig = plt.figure()
        plt.plot(ben_hist[0], ben_hist[1] / len(ben_data), c='b', lw=size)
        plt.plot([np.median(ben_data), np.median(ben_data)], [0, 2200], c='purple', ls='--', lw=1)
        plt.xlim([0, 2])
        plt.ylim([0, .25])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentage of Benign Runs")
        if display:
            plt.show()
        fig.savefig(fig_name + " Benign.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        # Malicious Plot
        mal_hist = get_bins(mal_data, min(mal_data), max(mal_data), n)
        fig = plt.figure()
        plt.plot(mal_hist[0], mal_hist[1] / len(mal_data), c='r', lw=size)

        # Shade Vulnerable Area
        n_i = find_nearest_index(mal_hist[0], 750)
        plt.fill_between(mal_hist[0][n_i:], [0 for i in mal_hist[1][n_i:]], mal_hist[1][n_i:] / len(mal_data),
                         color='grey', alpha='0.5')

        plt.plot([np.median(mal_data), np.median(mal_data)], [0, 140], c='purple', ls='--', lw=1)
        plt.plot([750, 750], [0, 140], c='black', ls='-', lw=1)
        plt.xlim([0, 2000])
        plt.ylim([0, .014])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentage of Malicious Runs")
        if display:
            plt.show()
        fig.savefig(fig_name + " Malicious.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        print("Result: Median\n\tAverage Input: ", np.median(ben_data))
        print("\tMalicious Input: ", np.median(mal_data))

    elif plot_type == "Cumulative":
        mal_50 = 0
        ben_50 = 0
        for percentile in np.arange(1, 100, 0.5):
            mal_runtime = calc_percentile(mal_data, percentile)
            mal_x.append(mal_runtime)
            mal_y.append(percentile)
            ben_runtime = calc_percentile(ben_data, percentile)
            ben_x.append(ben_runtime)
            ben_y.append(percentile)
            if percentile == 50:
                mal_50 = mal_runtime
                ben_50 = ben_runtime

        # Benign Plot
        fig = plt.figure()
        plt.plot(ben_x, ben_y, c='b', lw=size)
        plt.plot([ben_50, ben_50], [0, 102], c='b', ls='--', lw=1)
        plt.xlim([0, 1.1])
        plt.ylim([0, 100])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentile of Benign Runs")
        if display:
            plt.show()
        fig.savefig(fig_name + " Benign.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        # Malicious Plot
        fig = plt.figure()
        plt.plot(mal_x, mal_y, c='r', lw=size)
        plt.plot([mal_50, mal_50], [0, 102], c='r', ls='--', lw=1)
        plt.plot([900, 900], [0, 102], c='k', ls='--', lw=1)
        plt.plot([0, 2000], [50, 50], c='orange', ls='--', lw=1)
        plt.xlim([0, 2000])
        plt.ylim([0, 100])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentile of Malicious Runs")
        if display:
            plt.show()
        fig.savefig(fig_name + " Malicious.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        print("Result: 50th Percentile\n\tAverage Input: ", ben_50)
        print("\tMalicious Input: ", mal_50)


def main():
    filename = "Input Data/Data.p"
    data = read_data(filename)
    plot_data(data, "Figures/Resource Usage No Network", False)


if __name__ == "__main__":
    main()

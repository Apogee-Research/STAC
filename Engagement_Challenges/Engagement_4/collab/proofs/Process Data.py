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
import numpy as np
import math
import matplotlib.pyplot as plt


def read_data(category):
    """Read Data. Categories: NV_java, V_java, NV_python, V_python"""
    if category not in ["NV_java", "V_java", "NV_python", "V_python"]:
        raise ValueError

    filename = "Input Data/combined_data_" + category + ".p"
    data = pickle.load(open(filename, "rb"))
    return data


def process_raw_data(data, print_summary=True):
    """Segment data into split versus non-split insert operations"""
    split_times = []
    non_split_times = []
    for dat in data:
        for i in range(3, len(dat), 1):
            if (i - 3) % 5 == 0:
                split_times.append(dat[i] * 10 ** -3)
            else:
                non_split_times.append(dat[i] * 10 ** -3)

    if print_summary:
        print("Samples:", len(split_times),
              "Mean Split:", np.mean(split_times), "Stdev:", np.std(split_times),
              "SM", np.std(split_times) / math.sqrt(len(split_times)))
        print("Samples:", len(non_split_times),
              "Mean No Split:", np.mean(non_split_times), "Stdev:", np.std(non_split_times),
              "SM", np.std(non_split_times) / math.sqrt(len(non_split_times)))

    return split_times, non_split_times


def plot_raw_data(program, plot_base, split_times, non_split_times, display=False):
    """Create scatter plot of data for split versus non-split insert operations"""

    fig = plt.figure()
    plt.scatter(list(range(len(split_times))), split_times, s=2, c="r", edgecolor="r")
    plt.scatter(list(range(len(split_times))), split_times, s=2, c="b", edgecolor="b")
    plt.ylim([0, 2010])
    plt.xlim([0, len(non_split_times)])
    if display:
        plt.show()
    figure_name = "Figures/" + program + " " + plot_base + '_Raw_Data_Scatter.png'
    fig.savefig(figure_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()


def plot_frequency_data(program, plot_base, split_times, non_split_times, display=False):
    """Create histograms and frequency plots for split versus non-split insert operations"""
    font_size = 12

    a = 0
    b = 2010
    w = 75

    # Plot histograms
    fig1 = plt.figure()
    ns_h = plt.hist(non_split_times, bins=np.arange(a, b, (b - a) / w), color="blue", normed=True)
    s_h = plt.hist(split_times, bins=np.arange(a, b, (b - a) / w), color="red", normed=True)
    plt.xticks(fontsize=font_size)
    plt.yticks(fontsize=font_size)
    plt.xlabel("Server Response Time (microseconds)", fontsize=font_size)
    plt.ylabel("Normalized Frequency", fontsize=font_size)
    if display:
        plt.show()
    figure1_name = "Figures/" + program + " " + plot_base + '_Histogram.png'
    fig1.savefig(figure1_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()

    # Plot line plot of histogram data
    fig2 = plt.figure()
    plt.plot(ns_h[1][1:], ns_h[0], color="blue")
    plt.plot(s_h[1][1:], s_h[0], color="red")
    plt.xticks(fontsize=font_size)
    plt.yticks(fontsize=font_size)
    plt.xlabel("Server Response Time (microseconds)", fontsize=font_size)
    plt.ylabel("Normalized Frequency", fontsize=font_size)
    if display:
        plt.show()
    figure2_name = "Figures/" + program + " " + plot_base + '_Histogram_Plot.png'
    fig2.savefig(figure2_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()


def main():
    plot_base = "Not Vulnerable"
    program = "Java"
    category = "NV_java"
    data = read_data(category)
    split_times, non_split_times = process_raw_data(data)
    plot_raw_data(program, plot_base, split_times, non_split_times)
    plot_frequency_data(program, plot_base, split_times, non_split_times)


if __name__ == "__main__":
    main()

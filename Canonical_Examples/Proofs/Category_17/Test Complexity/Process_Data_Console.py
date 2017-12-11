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

import matplotlib.pyplot as plt
import math
import numpy as np


def read_data(filename):
    """Read in file"""
    with open(filename, "r") as file:
        return [int(line.strip()) for line in file.readlines()]


def calc_percentile(data, percentile):
    my_data = sorted(data, reverse=True)
    index = int(math.floor(percentile * len(my_data) / 100))
    val = my_data[index]
    return val


def plot_data(data, color):
    x = []
    y = []
    size = 1

    for percentile in np.arange(1, 100, 0.5):
        runtime = calc_percentile(data, percentile)
        x.append(runtime)
        y.append(percentile)
    plt.plot(x, y, color, lw=size)


def main(display=False):
    mal_file = "Input Data/Mal_Data.txt"
    ben_file = "Input Data/Ben_Data.txt"
    mal_data = read_data(mal_file)
    ben_data = read_data(ben_file)

    fig = plt.figure()
    plot_data(ben_data, 'b')
    plot_data(mal_data, 'r')
    plt.plot([800, 800], [0, 102], ls='--', lw=1)
    plt.plot([0, 2000], [50, 50], ls='--', lw=1)
    plt.plot([980, 980], [0, 102], ls='--', lw=1, c='black')
    plt.xlim([0, 2000])
    plt.ylim([0, 100])
    plt.grid()
    plt.xlabel("Runtime (ms) [Target Resource Usage Limit]")
    plt.ylabel("Percentile of Mal Inputs")
    if display:
        plt.show()
    fig_name = "Figures/Resource Usage No Network.png"
    fig.savefig(fig_name, bbox_inches='tight', format='png', dpi=1200)
    plt.close()


if __name__ == "__main__":
    main(False)

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
from sklearn.mixture import GMM
import Analysis_Methods


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


def process_base_data(timing_data_dict):
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
    return data_d


def model_mixed_gaussian(data_d, print_status=False, display=False):
    """Attempt to model data with mixed Gaussian distribution"""

    cu_means = []
    cu_stdevs = []
    cu_data_d = {i: [] for i in range(0, 300)}
    bins = np.arange(0.5, 11.85, (11.85 - 0.5) / 4500)
    mixed_means = []
    mixed_weights = []
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

        X = np.array([np.array([i2]) for i2 in sorted(cu_data_d[i])])
        model = GMM(n_components=2, n_iter=100, n_init=2).fit(X)

        AIC = model.aic(X)
        BIC = model.bic(X)
        mixed_mean = [model.means_[0][0], model.means_[1][0]]
        mixed_cov = [model.covars_[0][0], model.covars_[1][0]]
        mixed_weight = [model.weights_[0], model.weights_[1]]
        mixed_means.append(mixed_mean)
        mixed_cov.append(mixed_cov)
        mixed_weights.append(mixed_weight)

        # Plot Cutoff Points
        fig = plt.figure()
        a = min(cu_data_d[i])
        b = max(cu_data_d[i])
        d = (b - a) / 1000
        gauss_x = np.arange(a, b + d, d)
        m_gauss_y = [mixed_weight[0] * Analysis_Methods.pGaussian(mixed_mean[0], mixed_cov[0], gx) +
                     mixed_weight[1] * Analysis_Methods.pGaussian(mixed_mean[1], mixed_cov[1], gx) for gx in gauss_x]
        u_gauss_y = [Analysis_Methods.pGaussian(cu_mean, cu_stdev, gx) for gx in gauss_x]

        h = plt.hist(cu_data_d[i], bins=bins, color="blue", zorder=-1)
        plt.scatter(gauss_x, u_gauss_y, s=1, color="black", zorder=0)
        plt.scatter(gauss_x, m_gauss_y, s=1, color="red", zorder=1)
        plt.xlim([min(cu_data_d[i]), max(cu_data_d[i])])
        plt.ylim([0, max(max(m_gauss_y), max(u_gauss_y), max(h[0]))])
        if display and print_status:
            print("Sample:", i)
            plt.show()
        figure_name = 'Figures/Mixed Gauss' + str(i) + '.png'
        fig.savefig(figure_name, bbox_inches='tight', format='png')
        plt.close()


def main():
    """Experimentation with replacing gaussian model with mixed gaussian model"""
    edge_list, cities, timing_data_dict = read_data()
    data_d = process_base_data(timing_data_dict)
    model_mixed_gaussian(data_d)


if __name__ == "__main__":
    main()

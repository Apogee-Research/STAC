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

import numpy as np


def makeMatrices(mu, sig, x):
    # Assumes mu, sig, and x are vectors and the guassians are independent
    if not len(mu) == len(sig) or not len(sig) == len(x):
        print('Vectors need to be the same length')
        return -1
    else:
        n = len(mu)

        muM = np.zeros((n, 1))
        sigM = np.zeros((n, n))
        xM = np.zeros((n, 1))

        for ii in range(n):
            muM[ii] = mu[ii]
            sigM[ii][ii] = sig[ii]
            xM[ii] = x[ii]
        mu = np.matrix(muM)
        sig = np.matrix(sigM)
        x = np.matrix(xM)
        return mu, sig, x


def nvarGaussian(mu, sig, x):
    # Assumes mu, sig, and x are all np.matrices of appropriate size
    sig = sig * sig  # Need variance, not std dev
    sigD = np.linalg.det(sig)
    sigI = np.linalg.inv(sig)
    k = mu.shape[0]
    xmudiff = x - mu
    insides = -1 / 2 * xmudiff.transpose(1, 0) * sigI * xmudiff
    f = 1 / np.sqrt(((2 * np.pi) ** k) * sigD) * np.exp(insides)
    return float(f * np.sqrt(((2 * np.pi) ** k) * sigD))

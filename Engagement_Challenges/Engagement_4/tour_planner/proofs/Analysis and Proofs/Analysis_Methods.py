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

import math


def plotData(means_in, stdevs_in, description):
    import matplotlib.pyplot as plt
    x = list(range(0, len(means_in)))
    print("\n****", description, "****\n")
    plt.scatter(x, means_in, s=1, color="blue")
    plt.ylim([0, 0.0126])
    plt.xlim([-2, 302])
    plt.show()
    plt.close()
    plt.scatter(x, stdevs_in, s=1)
    plt.ylim([0, 0.0004])
    plt.xlim([-2, 302])
    plt.show()
    plt.close()


def plotError(y_in, description, color):
    import matplotlib.pyplot as plt
    x_in = list(range(0, len(y_in)))
    print("\n****", description, "****\n")
    plt.scatter(x_in, y_in, s=1, color=color)
    plt.xlim([-2, 302])
    plt.ylim([0, 1])
    plt.ylabel("Incorrect Probability")
    plt.xlabel("Index")
    plt.show()
    plt.close()


def erf(z_in):
    """Custom erf implementation"""
    z = abs(z_in)
    a1 = 0.278393
    a2 = 0.230389
    a3 = 0.000972
    a4 = 0.078108
    value = 1 - (1 / ((1 + a1 * z + a2 * z ** 2 + a3 * z ** 3 + a4 * z ** 2) ** 4))
    if z_in >= 0:
        return value
    else:
        return -1 * value


def pGaussian(u, s, t):
    """Get points from Gaussian distribution"""
    return math.exp(-(t - u) ** 2 / (2 * s ** 2)) / (s * math.sqrt(2 * math.pi))


def integratePGaussian(u, s, a, b):
    z_a = (a - u) / (math.sqrt(2) * s)
    z_b = (b - u) / (math.sqrt(2) * s)
    return (0.5) * (math.erf(z_b) - math.erf(z_a))


def calcIntersectionT(u_1, s_1, u_2, s_2):
    """Calculate the intersection point of two Gaussian distributions"""
    if s_1 != s_2:
        A = (s_2 ** 2 - s_1 ** 2)
        B = 2 * (u_2 * s_1 ** 2 - u_1 * s_2 ** 2)
        C = u_1 ** 2 * s_2 ** 2 - u_2 ** 2 * s_1 ** 2
        D = 2 * s_2 ** 2 * s_1 ** 2 * math.log(s_2 / s_1)
        const = C - D
        t_1 = (-B + math.sqrt(B ** 2 - 4 * A * const)) / (2 * A)
        t_2 = (-B - math.sqrt(B ** 2 - 4 * A * const)) / (2 * A)
        if min(u_1, u_2) < t_1 and max(u_1, u_2) > t_1:
            return t_1
        if min(u_1, u_2) < t_2 and max(u_1, u_2) > t_2:
            return t_2
        return (u_1 + u_2) / 2  # 1000000
    elif u_1 != u_2:
        t_1 = (u_2 ** 2 - u_1 ** 2) / (2 * (u_2 - u_1))
        return t_1
    return 1000000


def findClosestIntersections(index, m, s, intersects):
    """Find closest intersection point to given index"""

    def getIndex(near, match, list_in, direction):
        matches = [i for i in range(len(list_in)) if list_in[i] == match]
        if direction == 'left':
            dist = [near - i for i in matches]
            return matches[dist.index(max(dist))]
        elif direction == 'right':
            dist = [i - near for i in matches]
            return matches[dist.index(min(dist))]

    ordered = sorted(intersects)
    if index == 0 or index == len(intersects) - 1:
        dist = [abs(i - m) for i in intersects]
        return [dist.index(min(dist))]

    for i in range(len(ordered)):
        t_1 = ordered[i]
        t_2 = ordered[i + 1]
        if t_1 < m and t_2 > m:
            return [intersects.index(t_1), intersects.index(t_2)]
    return 'NaN'


def findChains(index, a_index, b_index, intersects_all, means_in, stdevs_in):
    """Find chains"""
    if a_index == "NaN":
        a = 0
        a_mean = means_in[index]
        a_stdev = stdevs_in[index]
        b = intersects_all[index][b_index]
        b_mean = means_in[b_index]
        b_stdev = stdevs_in[b_index]
        midPoint = 10000000000000000
    elif b_index == "NaN":
        a = intersects_all[index][a_index]
        a_mean = means_in[a_index]
        a_stdev = stdevs_in[a_index]
        b = 14000000  # 2
        b_mean = means_in[index]
        b_stdev = stdevs_in[index]
        midPoint = 10000000000000000
    else:
        a = intersects_all[index][a_index]
        a_mean = means_in[a_index]
        a_stdev = stdevs_in[a_index]
        b = intersects_all[index][b_index]
        b_mean = means_in[b_index]
        b_stdev = stdevs_in[b_index]
        midPoint = intersects_all[a_index][b_index]

    # Find all intersections, t of distributions of list of pairs a < t < b    
    t_d_List = []
    for i1 in range(len(intersects_all)):
        for i2 in range(i1):
            if intersects_all[i1][i2] > a and intersects_all[i1][i2] < b:
                if means_in[i1] < means_in[i2]:
                    t_d_List.append([intersects_all[i1][i2], i1])
                else:
                    t_d_List.append([intersects_all[i1][i2], i2])

    # Sort intersections, t by value of t
    t_d_List.sort(key=lambda x: x[0])
    t_List = [i[0] for i in t_d_List]
    d_List = [i[1] for i in t_d_List]

    # Starting at a, check to see if P(X=t_candidate |mean[prev], stdev[prev]) >
    # P(X=t_previous |mean[prev], stdev[prev]) if so add it to the final list
    final_T_List = [a]
    final_D_List = [[a_mean, a_stdev]]
    pastMidPoint = False
    for i in range(len(t_List)):
        t_val = t_List[i]
        d_val = d_List[i]

        # If midPoint passed
        if t_val > midPoint and not pastMidPoint:
            pastMidPoint = True
            final_T_List.append(midPoint)
            final_D_List.append([b_mean, b_stdev])

        # Get previously added distribution
        last_t = final_T_List[-1]
        mean_prev = final_D_List[-1][0]
        stdev_prev = final_D_List[-1][1]

        pt = pGaussian(mean_prev, stdev_prev, t_val)
        pCompare = pGaussian(mean_prev, stdev_prev, last_t)

        # Conditions met for adding 
        if pt > pCompare:
            final_T_List.append(t_val)
            final_D_List.append([means_in[d_val], stdevs_in[d_val]])

    # No Crossing Points found between a, midpoint, b
    # And not at the beginning of the list
    if not pastMidPoint and midPoint != 10000000000000000:
        final_T_List.append(midPoint)
        final_D_List.append([b_mean, b_stdev])
    final_T_List.append(b)

    return final_T_List, final_D_List


def pDifference(u_x, s_x, u_y, s_y, a, b):
    """Definite Integral of Difference Distribution Function -> pc"""
    s_sum = math.sqrt(s_x ** 2 + s_y ** 2)
    A = math.sqrt(2) * s_sum
    diffDist = (0.5) * (math.erf((b - u_x) / A) - math.erf((a - u_x) / A))
    return diffDist


def pErrorDiff(index, closest, intersects_all, means_in, stdevs_in):
    """Find Max Error using Difference Distribution in Guess for Given index
    a -> intersection -> b"""
    intersects = intersects_all[index]
    # If start 0 -> t_2
    if index == 0:
        a = 0
        b = intersects[closest[0]]
        correct = pDifference(means_in[index],
                              stdevs_in[index],
                              means_in[closest[0]],
                              stdevs_in[closest[0]],
                              a, b)
        incorrect = 1 - correct
        return min(max(incorrect, 0), 1)
    # If end t_1 -> 2
    elif index == len(intersects_all) - 1:
        a = intersects[closest[0]]
        b = 14000000  # 2
        correct = pDifference(means_in[index],
                              stdevs_in[index],
                              means_in[closest[0]],
                              stdevs_in[closest[0]],
                              a, b)
        incorrect = 1 - correct
        return min(max(incorrect, 0), 1)
    # Else left: t_1 -> u right: u-> t_2
    else:
        midPoint = intersects_all[closest[0]][closest[1]]
        # Left
        t_L_index = closest[0]
        a_L = intersects[t_L_index]
        b_L = midPoint  # means_in[index]
        correct_L = pDifference(means_in[index],
                                stdevs_in[index],
                                means_in[t_L_index],
                                stdevs_in[t_L_index],
                                a_L, b_L)
        # Right
        t_R_index = closest[1]
        a_R = midPoint  # means_in[index]
        b_R = intersects[t_R_index]
        correct_R = pDifference(means_in[index],
                                stdevs_in[index],
                                means_in[t_R_index],
                                stdevs_in[t_R_index],
                                a_R, b_R)
        correct = correct_R + correct_L
        # print(correct_L,correct_R,t_L_index,t_R_index)
        incorrect = 1 - correct
        return min(max(incorrect, 0), 1)


def pErrorDiffChains(index, closest, intersects_all, means_in, stdevs_in):
    """Find Max Error Difference Distribution with chains in Guess for Given index
    a -> chain1 -> chain2 -> ... -> chain_n -> b"""

    # Build Chain
    if index == 0:
        t_chains, d_chains = findChains(index, "NaN", closest[0],
                                        intersects_all, means_in, stdevs_in)
    elif index == len(intersects_all) - 1:
        t_chains, d_chains = findChains(index, closest[0], "NaN",
                                        intersects_all, means_in, stdevs_in)
    else:
        t_chains, d_chains = findChains(index, closest[0], closest[1],
                                        intersects_all, means_in, stdevs_in)

    # Integrate Difference Distribution of chain
    chainIntegrals = []
    for i in range(len(d_chains)):
        a = t_chains[i]
        b = t_chains[i + 1]
        lowerDist = d_chains[i]
        partIntegral = pDifference(means_in[index], stdevs_in[index],
                                   lowerDist[0], lowerDist[1],
                                   a, b)
        chainIntegrals.append(partIntegral)

    # Sum to get the Total Probability
    correct = sum(chainIntegrals)
    incorrect = 1 - correct
    return min(max(incorrect, 0), 1)


def pErrorInt(index, closest, intersects_all, means_in, stdevs_in):
    """Integrate to find Difference in Areas"""
    intersects = intersects_all[index]
    # If start 0 -> t_2
    if index == 0:
        a = 0
        b = intersects[closest[0]]
    # If end t_1 -> 2
    elif index == len(intersects_all) - 1:
        a = intersects[closest[0]]
        b = 2
    # Else left: t_1 -> u right: u-> t_2
    else:
        a = intersects[closest[0]]
        b = intersects[closest[1]]
    # Calculate ratio over interval        
    intMe = integratePGaussian(means_in[index], stdevs_in[index], a, b)
    intTotalList = []
    for i in range(len(means_in)):
        intTotalList.append(integratePGaussian(means_in[i], stdevs_in[i], a, b))
    intTotal = sum(intTotalList)
    ratio = intMe / intTotal
    correct = ratio
    incorrect = 1 - correct
    return incorrect


def pErrorIntSigma(index, numSigma, means_in, stdevs_in):
    """Integrate to find Difference in Areas"""
    myMean = means_in[index]
    myStdev = stdevs_in[index]
    mySigma = math.sqrt(myStdev)

    a = max(0, myMean - (numSigma * mySigma))
    b = myMean + (numSigma * mySigma)

    # Calculate ratio over interval        
    intMe = integratePGaussian(myMean, myStdev, a, b)
    intTotalList = []
    for i in range(len(means_in)):
        intTotalList.append(integratePGaussian(means_in[i], stdevs_in[i], a, b))
    intTotal = sum(intTotalList)
    ratio = intMe / intTotal
    correct = ratio
    incorrect = 1 - correct
    return incorrect

import math
import sys
import random
#import scipy.stats

# python generate_bad_input.py N > outputFile
# Generates a sparse, asymmetric matrix of size N and stores it in outputFile

numStates = int(sys.argv[1])

def mean(x):
	return float(sum(x)) / len(x)

def moment_m(x, m, mu):
	n = float(len(x))
	tot = 0
	for val in x:
		tot += (1/n)*((val-mu)**m)
	return tot

def moment(x,m):
	return moment_m(x,m,mean(x))

def svar(x):
	n = len(x)
	return (n/(n-1))*moment(x,2)


def skewness(x):
	m3 = moment(x, 3)
	s3 = svar(x)**1.5
	return m3/s3

def gen_matrix(n):
	m = []
	for i in range(0, n):
		s = [0] * n
		mys = ""
		for j in range(0, n):
			s[j] = random.expovariate(.1)
		m.append(s)
	return m

def print_matrix(m):
	mys = ""
	for i in range(0, len(m)):
		for j in range(0,len(m[i])):
			if (j == 0):
				mys += "{0:.15f}".format(m[i][j])
			else:
				mys += "," + ("{0:.15f}".format(m[i][j]))
		if (i != len(m) -1):
			mys += "\n"
	return mys

def check_matrix(m):
	n = len(m)
	all_skew = []
	for row in m:
		skew = skewness(row)
		all_skew.append(skew)
	s = (sum(all_skew) / len(all_skew)) - .69314718056
	k = max(n*n/2, n*n*math.exp(s))
	return k > (n*n)


while True:
	m = gen_matrix(numStates)
	if (check_matrix(m)):
		print print_matrix(m)
		break


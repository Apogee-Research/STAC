# python generate_random_cpsp_graph.py n > outputFile 
# Generates a random graph  of size n where each nodes are 
# connected with probability = .5 and stores the result in
# outputFile


import sys
import random


numStates = int(sys.argv[1])
for i in range(0, numStates):
	s = [0.000000000000] * numStates
	for j in range(numStates):
		if (i == j):
			s[j] = 1 
			continue
		if (random.randint(1,10) > 5):
			s[j] = random.random() 
	mys = ""
	for j in range(0, numStates):
		if (j == 0):
			mys += ('%.15f' % s[j])
		else:
			mys += "," + ('%.15f' % s[j])
	print mys

		





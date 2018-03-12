# python generate_good_input.py <N> > file
# Generates a dense, symmetric  matrix of size N and stores it in outputFile
 

import sys
import random
numStates = int(sys.argv[1])
for i in range(0, numStates):
	s = [0] * numStates
	for j in range(0,numStates):
		s[j] = random.random() 
	mys = ""
	for j in range(0, numStates):
		if (j == 0):
			mys += str("{0:.15f}".format(s[j]))
		else:
			mys += "," + str("{0:.15f}".format(s[j]))
	print mys


import sys
import random

# python generate_cpsp.py N > outputFile

# Generates a vector of length N signifying which nodes
# to compute shortest paths from


numStates = int(sys.argv[1])
print ",".join([str(x) for x in range(numStates)])

		





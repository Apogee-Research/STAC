import sys
import math

# Given the number of rows in a matrix
# calculates the number of rows a second
# matrix would be to be twice the size
# on disk. If the number is too large
# then it returns 1000. Alternatively, 
# generates one that is log the size 
# of the original matrix

n = float(sys.argv[1])
op = int(sys.argv[2])


# Double the first matrix size
if (op == 1):

	k = ((3872*n**2)-(176*n)+4841)**0.5 -1
	m = int(k / (44))
	if (m > 1000):
		print 1000
	else:
		print m
# Log of the first matrix size
else:
	print int(math.log(n))	


# Demonstration script to interact with web service
# python send_mul.py <local port> <inputFile1> <inputFile2> <desiredoutputFile> 
# 	Example: python send_mul.py 8080 matrix1.csv matrix2.csv matrix1_x_matrix2.csv 
#	Multiplies the matrix stored in csv format in inputFile1 by the matrix stored in
#	inputFile2 and stores the result in desiredoutputFile

import requests
import sys
import json
import time

def load_csv(filename):
	fp = open(filename)
	data = fp.read()
	fp.close()
	rows = data.split("\n")
	cols = rows[0].split(",")
	#print {"rows": len(rows) -1, "cols": len(cols) }
	return {"rows": len(rows) -1, "cols": len(cols), "matrix": data}

def write_json(filename, jobj):
	fp = open(filename, "w+")
	fp.write(json.dumps(mp))
	fp.close()

#print sys.argv
if (len(sys.argv) < 4):
	print "BAD SYNTAX: send_mul.py <local port> <inputFile1> <inputFile2> <desiredoutputFile>" 
	sys.exit(1)

matrixA = load_csv(sys.argv[2])
matrixB =  load_csv(sys.argv[3])

mp = {"operation": 1, "numberOfArguments": 2, "args": [matrixA, matrixB] }
req = json.dumps(mp) + "\n"
start = time.time()

r = requests.post(("http://127.0.0.1:" + sys.argv[1]), data=req)
stop = time.time()
print "Measured time elapsed:", (stop-start)
print "Request size:", len(req)
print "Multiply Measured,", (stop-start), ", Size,", len(req)


obj = json.loads(r.text)


fw = open(sys.argv[4], "w+")
fw.write(obj['returnValue'])
fw.close()


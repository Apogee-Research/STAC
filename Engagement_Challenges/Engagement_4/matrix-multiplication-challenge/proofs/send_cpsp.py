# Demonstration script to interact with web service to compute shortest paths
#	python send_cpsp.py <local port> <inputMatrix> <columnVector> <outputFile>
#
# Reads the graph stored in adjacency matrix inputMatrix, and computes the shortest
# paths from each node specified by columnVector to each other node in the graph.
#
# 	Example: python send_cpsp.py 8080 graph.csv interesting_nodeVector.csv results.csv


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
	print "BAD SYNTAX: python send_cpsp.py <local port> <inputMatrix> <columnVector> <outputFile>"
	sys.exit(1)

matrixA = load_csv(sys.argv[2])
matrixB = load_csv(sys.argv[3])

#{"rows": 1, "cols": 10, "matrix": "0,1,2,3,4,5,6,7,8,9"} 

mp = {"operation": 2, "numberOfArguments": 2, "args": [matrixA, matrixB] }

req = json.dumps(mp)+ "\n"
start = time.time()
r = requests.post(("http://127.0.0.1:" + sys.argv[1]), data=req)
stop = time.time()
print "CPSP Measured,", (stop-start), ", Size,", len(req)
#print "Request size:", len(req)
obj = json.loads(r.text)


fw = open(sys.argv[4], "w+")
fw.write(obj['returnValue'])
fw.close()


# Demonstration script to interact with web service to compute graph laplacians 
#	python send_laplacian.py <local port> <inputAdjacencyMatrix> <outputFile>
#
# Reads the graph stored in adjacency matrix inputMatrix, and computes the laplacian
# storing the result in outputFile
#
# 	Example: python send_laplacian.py 8080 graph.csv laplacian.csv


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
if (len(sys.argv) < 3):
	print "BAD SYNTAX: python send_laplacian.py <local port> <inputAdjacencyMatrix> <outputFile>"
	sys.exit(1)

matrixA = load_csv(sys.argv[2])


mp = {"operation": 3, "numberOfArguments": 1, "args": [matrixA] }

start = time.time()
req = (json.dumps(mp)+ "\n")
r = requests.post(("http://127.0.0.1:" + sys.argv[1]), data=req)
stop = time.time()
print "Measured time taken:", (stop-start)
print "Request size:", len(req)
print "Laplacian Measured,", (stop-start), ", Size,", len(req)
obj = json.loads(r.text)


fw = open(sys.argv[3], "w+")
fw.write(obj['returnValue'])
fw.close()

# Demonstration script to interact with web service to compute the minimum spanning
# tree of an input graph 
#	python send_mst.py <local port> <inputGraph> <outputFile>
#
# Reads the non directed graph stored in inputGraph, and computes the laplacian
# MST for the graph, storing the result in outputFile
#
# 	Example: python send_mst.py 8080 graph.csv mst.csv


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
	print "BAD SYNTAX: python send_mst.py <local port> <inputAdjacencyMatrix> <outputFile>"
	sys.exit(1)

matrixA = load_csv(sys.argv[2])

mp = {"operation": 4, "numberOfArguments": 1, "args": [matrixA] }

req = json.dumps(mp) + "\n"
start = time.time()
r = requests.post(("http://127.0.0.1:" + sys.argv[1]), data=req)
stop = time.time()
print "Measured time elapsed:", (stop-start)
print "Request size:", len(req)
print "MST Measured,", (stop-start), ", Size,", len(req)


obj = json.loads(r.text)


fw = open(sys.argv[3], "w+")
fw.write(obj['returnValue'])
fw.close()


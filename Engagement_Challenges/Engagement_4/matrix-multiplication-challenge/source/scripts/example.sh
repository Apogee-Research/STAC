#!/bin/sh
echo "Sending example shortest path calculation"
cat example-input-sp.json | curl -d @- http://localhost:8080/ > output-shortestpath.json
echo "Sending example Laplacian calculation"
cat example-input-laplacian.json | curl -d @- http://localhost:8080/ > output-laplacian.json
echo "Sending example multiplication calculation"
cat example-input-mul.json | curl -d @- http://localhost:8080/ > output-multiplication.json
echo "Sending example MST calculation"
cat example-input-mst.json | curl -d @- http://localhost:8080/ > output-mst.json


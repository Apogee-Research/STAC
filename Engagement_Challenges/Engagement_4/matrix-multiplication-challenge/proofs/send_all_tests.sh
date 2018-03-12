#!/bin/bash

rm -rf proof_data
mkdir proof_data
rm -rf proof_results
mkdir proof_results


echo "Generating bad matrix of size: $1"
python generate_bad_input.py $1 > proof_data/bad.csv
echo "Generating good matrix of size: $1"
python generate_good_input.py $1 > proof_data/good.csv
L=`python expr.py $1 2`
echo "Generating chosen shortest path vector of length: $L"
python generate_cpsp.py $L > proof_data/shortest_paths.csv
L=`python expr.py $1 1`
echo "Generating random larger graph of size: $L":
python generate_good_input.py $L > proof_data/large.csv
du -h proof_data/*
echo "==========================="
echo "Laplacian"
echo "==========================="
echo "Large:"
python send_laplacian.py $2 proof_data/large.csv proof_results/laplacian_large.csv
echo ""
echo "==========================="
echo "MST"
echo "==========================="
echo "Large:"
python send_mst.py $2 proof_data/large.csv proof_results/mst_large.csv
echo ""
echo "==========================="
echo "Shortest Paths"
echo "==========================="
echo "Large:"
python send_cpsp.py $2 proof_data/large.csv proof_data/shortest_paths.csv proof_results/sp_large.csv
echo ""
echo "==========================="
echo "Multiplication"
echo "==========================="
echo "Good:"
python send_mul.py $2 proof_data/good.csv proof_data/bad.csv proof_results/mul_good.csv
echo ""
echo "Bad:"
python send_mul.py $2 proof_data/bad.csv proof_data/good.csv proof_results/mul_bad.csv




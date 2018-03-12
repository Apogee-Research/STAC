#!/bin/sh

export PYTHONPATH="$PYTHONPATH:../../../examples"

python airplan_pathheuristic_malicious.py 127.0.0.1 8443 usr pwd routemap_pathheuristic_2.txt pathheuristic_2 bd aa cost

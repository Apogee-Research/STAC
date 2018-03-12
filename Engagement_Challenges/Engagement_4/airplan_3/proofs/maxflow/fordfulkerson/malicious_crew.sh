#!/bin/sh

export PYTHONPATH=$PYTHONPATH:../../../examples

python numCrews.py 127.0.0.1 8443 usr pwd crewmap.txt crewmap

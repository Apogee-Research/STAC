#!/bin/sh

export PYTHONPATH=$PYTHONPATH:../../../examples

python ../../../examples/airplan_client.py 127.0.0.1 8443 usr pwd upload -file lol.xml


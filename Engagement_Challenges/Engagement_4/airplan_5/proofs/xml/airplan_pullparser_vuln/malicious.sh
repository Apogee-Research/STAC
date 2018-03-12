#!/bin/sh

export PYTHONPATH=$PYTHONPATH:../../../examples

python ../../../examples/airplan_client.py 127.0.0.1 8443 indigo nn55io3J4 upload -file lol.xml


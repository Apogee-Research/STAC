#!/bin/bash

export PYTHONPATH=$PYTHONPATH:../../../examples

sleep 10

expect ac_victim.expect > ac_victim.out &

sleep 2

python ac_attack.py


#!/bin/bash

FILE="neg_attack_file"

sleep 10

python withmi_bad_compression.py localhost 9000 attacker 300 $FILE


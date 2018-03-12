#!/bin/bash

sleep 10

mkdir files

python makeZeroes.py > files/all_zeroes.txt

/usr/bin/expect file_AC_attack.expect


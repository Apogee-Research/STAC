#!/bin/bash
# -r - rerun generation of initial distribution and mean
# --env j - turn jit off
# 200 - number of samples to use when measuring timing
# 5 - number of samples to drop to let the VM warm up
# 6 - initial guess for the first bits of the secret, namely 0b110


INITIAL_GUESS=6
FAST=0xd873d624adf984

if [ "$#" -eq 1 ]; then
    if [ "$1" == "fasttest" ]; then
        INITIAL_GUESS=$FAST
    fi
fi

python guessbit.py -v -r --env j 200 5 $INITIAL_GUESS
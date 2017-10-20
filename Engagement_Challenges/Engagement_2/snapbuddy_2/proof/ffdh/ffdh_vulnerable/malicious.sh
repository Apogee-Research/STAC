#!/bin/bash
# -r - rerun generation of initial distribution and mean
# --env j - turn jit off
# 200 - number of samples to use when measuring timing
# 5 - number of samples to drop to let the VM warm up
# 5 - initial guess for the first bits of the secret, namely 0b101

INITIAL_GUESS=5
FAST=0xb4d3d00d237a56

if [ "$#" -eq 1 ]; then
    if [ "$1" == "fasttest" ]; then
        INITIAL_GUESS=$FAST
    fi
fi

python guessbit.py -v -r --env j 200 5 $INITIAL_GUESS
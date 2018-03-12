#!/bin/sh

../../../examples/login.sh usr pwd
# upload a route map
curl -s -b cookies.txt -F file=@sortedRouteMap.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

# capacity AAA to DCA
curl -s -L -b cookies.txt -F "origin=2090749135" -F "destination=508977800" -F "weight=cost" --insecure https://localhost:8443/capacity/1553932502


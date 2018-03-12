#!/bin/sh

./login.sh usr pwd

# upload a route map
curl -s -b cookies.txt -F file=@routemap.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

# upload another one
curl -s -b cookies.txt -F file=@routemap.txt -F "route_map_name=map_two" --insecure https://localhost:8443/add_route_map

# delete map_one
curl -s -L -b cookies.txt -F "routemap=1553932502" --insecure https://localhost:8443/delete_route_maps


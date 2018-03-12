#!/bin/sh

./login.sh usr pwd

# upload a route map
curl -s -b cookies.txt -F file=@routemap.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

curl -s -L -b cookies.txt --insecure https://localhost:8443/tips
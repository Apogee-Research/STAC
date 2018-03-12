#!/bin/sh

./login.sh usr pwd

# upload a route map that has too many airports and flights
curl -s -L -b cookies.txt -F file=@large_map.json -F "route_map_name=large" --insecure https://localhost:8443/add_route_map

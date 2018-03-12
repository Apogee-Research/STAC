#!/bin/sh

./login.sh usr pwd

# upload a route map
curl -s -L -b cookies.txt -F file=@test_map400.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

curl -s -b cookies.txt -F "weight-type=Cost" --insecure https://localhost:8443/map_properties/1553932502 > /dev/null



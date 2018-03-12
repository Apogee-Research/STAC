#!/bin/sh


./login.sh usr pwd

# upload a route map
curl -s -b cookies.txt -F file=@test_map.json -F "route_map_name=test" --insecure https://localhost:8443/add_route_map

# view it
curl -s -L -b cookies.txt --insecure https://localhost:8443/passenger_capacity_matrix/1553932502
#!/bin/sh

./login.sh usr pwd
# upload a route map
curl -s -b cookies.txt -F file=@test_map500.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

# edit a flight
curl -s -b cookies.txt -F "distance=1000" -F "time=4" --insecure https://localhost:8443/edit_flight/1553932502/2090749135/856475492

# view the updated route map
curl -s -b cookies.txt --insecure https://localhost:8443/route_map/1553932502

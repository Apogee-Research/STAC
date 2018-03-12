#!/bin/sh

./login.sh usr pwd
# upload a route map
curl -s -b cookies.txt -F file=@test_map400.txt -F "route_map_name=map_one" --insecure https://localhost:8443/add_route_map

# add a flight
curl -s -b cookies.txt -F "destination=256" -F "distance=3000" -F "cost=500" -F "time=4" -F "crewMembers=5" -F "weightCapacity=5000" -F "passengerCapacity=400" --insecure https://localhost:8443/add_flight/1553932502/355989640

# view the updated route map
curl -s -b cookies.txt --insecure https://localhost:8443/route_map/1553932502
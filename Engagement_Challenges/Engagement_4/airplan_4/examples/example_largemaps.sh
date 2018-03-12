#!/bin/sh


./login.sh usr pwd
# upload a route map with many airports
curl -s -L -b cookies.txt -F file=@many_airports_map.txt -F "route_map_name=many_airports" --insecure https://localhost:8443/add_route_map

# upload a route map with many flights
curl -s -L -b cookies.txt -F file=@many_flights_map.txt -F "route_map_name=many_flights" --insecure https://localhost:8443/add_route_map

# upload another route map to make sure we can handle this much data at once
curl -s -L -b cookies.txt -F file=@test_map500.txt -F "route_map_name=another_map" --insecure https://localhost:8443/add_route_map


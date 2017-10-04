#!/bin/sh
../../../examples/login.sh foo df89gy9Qw
curl -s -b cookies.txt --insecure https://localhost:8080/search?suppressTimestamp=true
curl -s -L -F search="abcd" -b cookies.txt --insecure https://localhost:8080/search
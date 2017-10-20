#!/bin/sh
../../../examples/login.sh foo df89gy9Qw
curl -s -b cookies.txt -F messageContents=@hashmap_1.txt  --insecure https://localhost:8080/newmessage/1_0
curl -s -L -b cookies.txt --insecure https://localhost:8080/thread/1_0?suppressTimestamp=true

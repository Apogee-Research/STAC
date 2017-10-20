#!/bin/sh
../../../examples/login.sh foo df89gy9Qw
curl -s -b cookies.txt -X PUT --insecure  https://localhost:8080/width/1500010
curl -s -F messageContents=@bad.input -b cookies.txt --insecure https://localhost:8080/newmessage/1_0
curl -s -L -b cookies.txt --insecure https://localhost:8080/thread/1_0?suppressTimestamp=true

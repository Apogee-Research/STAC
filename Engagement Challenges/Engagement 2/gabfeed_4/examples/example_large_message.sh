#!/bin/sh

./login.sh foo df89gy9Qw
curl -s -F messageContents=@largetext1.txt -b cookies.txt --insecure https://localhost:8080/newmessage/1_0
curl -s -L -b cookies.txt --insecure https://localhost:8080/thread/1_0?suppressTimestamp=true

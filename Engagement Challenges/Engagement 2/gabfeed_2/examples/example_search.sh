#!/bin/sh

./login.sh foo df89gy9Qw
curl -s -F messageContents="hello world" -b cookies.txt --insecure https://localhost:8080/newmessage/1_0?suppressTimestamp=true
curl -s -F messageContents="HELlo heLLo world" -b cookies.txt --insecure https://localhost:8080/newmessage/1_0
curl -s -L -F search="hello" -b cookies.txt --insecure https://localhost:8080/search

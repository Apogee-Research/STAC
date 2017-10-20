#!/bin/sh

./login.sh foo df89gy9Qw
curl -s -L -b cookies.txt -F messageContents="Here is a new chat message to add to our chat" --insecure https://localhost:8080/chat/0?suppressTimestamp=true

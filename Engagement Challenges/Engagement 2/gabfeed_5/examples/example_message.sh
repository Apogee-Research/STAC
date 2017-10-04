#!/bin/sh

./login.sh foo df89gy9Qw
python add_messages.py 127.0.0.1 8080 johndoe BI39sdhgio2 95
curl -s -L -b cookies.txt --insecure https://localhost:8080/thread/1_1?suppressTimestamp=true

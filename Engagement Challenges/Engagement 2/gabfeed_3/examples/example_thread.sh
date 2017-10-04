#!/bin/sh

./login.sh foo df89gy9Qw
curl -s -F threadName="gabfeed is awesome" -F messageContents="on this thread we can talk about how much we like gabfeed" -b cookies.txt --insecure https://localhost:8080/newthread/1
curl -s -L -b cookies.txt --insecure https://localhost:8080/thread/1_2?suppressTimestamp=true

#!/bin/sh
set timeout -1

/usr/bin/expect logging_dc.expect > logging_dc.out &

sleep 10

python powerbroker_client.py -c True 127.0.0.1 9005 9006
python powerbroker_client.py 127.0.0.1 9005 9007

wait
sleep 1

cat logging_dc.out


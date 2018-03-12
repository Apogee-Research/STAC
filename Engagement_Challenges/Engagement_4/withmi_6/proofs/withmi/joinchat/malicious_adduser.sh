#!/bin/sh

# step 1, start tcpdump
/usr/sbin/tcpdump -i lo -w addusers.pcap tcp port 9002 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# start the users
/usr/bin/expect sally.expect > sally.out &
SALLY_PID=$!
sleep 1

/usr/bin/expect deven.expect > deven.out &
DEVEN_PID=$!
sleep 1

/usr/bin/expect megan.expect > megan.out &
MEGAN_PID=$!
sleep 10

/usr/bin/expect joe.expect > joe.out &
JOE_PID=$!

wait $SALLY_PID $DEVEN_PID $MEGAN_PID $JOE_PID

sleep 1

# stop tcpdump
kill $TCPDUMP_PID > /dev/null

python chat_attack.py 127.0.0.1 9002 addusers.pcap --db vulnerable.db


#!/bin/sh

# this script is to demonstrate that merely looking at ports and ips sending messages to one another is
# insufficient establish how many users are in a chat

# step 1, start tcpdump
/usr/sbin/tcpdump -i lo -w threeway.pcap tcp port 9002 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# a user has a joint chat with two others
/usr/bin/expect one_three_way.sh > three_way.out &
PID=$!
sleep 1

wait $PID

sleep 1

# stop tcpdump
kill $TCPDUMP_PID > /dev/null

# clean up
rm -rf sally/
rm -rf megan/
rm -rf deven/

# start tcpdump
/usr/sbin/tcpdump -i lo -w twoway.pcap tcp port 9002 > /dev/null &
TCPDUMP_PID=$!
sleep 1


# a user has two separate chats with two users
/usr/bin/expect two_two_way.sh > two_way.out &
PID=$!
sleep 1

wait $PID

sleep 1

# stop tcpdump
kill $TCPDUMP_PID > /dev/null

# show looking at ips and ports is identical
python flawed_chat_attack.py 127.0.0.1 9002 threeway.pcap twoway.pcap

# show our attack can distinguish
python chat_attack.py 127.0.0.1 9002 threeway.pcap --db vulnerable.db

python chat_attack.py 127.0.0.1 9002 twoway.pcap --db vulnerable.db


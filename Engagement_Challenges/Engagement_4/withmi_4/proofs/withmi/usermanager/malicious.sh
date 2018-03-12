#!/bin/sh

Run_And_Capture_Conversation() {
    CONVERSATION_STR=$1
    PCAP_NAME=$2
    PORT=$3
    SERVER_SCRIPT=$4
    CLIENT_SCRIPT=$5
    echo "Start eavesdropping on $CONVERSATION_STR"
    /usr/sbin/tcpdump -i lo -w $PCAP_NAME tcp port $PORT > /dev/null &
    TCPDUMP_PID=$!
    sleep 5

    # start the victim and the unknown user
    /usr/bin/expect $SERVER_SCRIPT > server.out &
    SERVER_PID=$!
    sleep 1
    /usr/bin/expect $CLIENT_SCRIPT > client.out &
    CLIENT_PID=$!

    wait $SERVER_PID $CLIENT_PID

    # stop tcpdump
    sleep 1
    echo "Stop eavesdropping on $CONVERSATION_STR"
    kill $TCPDUMP_PID > /dev/null
}

# step one, gather statistics for how long it takes to connect to known and unknown users

for i in $(seq 10)
do
    # first connect to an unknown user
    Run_And_Capture_Conversation "unknown users $i" unknown$i.pcap 9002 test_cases/sally.expect test_cases/megan.expect

    # then connect to a known user
    Run_And_Capture_Conversation "known users $i" known$i.pcap 9002 test_cases/sally.expect test_cases/megan.expect

    # remove the previous_users file
    rm test_cases/sally/previous_users.txt test_cases/megan/previous_users.txt
done

# start the actual test. We will test the victim connecting to two users. The victim should know the first user
# but not the second

# victim connects to the first user
Run_And_Capture_Conversation "victim and first user" conversation1.pcap 9002 known.expect victim.expect

# delete the victim's previous_users file to ensure the side channel is the worst-case scenario
rm victim/previous_users.txt

# victim connects to the second user
Run_And_Capture_Conversation "victim and second user" conversation2.pcap 9002 unknown.expect victim.expect

# the attacker analyzes the packets
python connections_attack.py 127.0.0.1 9002 9002 conversation1.pcap conversation2.pcap


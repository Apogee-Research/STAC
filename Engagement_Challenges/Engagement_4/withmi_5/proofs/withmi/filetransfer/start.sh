#!/usr/bin/expect
set timeout -1

spawn ../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ./victim -i ../../../examples/megan.id 

expect {WithMi>}
send "\n"
expect "WithMi>"

expect "all done"
expect {WithMi>}

# give test time to check disk usage
sleep 60

send "exit\n"

expect "Closing connections..."


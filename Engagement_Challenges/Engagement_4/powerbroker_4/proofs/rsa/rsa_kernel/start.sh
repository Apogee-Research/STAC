#!/usr/bin/expect
set timeout -1

spawn ../../../challenge_program/bin/powerbroker -i ../../../challenge_program/data/detroit/detroit.id
expect {PowerBroker>}
send "begin connectionlist.txt detroit_profile.json\n"

expect "Never gonna happen"


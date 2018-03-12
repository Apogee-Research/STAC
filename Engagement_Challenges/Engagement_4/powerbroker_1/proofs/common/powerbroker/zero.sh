#!/bin/sh
set timeout -1

/usr/bin/expect user.expect ../../../challenge_program/data/nyc/nyc.id profile_zero.json ../../../examples/connectionlist2.txt > zero_nyc.out &
sleep 1
/usr/bin/expect user.expect ../../../challenge_program/data/la/la.id profile_one.json ../../../examples/connectionlist2.txt > zero_la.out &

wait
sleep 1
cat zero_*.out


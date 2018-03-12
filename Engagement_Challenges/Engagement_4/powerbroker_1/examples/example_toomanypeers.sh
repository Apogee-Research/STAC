#!/bin/sh
set timeout -1

/usr/bin/expect user.expect ../challenge_program/data/detroit/detroit.id profile1.json connectionlist_long.txt > tmp_detroit.out&
sleep 1
/usr/bin/expect user.expect ../challenge_program/data/la/la.id profile2.json connectionlist_long.txt > tmp_la.out&
sleep 1
/usr/bin/expect user.expect ../challenge_program/data/nyc/nyc.id profile3.json connectionlist_long.txt > tmp_nyc.out&
sleep 1
/usr/bin/expect dropout.expect ../challenge_program/data/rochester/rochester.id profile1.json connectionlist_long.txt > tmp_rochester.out&
sleep 1
/usr/bin/expect dropout.expect ../challenge_program/data/seattle/seattle.id profile1.json connectionlist_long.txt > tmp_seattle.out&

wait
sleep 1

cat tmp_*.out
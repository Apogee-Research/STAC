#!/bin/sh
set timeout -1

/usr/bin/expect user.expect ../challenge_program/data/la/la.id profile_woa_seller.json connectionlist2.txt > winownauction_la.out&
sleep 1
/usr/bin/expect user.expect ../challenge_program/data/nyc/nyc.id profile_woa_other.json connectionlist2.txt > winownauction_nyc.out&

wait
sleep 1

cat winownauction_*.out

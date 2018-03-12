#!/bin/sh

/usr/bin/expect file_megan.expect > file_megan.out&
sleep 1
/usr/bin/expect file_sally.expect > file_sally.out&


wait

sleep 1

cat file_*.out

cat ../challenge_program/data/megan/incoming/sally_cache/pi.txt
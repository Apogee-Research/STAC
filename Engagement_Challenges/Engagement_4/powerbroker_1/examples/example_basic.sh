#!/bin/sh
set timeout -1

/usr/bin/expect basic_nyc.expect > basic_nyc.out&
sleep 1
/usr/bin/expect basic_la.expect > basic_la.out&
sleep 1
/usr/bin/expect basic_detroit.expect > basic_detroit.out&

wait
sleep 1

cat basic_*.out

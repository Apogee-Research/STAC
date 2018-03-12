#!/bin/sh

/usr/bin/expect basic_deven.expect > basic_deven.out&
sleep 1
/usr/bin/expect basic_sally.expect > basic_sally.out&

wait

sleep 1

cat basic_*.out


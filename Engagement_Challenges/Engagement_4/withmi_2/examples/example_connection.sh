#!/bin/sh

/usr/bin/expect connection_sally.expect > connection_sally.out&
sleep 1
/usr/bin/expect connection_megan.expect > connection_megan.out&

wait

sleep 1

cat connection_*.out
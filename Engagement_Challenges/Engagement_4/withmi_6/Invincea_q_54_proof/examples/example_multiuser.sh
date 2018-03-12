#!/bin/sh

/usr/bin/expect multiuser_deven.expect > multiuser_deven.out&
sleep 1
/usr/bin/expect multiuser_megan.expect > multiuser_megan.out&
sleep 1
/usr/bin/expect multiuser_sally.expect > multiuser_sally.out&

wait

sleep 1

cat multiuser_*.out

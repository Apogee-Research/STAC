#!/bin/sh

/usr/bin/expect user.expect ../../../challenge_program/data/nyc/nyc.id profile_oneauction.json connectionlistmax.txt > log_nyc.out &
sleep 1
/usr/bin/expect user.expect ../../../challenge_program/data/la/la.id profile_twoauctions.json connectionlistmax.txt > log_la.out &
sleep 1
/usr/bin/expect user.expect ../../../challenge_program/data/detroit/detroit.id profile_twoauctions.json connectionlistmax.txt > log_detroit.out &

wait


#!/bin/sh
set timeout -1

/usr/bin/expect user.expect ../../../challenge_program/data/nyc/nyc.id profile_maxauctions.json connectionlistmax.txt > maxauctions_nyc.out &
sleep 1
/usr/bin/expect user.expect ../../../challenge_program/data/la/la.id profile_needmax.json connectionlistmax.txt > maxauctions_la.out &
sleep 1
/usr/bin/expect user.expect ../../../challenge_program/data/detroit/detroit.id profile_needmax.json connectionlistmax.txt > maxauctions_detroit.out &

wait


#!/bin/sh

/usr/bin/expect negativepower_nyc.expect > negativepower_nyc.out &

wait
sleep 1
cat negativepower_nyc.out


#!/bin/bash
# Used to run the test a number of times to get an idea of how often it might fail
for i in `seq 1 10`;
do
	echo Run $i
	time ./malicious.sh
done

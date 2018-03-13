#!/usr/bin/env bash
source ../functions.sh

SIZE=${1:-"NOSIZE"}

PID=$(jps | grep Tweeter | awk '{ print $1 }')

if [ "$SIZE" == "NOSIZE" ]; then
    echo "Provide the size you want as an argument. Options are 128, 256, and 512."
    exit -1
fi

echo "Logging out: "
logout

killCookies

echo "Total space used of the input budget is the sum of these: "

for i in $(seq 2048); do
    username=$(export LC_CTYPE=C; cat /dev/urandom | tr -dc 'a-zA-Z' | fold -w 16 | head -n 1) 
    echo "Adding user ($i, $username)"
    createUser "$i" "$username" "$i" "$SIZE"
done

jcmd "$PID" GC.run
echo "Memory used: " $(jstat -gc "$PID" 2> /dev/null | tail -n1 | awk '{printf "%d\n", $6 + $8}') " KB"

killCookies

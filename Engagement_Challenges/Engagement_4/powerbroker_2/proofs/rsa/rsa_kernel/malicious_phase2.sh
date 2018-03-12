#!/bin/bash

# for this phase, we start with a random interval around one of the primes
ANSWER=9739163282356647205961370465538717274720884095781168250213331986352640333583925144850105822083132845683468479911548534436085398617490134844637044452205089

RAND1=`shuf -i 10000-10000000 -n 1`
RAND2=`shuf -i 10000-10000000 -n 1`

# bash can't handle large numbers, so use dc to create an interval around the prime.
# dc returns multiple lines, so remove non-digits with tr
LOWER=`echo $ANSWER $RAND1 -p | dc | tr -dc [0-9]`
UPPER=`echo $ANSWER $RAND2 +p | dc | tr -dc [0-9]`

python ./comms_attack.py localhost 9000 ./rsa1 -r 20 -d 2 -a $ANSWER -2 --u1 $LOWER --u2 $UPPER


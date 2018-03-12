#!/bin/sh

ant -f ../source/SmartMail/
tar -C ../source/SmartMail/dist/ -cvf challenge_program.tar lib/ SmartMail.jar
mv challenge_program.tar ../../BT/challenge_program/


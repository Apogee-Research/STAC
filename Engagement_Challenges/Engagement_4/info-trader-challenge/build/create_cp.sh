#!/bin/sh

ant -f ../source/InfoTrader/
mkdir ../source/InfoTrader/dirs
tar -xvf ../source/InfoTrader/dirs.tar -C ../source/InfoTrader
tar -cvf challenge_program.tar -C ../source/InfoTrader/ dirs/ -C dist/ lib/ InfoTrader.jar
mkdir -p ../../BT/challenge_program/
mv challenge_program.tar ../../BT/challenge_program/


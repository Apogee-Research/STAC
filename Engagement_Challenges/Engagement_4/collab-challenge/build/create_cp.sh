#!/bin/sh

ant -f ../source/collab/
tar -C ../source/collab/dist/ -cf challenge_program.tar lib/ Collab.jar
mv challenge_program.tar ../../BT/challenge_program/


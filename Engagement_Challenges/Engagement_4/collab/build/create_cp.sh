#!/bin/sh

ant -f ../source/
rm -rf challenge_program/lib challenge_program/Collab.jar
cp ../source/dist/Collab.jar challenge_program/
cp -r ../source/dist/lib challenge_program/

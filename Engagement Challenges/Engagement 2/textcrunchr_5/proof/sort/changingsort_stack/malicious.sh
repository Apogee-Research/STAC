#!/bin/sh
javac src/maliciousInput/*.java
java -cp ./src maliciousInput.MaliciousInputCreator
exec ../../../challenge_program/bin/textcrunchrhost_1 ./sortInput.zip -p "wordFreqs"

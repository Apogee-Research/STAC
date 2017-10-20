#!/bin/sh

DIR=../challenge_program/client/dist

set -x
java -Djava.compiler=NONE -Xint -jar "$DIR"/DStoreClient.jar

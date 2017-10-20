#!/bin/sh

DIR=/home/challenge_program/server
SERVER_JAR="$DIR"/dist/DistributedStore.jar
NETTY_JAR="$DIR"/lib/netty-all-4.0.29.Final.jar
MAIN_CLASS=server.DistFSysServer
cd $DIR
set -x
java -Xint -Djava.compiler=NONE -cp "$SERVER_JAR:$NETTY_JAR" $MAIN_CLASS

#!/bin/sh

DIR=$(cd "$(dirname "$0")" && pwd)
SERVER_JAR="$DIR"/dist/DistributedStore.jar
NETTY_JAR="$DIR"/lib/netty-all-4.0.29.Final.jar
MAIN_CLASS=server.DistFSysServer
echo "Starting server, please wait"
sleep 3s
echo "Starting server, please wait"
sleep 3s
set -x
cd "$DIR" && java -Djava.compiler=NONE -cp "$SERVER_JAR:$NETTY_JAR" $MAIN_CLASS

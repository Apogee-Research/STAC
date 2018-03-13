#!/bin/sh

CHALLENGE_JAR=linalgservice.jar
GSON_JAR=gson-2.4.jar
NANOHTTPD_JAR=nanohttpd-2.2.0.jar
MAIN_CLASS=com.example.linalg.Main

set -x
java -Xmx12g -Xint -cp "$CHALLENGE_JAR:$GSON_JAR:$NANOHTTPD_JAR" "$MAIN_CLASS" 8080 

#!/bin/sh

[ -n "$JAVA" ] || JAVA=java

CHALLENGE_JAR=linalgservice.jar
GSON_JAR=gson-2.4.jar
NANOHTTPD_JAR=nanohttpd-2.2.0.jar
if [ -d target ]; then
    CHALLENGE_JAR=target/"$CHALLENGE_JAR"
    GSON_JAR=target/dependency/"$GSON_JAR"
    NANOHTTPD_JAR=target/dependency/"$NANOHTTPD_JAR"
fi

MAIN_CLASS=com.example.linalg.Main

set -x
"$JAVA" -Xmx4096m -Xint -cp "$CHALLENGE_JAR:$GSON_JAR:$NANOHTTPD_JAR" "$MAIN_CLASS" 8080 

#!/bin/sh

JAR=graphhopper-tour-0.5.0-jar-with-dependencies.jar
if [ ! -s "$JAR" ] && [ -d tour/target ]; then
    JAR=tour/target/"$JAR"
fi

CLASS=com.graphhopper.tour.tools.TourCLI

JAVA=$JAVA_HOME/bin/java
if [ "$JAVA_HOME" = "" ]; then
    JAVA=java
fi

if [ ! -s "$JAR" ]; then
    echo >&2 "$JAR does not exist"
    exit 1
fi

set -x

"$JAVA" -Xint -cp "$JAR" "$CLASS" "$@"

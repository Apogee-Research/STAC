#!/bin/sh

set -eu

JAVA=java
JAVA_OPTS="-Xmx1000m -Xms1000m -server"
JAVA_OPTS="$JAVA_OPTS -Xint"   # disable JIT

JAR=challenge.jar
#if [ -f challenge.jar ]; then
#    JAR=challenge.jar
#else
#    JAR=graphhopper-web-0.5.0-with-dep.jar
#    if [ -f lib/"$JAR" ]; then
#        JAR=lib/"$JAR"
#    else
#        JAR=web/target/"$JAR"
#    fi
#fi

CLASS=com.graphopper.web.GHServer
CONFIG=config.properties

OSM_FILE=data/massachusetts-latest.osm.pbf
GRAPH=${OSM_FILE%.*}-gh
MATRIX_FILE=data/matrix.csv

JETTY_HOST=127.0.0.1
JETTY_PORT=8989

if [ ! -f "$JAR" ]; then
    echo >&2 "$JAR does not exist"
    exit 1
fi

set -x

exec "$JAVA" $JAVA_OPTS -jar "$JAR" \
     jetty.host="$JETTY_HOST" jetty.port=$JETTY_PORT \
     config="$CONFIG" graph.location="$GRAPH" \
     matrix.csv="$MATRIX_FILE"

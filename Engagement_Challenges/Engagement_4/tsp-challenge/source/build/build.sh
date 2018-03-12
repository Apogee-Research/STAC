#!/bin/sh

# Build one or more modules (comma-separated) using Maven.

set -e

if [ $# -eq 0 ]; then
    set -- core,tour,web
fi

set -x
mvn install assembly:single -DskipTests=true --projects "$1"

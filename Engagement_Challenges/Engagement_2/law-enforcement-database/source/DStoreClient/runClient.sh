#!/bin/sh

DIR=$(dirname "$0")

set -x
java -Djava.compiler=NONE -jar "$DIR"/dist/DStoreClient.jar

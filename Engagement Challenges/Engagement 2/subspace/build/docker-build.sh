#!/bin/sh

set -eu

BUILD_DIR=$(cd "$(dirname "$0")" && pwd)
CP_DIR=$(cd "$BUILD_DIR"/../../BT/challenge_program && pwd)

set -x

cp "$BUILD_DIR"/Dockerfile "$CP_DIR"

cd "$CP_DIR" &&
docker build --rm -t trie-challenge .

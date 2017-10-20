#!/bin/sh

set -eu

BUILD_DIR=$(cd "$(dirname "$0")" && pwd)
CP_DIR=$(cd "$BUILD_DIR"/../../BT/challenge_program)

set -x

docker run --rm -it --net=host --name=trie-challenge trie-challenge

#!/bin/sh

set -eu

set -x

docker run --rm -it --net=host --name=trie-challenge-exploit \
   trie-challenge-exploit

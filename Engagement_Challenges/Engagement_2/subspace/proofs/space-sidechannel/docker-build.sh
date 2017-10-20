#!/bin/sh

set -eu

set -x

docker build --rm -t trie-challenge-exploit .

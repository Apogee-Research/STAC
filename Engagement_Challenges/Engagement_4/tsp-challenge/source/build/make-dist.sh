#!/bin/sh

set -ex

#./build/build.sh core,tour,web

mkdir -p dist

mkdir -p dist/lib
cp tour/target/*.jar dist/lib
cp web/target/*.jar dist/lib

rsync -a --delete scripts dist/

rsync -a --delete \
      --include 'scripts' \
      --include '*.py' \
      --include 'README.md' \
      --exclude '*' \
      ../proofs/time-sidechannel/ dist/proof

rsync -a --delete data --exclude '*-gh' dist/

cp config.properties dist/

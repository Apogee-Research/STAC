#!/bin/sh

set -ex

./build/make-dist.sh
rsync -av dist/ stac-nuc1:tsp-challenge-dist/

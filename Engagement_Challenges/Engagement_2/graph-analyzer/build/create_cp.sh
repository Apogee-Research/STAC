#!/bin/sh

die() {
    echo "$1" >&2
    exit 1
}

info() {
    echo "$1" >&2
}

set -eu

BUILD=$(cd "$(dirname "$0")" && pwd)
SOURCE=$(cd "$BUILD"/../source && pwd)
DEST_DIR=$(cd "$BUILD"/../../BT/challenge_program && pwd)
DEST_TAR="$DEST_DIR"/challenge_program.tar

STAGING="$BUILD"/staging
info "creating staging directory $STAGING"
rm -rf "$STAGING"
mkdir "$STAGING"

info "building source"
(cd "$SOURCE" && ant clean && ant jar) || die "build failed"

info "creating challenge_program.tar"
cp "$SOURCE"/dist/GraphDisplay.jar "$STAGING"
cp "$SOURCE"/data/example_NoSubs.dot "$STAGING"/example.dot
tar -C "$STAGING" -cf "$DEST_TAR" .

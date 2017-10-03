#!/bin/sh

PROG=$(basename "$0")

die() {
    echo "$PROG: $1" >&2
    exit 1
}

info() {
    echo "$PROG: $1" >&2
}

set -e

# detect whether we're in dev environment or delivery environment
BUILD=$(cd "$(dirname "$0")" && pwd)
if [ -d "$BUILD"/../../EL ]; then
    SOURCE=$(cd "$BUILD"/../source && pwd)
    STAGING="$BUILD"/challenge_dist
    CP_DIR="$BUILD"/../../BT/challenge_program
    CP_TAR="$CP_DIR"/challenge_program.tar
    [ -d "$CP_DIR" ] || die "$CP_DIR does not exit"
else
    SOURCE=$(cd "$BUILD"/../challenge && pwd)
    STAGING=$(cd "$BUILD"/.. && pwd)/challenge_dist
    CP_DIR=
    CP_TAR=
fi

info "SOURCE=$SOURCE"
info "STAGING=$STAGING"
info "CP_DIR=$CP_DIR"
info "CP_TAR=$CP_TAR"

info "creating/cleaning staging directory"
rm -rf "$STAGING"
mkdir "$STAGING"

info "building source"
(
    cd "$SOURCE/DistributedStore" && ant clean jar
    cd "$SOURCE/DStoreClient" && ant clean jar
) || die "build failed"

info "copying files"

mkdir "$STAGING"/server
cp -R "$SOURCE"/DistributedStore/dist "$STAGING"/server
cp -R "$SOURCE"/DistributedStore/lib "$STAGING"/server/lib
cp -R "$SOURCE"/DistributedStore/dumps "$STAGING"/server/dumps
cp -R "$SOURCE"/DistributedStore/files "$STAGING"/server/files
cp "$SOURCE"/DistributedStore/runServer.sh "$STAGING"/server/runServer.sh
chmod +x "$STAGING"/server/runServer.sh

mkdir "$STAGING/client"
cp -R "$SOURCE"/DStoreClient/dist "$STAGING"/client
cp -R "$SOURCE"/DStoreClient/runClient.sh "$STAGING"/client/runClient.sh
chmod +x "$STAGING"/client/runClient.sh

# not building DStoreTestHarness here; it's not part of the challenge

if [ -n "$CP_TAR" ]; then
    info "creating challenge_program.tar"
    tar -C "$STAGING" -cf "$CP_TAR" .
fi

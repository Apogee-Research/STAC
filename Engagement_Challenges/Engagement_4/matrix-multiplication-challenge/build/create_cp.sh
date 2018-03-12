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

BUILD=$(cd "$(dirname "$0")" && pwd)
SOURCE=$(cd "$BUILD"/../source && pwd)
STAGING="$BUILD"/challenge_dist
CP_DIR="$BUILD"/../../BT/challenge_program
if [ -d "$CP_DIR" ]; then
    CP_DIR=$(cd "$CP_DIR" && pwd)
    CP_TAR="$CP_DIR"/challenge_program.tar
else
    CP_DIR=
    CP_TAR=
fi

info "SOURCE=$SOURCE"
info "STAGING=$STAGING"
info "CP_DIR=$CP_DIR"
info "CP_TAR=$CP_TAR"

info "creating/cleaning staging directories"

rm -rf "$STAGING"
mkdir "$STAGING"

info "building source"
(
    cd "$SOURCE" && mvn clean package dependency:copy-dependencies 
) || die "build failed"

info "copying files"

CHALLENGE_JAR="$SOURCE"/target/matrixmultiply-0.0.1-SNAPSHOT.jar
GSON_JAR="$SOURCE"/target/dependency/gson-2.4.jar
NNHTTP_JAR="$SOURCE"/target/dependency/nanohttpd-2.2.0.jar

RUN_SCRIPT="$SOURCE"/scripts/run_challenge.sh
INTERACT_SCRIPT="$SOURCE"/scripts/example.sh

cp "$CHALLENGE_JAR" "$STAGING/linalgservice.jar"
cp "$GSON_JAR" "$STAGING"
cp "$NNHTTP_JAR" "$STAGING"
cp "$RUN_SCRIPT" "$STAGING"
cp "$INTERACT_SCRIPT" "$STAGING"


gunzip -c "$BUILD"/../proofs/blueteam-examples/example-input-laplacian.json.gz > "$STAGING"/example-input-laplacian.json
gunzip -c "$BUILD"/../proofs/blueteam-examples/example-input-mst.json.gz > "$STAGING"/example-input-mst.json
gunzip -c "$BUILD"/../proofs/blueteam-examples/example-input-mul.json.gz > "$STAGING"/example-input-mul.json
gunzip -c "$BUILD"/../proofs/blueteam-examples/example-input-sp.json.gz > "$STAGING"/example-input-sp.json



if [ -n "$CP_DIR" ]; then
    info "creating challenge_program.tar"
    tar -C "$STAGING" -cf "$CP_TAR" .
fi

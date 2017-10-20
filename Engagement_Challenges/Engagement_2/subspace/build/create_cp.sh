#!/bin/sh


# Utility functions
pecho() { printf %s\\n "$*"; }
log() { pecho "$@"; }
error() { log "ERROR: $@" >&2; }
fatal() { error "$@"; exit 1; }
try() { "$@" || fatal "'$@' failed"; }
require_cmd() { hash "$1" || fatal "Command '$1' does not exist"; }


top_challengedir=$(cd "$(dirname "$0")"/../.. && pwd)
top_srcdir=$top_challengedir/EL/source


# Test environment.
require_cmd gradle
require_cmd mktemp


# Build.
(
    try cd "$top_srcdir"

    test -r build.gradle \
        || fatal "Missing build.gradle, check the source directory."

    try gradle installApp
) || exit 1


# Create the archive.
top_archivedir="$(mktemp -t tmp.XXXXXXXXXX -d)" \
    || fatal "Could not create archive directory."

try rsync -a \
    "$top_srcdir"/build/install/Subspace/ \
    "$top_srcdir"/doc/subspace.properties \
    "$top_archivedir"

tar -cf "$top_challengedir"/BT/challenge_program/challenge_program.tar \
    -C "$top_archivedir" . || fatal "Could not create archive."


# Clean up.
try rm -rf "$top_archivedir"

(
    try cd "$top_srcdir"

    try gradle clean
) || exit 1

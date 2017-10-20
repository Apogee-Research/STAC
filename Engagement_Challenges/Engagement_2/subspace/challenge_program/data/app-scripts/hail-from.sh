#!/bin/sh

# Send a hail *from* the specified user to the closest other user.

script=${0##*/}

usage() {
    echo >&2 "usage: $script <username>"
    exit 2
}

log() { echo >&2 "$*"; }
error() { log "ERROR: $*" >&2; }
fatal() { error "$*"; exit 1; }
try() { "$@" || fatal "'$*' failed"; }
trace() { echo "+$*"; try "$@"; }

[ $# -eq 1 ] || usage

username=$1
email="stac+$username@localhost"

sendmail -f "$email" "hail@subspace.localhost" <<EOF
Subject: Hail!

Oh hail!
EOF

#!/bin/sh

script=${0##*/}

usage() {
    echo >&2 "usage: $script <username> <password> <latitude> <longitude>"
    exit 2
}

log() { echo >&2 "$*"; }
error() { log "ERROR: $*" >&2; }
fatal() { error "$*"; exit 1; }
try() { "$@" || fatal "'$*' failed"; }
trace() { echo "+$*"; try "$@"; }

[ $# -eq 4 ] || usage

username=$1
password=$2
latitude=$3
longitude=$4

log "setting location of user $username to $latitude,$longitude"
uri="https://localhost:8443/update-location"
uri="${uri}?username=${username}"
uri="${uri}&password=${password}"
uri="${uri}&lat=${latitude}"
uri="${uri}&lon=${longitude}"
try curl --insecure "${uri}"
echo; echo


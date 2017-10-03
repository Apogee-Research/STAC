#!/bin/sh

script=${0##*/}

usage() {
    echo >&2 "usage: $script <username> {away|<latitude> <longitude>}"
    exit 2
}

log() { echo >&2 "$*"; }
error() { log "ERROR: $*" >&2; }
fatal() { error "$*"; exit 1; }
try() { "$@" || fatal "'$*' failed"; }
trace() { echo "+$*"; try "$@"; }

[ $# -ge 1 ] || usage

username=$1
password="password"

uri="http://localhost:8080/update-location"
uri="${uri}?username=${username}"
uri="${uri}&password=${password}"

if [ $# -eq 3 ]; then
    latitude=$2
    longitude=$3

    log "setting location of user $username to $latitude,$longitude"
    uri="${uri}&lat=${latitude}"
    uri="${uri}&lon=${longitude}"
elif [ $# -eq 2 ] && [ "$2" = 'away' ]; then
    log "setting location of user $username to away"
    uri="${uri}&away=true"
fi

try curl "${uri}"
echo; echo

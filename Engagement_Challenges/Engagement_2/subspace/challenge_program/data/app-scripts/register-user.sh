#!/bin/sh

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
password="password"
email="stac+$username@localhost"

log "registering user $username"
uri="http://localhost:8080/register"
uri="${uri}?username=${username}"
uri="${uri}&password=${password}"
uri="${uri}&email=$(echo "${email}" | sed 's/+/%2B/g')"
try curl "${uri}"
echo

sleep 1

# visit link in confirmation email
uri=$(grep '^http://localhost:8080/confirm?token=[a-z0-9-]\+$' /var/mail/stac | tail -n 1)
try curl "${uri}"
echo; echo

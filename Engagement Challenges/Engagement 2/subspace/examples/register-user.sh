#!/bin/sh

script=${0##*/}

usage() {
    echo >&2 "usage: $script <username> <password>"
    exit 2
}

log() { echo >&2 "$*"; }
error() { log "ERROR: $*" >&2; }
fatal() { error "$*"; exit 1; }
try() { "$@" || fatal "'$*' failed"; }
trace() { echo "+$*"; try "$@"; }

[ $# -eq 2 ] || usage

username=$1
password=$2
email="stac+$username@localhost"

log "registering user $username"
uri="https://localhost:8443/register"
uri="${uri}?username=${username}"
uri="${uri}&password=${password}"
uri="${uri}&email=$(echo "${email}" | sed 's/+/%2B/g')"
try curl --insecure "${uri}"
echo

sleep 1

# visit link in confirmation email
uri=$(grep '^https://localhost:8443/confirm?token=[a-z0-9-]\+$' /var/mail/stac | tail -n 1)
try curl --insecure "${uri}"
echo; echo

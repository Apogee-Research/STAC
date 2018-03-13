#!/bin/sh

set -e

HOST=127.0.0.1
PORT=8989

usage() {
    cat >&2 <<EOF
Usage:
    ${0##*/} COMMAND

Commands:

    places
        Get list of known places

    tour <place> <place>...
        Calculate a tour.
EOF
    if [ $# -gt 0 ]; then
        echo >&2
        echo >&2 "$*"
    fi
    exit 2
}

places() {
    set -x
    curl -i -k "https://$HOST:$PORT/places"
}

tour() {
    query=

    for name in "$@"; do
        [ "$query" = "" ] || query="$query&"
        query="${query}point=${name}"
    done

    # duct-tape urlencode that handles spaces only
    query=$(echo "$query" | sed 's/ /%20/g')

    set -x
    curl -i -k "https://$HOST:$PORT/tour?$query"
}

subcommand=
while [ $# -gt 0 ]; do
    case "$1" in
        -h|--host)
            HOST="$2"; shift 2
            ;;
        -p|--port)
            PORT=="$2"; shift 2
            ;;
        places)
            subcommand=places; shift
            break
            ;;
        tour)
            subcommand=tour; shift
            break
            ;;
        *)
            usage "unrecognized argument \"$1\""
            ;;
    esac
done

[ "$subcommand" ] || usage "no subcommand specified"

$subcommand "$@"

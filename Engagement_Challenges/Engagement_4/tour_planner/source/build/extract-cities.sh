#!/bin/sh

# Worcester and east, not Cape or islands
#BOUNDING_BOX='--bounding-box left=-71.9 right=-70.5 bottom=42.19'
PLACE_TYPES='city,town,village'   # also valid: suburb,village

usage() {
    echo >&2 "usage: ${0##*/} input.pbf output.xml"
    exit 2
}

[ $# -eq 2 ] || usage
input_pbf=$1
output_xml=$2

set -ex

osmosis \
    --read-pbf "$input_pbf" \
    ${BOUNDING_BOX} \
    --tf accept-nodes place=${PLACE_TYPES} \
    --tf reject-ways \
    --tf reject-relations \
    --write-xml "$output_xml"

#!/bin/sh

# Utility functions
info() { printf '%s\n' "$*" >&2; }
error() { printf 'ERROR: %s\n' "$*" >&2; }

set -eu

BUILD=$(cd "$(dirname "$0")" && pwd)
SOURCE=$(cd "$(dirname "$0")"/../source && pwd )
PROOFS=$(cd "$(dirname "$0")"/../proofs && pwd )
BTCPDIR=$(cd "$(dirname "$0")"/../../BT/challenge_program && pwd )

info "Using build dir: ${BUILD}"
info "Using source dir: ${SOURCE}"
info "Using proofs dir: ${PROOFS}"
info "Using blue team challenge program dir: ${BTCPDIR}"

STAGING="${BUILD}/.staging"

info "Building..."
if ! (cd "${SOURCE}" && gradle build -x test > .create_cp.log 2>&1); then
    error "Build failed."
    cat "${SOURCE}"/.create_cp.log >&2
    rm "${SOURCE}"/.create_cp.log
    exit 1
else
    rm "${SOURCE}"/.create_cp.log
fi

info "Creating challenge jar..."

rm -rf "${STAGING}"
mkdir "${STAGING}"

mkdir -p "${STAGING}"/var/lib/trainer
mkdir "${STAGING}"/var/lib/trainer/images
cp -R "${PROOFS}"/images/{blue,red} "${STAGING}"/var/lib/trainer/images/

mkdir -p "${STAGING}"/home/stac
cp "${SOURCE}"/build/libs/ipchallenge-0.1.jar "${STAGING}"/home/stac/
cp "${PROOFS}"/{challenge,functions}.sh "${STAGING}"/home/stac/
cp "${PROOFS}"/images/{blue,cyan,red}.jpg "${STAGING}"/home/stac/

tar -C "${STAGING}" -cf "${BTCPDIR}"/challenge_program.tar home/ var/

rm -rf "${STAGING}"

info "Build complete."

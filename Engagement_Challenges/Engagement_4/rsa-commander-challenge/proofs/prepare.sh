#!/usr/bin/env bash

if ! which gradle; then
    echo "Cannot find gradle"
    exit 7
fi

(cd packetBuilder && gradle build -x test && cp build/libs/rsaCommander-0.1.jar ../) || exit 8
(cd keys && ./extract_keys.sh) || exit 9
(cd goodPackets && ./build_good_packets.sh) || exit 10
(cd badPackets && ./build_bad_packets.sh) || exit 11

echo "Operation completed successfully."
echo

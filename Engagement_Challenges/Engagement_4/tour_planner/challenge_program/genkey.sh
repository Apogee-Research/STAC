#!/bin/sh

set -eu

#BASEDIR=$(cd "$(dirname "$0")"/.. && pwd)

KEYTOOL=keytool

KEYSTORE=data/keystore.jks #${BASEDIR}/data/keystore.jks
STOREPASS=k3yst0r3
KEYALIAS=ghweb

"${KEYTOOL}" -keystore "${KEYSTORE}" \
             -genkey -alias "${KEYALIAS}" -keyalg RSA -keysize 1024 \
             >/dev/null 2>/dev/null <<EOF
${STOREPASS}
${STOREPASS}
com.graphhopper.http
GraphHopper Web Server



US
yes

EOF

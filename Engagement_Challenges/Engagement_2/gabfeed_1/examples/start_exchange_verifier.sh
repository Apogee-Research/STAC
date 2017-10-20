#!/bin/sh
if [ "$1" = "" ]; then
echo -e "usage: start_exchange_verifier.sh <private key>"
else
privatekey=$1
java -cp "../challenge_program/lib/*" com.cyberpointllc.stac.auth.KeyExchangeVerifier $privatekey ServersPublicKey.txt
fi

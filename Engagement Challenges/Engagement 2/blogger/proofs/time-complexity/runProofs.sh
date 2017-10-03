#!/usr/bin/env bash

echo -e "Normal Request..."
./normal-request.sh

echo -e "Normal Request 2..."

./normal-request-2.sh

echo -e "Normal 404..."

./normal-404.sh

echo -e "slow-404..."

./slow-404.sh

echo -e "Hang..."
./hang.sh

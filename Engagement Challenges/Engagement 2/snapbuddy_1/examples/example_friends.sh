#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -L -b cookies.txt --insecure https://localhost:8080/friends?suppressTimestamp=true


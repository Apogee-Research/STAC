#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -b cookies.txt -F "filter list=F00E" -F "filter list=F009" -F "filter list=F014" -F "filter list=F00F" --insecure https://localhost:8080/filter/29b9e/profile.jpg?suppressTimestamp=true
curl -s -L -b cookies.txt --insecure https://localhost:8080/photo/29b9e/profile.jpg

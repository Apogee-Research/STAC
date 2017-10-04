#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -F changename=@us_constitution.txt -b cookies.txt --insecure https://localhost:8080/profilename
curl -s -L -b cookies.txt --insecure https://localhost:8080/friends?suppressTimestamp=true

#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -b cookies.txt  -F addphoto=@image.png --insecure https://localhost:8080/addphoto?suppressTimestamp=true
curl -s -b cookies.txt -F "filter list=F009" --insecure https://localhost:8080/filter/6a3bb/image.png?suppressTimestamp=true
curl -s -L -b cookies.txt --insecure https://localhost:8080/photos?suppressTimestamp=true

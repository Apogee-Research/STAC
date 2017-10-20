#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -b cookies.txt  -F addprofilephoto=@profile.jpg --insecure https://localhost:8080/profilephoto?suppressTimestamp=true
curl -s -L -b cookies.txt --insecure https://localhost:8080/photos?suppressTimestamp=true

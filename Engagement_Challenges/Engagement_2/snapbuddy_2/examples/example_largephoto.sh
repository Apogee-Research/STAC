#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -L -b cookies.txt -F addphoto=@bigimage.png --insecure https://localhost:8080/addphoto

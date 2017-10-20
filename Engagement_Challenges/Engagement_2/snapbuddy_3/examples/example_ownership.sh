#!/bin/sh

./login.sh devenmartinez@hotmail.com PS1Ljv4NPs
curl -s -L -b cookies.txt --insecure https://localhost:8080/editcaption/29b9e/image_0063.jpg

#!/bin/sh
../../../examples/login.sh mateojohnson@gmail.com Pv9m53YmANf
curl -s -L -b cookies.txt -F "filter list=f00E" -F "filter list=f00e" -F "filter list=F00E" -F "filter list=F00e" --insecure https://localhost:8080/filter/6a3bb/profile.jpg?suppressTimestamp=true
curl -s -L -b cookies.txt --insecure https://localhost:8080/photo/6a3bb/profile.jpg

#!/bin/bash

if [ $# -eq 0 ]; then
	document="exampledocument.txt"
else
	document=$1
fi

curl -s -X POST --data-urlencode @$document "http://127.0.0.1:8988/doc.cgi"

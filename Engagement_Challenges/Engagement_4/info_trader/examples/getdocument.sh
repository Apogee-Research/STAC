#!/bin/bash

if [ $# -eq 0 ]; then
	document="Home%20is%20where%20the%20profit%20is"
else
	document=$( echo $1|sed 's/%/%25/g' |sed 's/ /%20/g' )
fi

#curl -s -X POST "http://127.0.0.1:8988/gdoc.cgi?name=$document&getAll=true"
echo -e $( curl -s -X POST "http://127.0.0.1:8988/gdoc.cgi?name=$document&getAll=true" | sed 's/\/n/\\n/g' )

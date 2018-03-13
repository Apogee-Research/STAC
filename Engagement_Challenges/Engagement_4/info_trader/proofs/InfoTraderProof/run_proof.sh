#!/bin/bash

# Run AC Space vulnerability proof
if [ "$1" == "space" ]; then
	echo -e "\tRunning AC Space Proof"
	java -jar ./dist/InfoTraderProof.jar
# Run beningn AC Time red herring check
elif [ "$1" == "ntime" ]; then
	echo -e "\tRunning Null AC Time Red Herring"
	java -jar ./dist/InfoTraderProof.jar redherring
else
	echo -e "usage <space|ntime>\n"
fi

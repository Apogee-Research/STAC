#!/bin/bash

createUser() {
	if [ "$#" -ne 3 ] ; then
		output="Usage: ./interact.sh createUser <fullname> <username> <password>"
		cookie="F"
	else
		fullname=$1
		username=$2
		password=$3
		echo -e "Creating user: $username"
		curl -so csrf.html -c ./cookies http://localhost:8080/register
		csrf=$(cat csrf.html | grep _csrf | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
		rm csrf.html
		curl -b ./cookies -X POST -F "fullname=$fullname" -F "username=$username" -F "password=$password" -F "password2=$password" -F "register=Register" -F "_csrf=$csrf" http://localhost:8080/register/create
		cookie="T"
	fi
}

login() {
	if [ "$#" -ne 2 ] ; then
                output="Usage: ./interact.sh login <username> <password>"
		cookie="F"
        else
		username=$1
                password=$2
		echo -e "Logging in user: $username"
		curl -so csrf.html -c ./cookies http://localhost:8080/register
                csrf=$(cat csrf.html | grep _csrf | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
                rm csrf.html
		curl -sc ./cookies -b ./cookies -X POST -F "username=$username" -F "password=$password" -F "login=Login" -F "_csrf=$csrf" http://localhost:8080/login
		cookie="T"
	fi
}

logout() {
	if [ -e "./cookies" ] ; then
		output=$output"\nLogging out"
		curl -sc ./cookies -b ./cookies http://localhost:8080/logout
		cookie="T"
	else
		echo -e "Not logged in"
		cookie="F"
	fi
}

sendTweet() {
	if [ "$#" -ne 3 ] ; then
		output="Usage: ./interact.sh sendTweet <username> <password> <tweet>"
		cookie="F"
	else
		login "$1" "$2";
		echo "Sending tweet"
		curl -so csrf.html -b ./cookies http://localhost:8080/
		csrf=$(cat csrf.html | grep _csrf | head -n1 | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
		rm csrf.html
		curl -sb ./cookies -X POST -F "value=$3" -F "_csrf=$csrf" http://localhost:8080/tweet
		cookie="T"
		logout;
	fi
}

viewNotifications() {
	if [ "$#" -ne 2 ] ; then
                output="Usage: ./interact.sh viewNotifications <username> <password>"
                cookie="F"
        else
		login "$1" "$2";
		curl -so csrf.html -b ./cookies http://localhost:8080/
                csrf=$(cat csrf.html | grep _csrf | head -n1 | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
                rm csrf.html
		output=$(curl -c ./cookies -b ./cookies http://localhost:8080/i/notifications)
		cookie="T"
		logout;
	fi
}

if [[ "$1" == "createUser" ]] ; then
	createUser "$2" "$3" "$4";
elif [[ "$1" == "login" ]] ; then
        login "$2" "$3";
elif [[ "$1" == "sendTweet" ]] ; then
	sendTweet "$2" "$3" "$4";
elif [[ "$1" == "viewNotifications" ]] ; then
	viewNotifications "$2" "$3";
else
	output="Usage:\n"
	output=$output"\t./interact.sh createUser <fullname> <username> <password>\n"
	output=$output"\t./interact.sh login <username> <password>\n"
	output=$output"\t./interact.sh sendTweet <username> <password> <tweet>\n"
	output=$output"\t./interact.sh viewNotifications <username> <password>\n"
fi
echo -e "$output"
if [[ "$cookie" == "T" ]] ; then
	rm -f ./cookies
fi

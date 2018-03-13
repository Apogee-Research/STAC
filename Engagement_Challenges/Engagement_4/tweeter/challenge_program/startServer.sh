#!/bin/bash

# Start Tweeter Server
echo "Starting Twitter Application"
usercreated=false
java -Xint -jar Tweeter-1.0.0a.jar | while read line
do
        echo -e "$line"
        if [[ "$line" == *"Started TwitterApplication"* ]] && [ $usercreated = false ]
        then
                # Create User "user" with password "password"
                echo "Creating user: user"
                fullname=user
                username=user
                password=password
                curl -so csrf.html -c ./cookies http://localhost:8080/register
                csrf=$(cat csrf.html | grep _csrf | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
                rm csrf.html
                curl -sb ./cookies -X POST -F "fullname=$fullname" -F "username=$username" -F "password=$password" -F "password2=$password" -F "register=Register" -F "_csrf=$csrf" http://localhost:8080/register/create
                rm cookies
                usercreated=true
                echo "Started Tweeter Application"
        fi
done

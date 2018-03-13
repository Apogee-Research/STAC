#!/usr/bin/env bash

newCSRF() {
    sizes=$(curl -so csrf.html -c ./cookies http://localhost:8080/register -w "%{size_upload}:%{size_request}")
    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    csrf=$(cat csrf.html | grep _csrf | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
    rm csrf.html
    echo "Size: " $(($req_size + $size_up))
}

updateCSRF() {
    sizes=$(curl -so csrf.html -b ./cookies http://localhost:8080/ -w "%{size_upload}:%{size_request}")
    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    csrf=$(cat csrf.html | grep _csrf | head -n1 | sed 's/^.*value="\([a-f0-9-]*\)".*/\1/g')
    rm csrf.html
    echo "Size: " $(($req_size + $size_up))
}

killCookies() {
    rm ./cookies
}

createUser() {
    newCSRF;
    size=${4:-"0"}

    sizes=$(curl -sb ./cookies -X POST -F "size=$size" -F "fullname=$1" -F "username=$2" -F "password=$3" -F "password2=$3" -F "register=Register" -F "_csrf=$csrf" http://localhost:8080/register/create -w "%{size_upload}:%{size_request}")

    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    echo "Size: " $(($req_size + $size_up))
}

logout() {
    sizes=$(curl -sc ./cookies -b ./cookies http://localhost:8080/logout -w "%{size_upload}:%{size_request}")

    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    echo "Size: " $(($req_size + $size_up))
}

login () {
    newCSRF;

    sizes=$(curl -sc ./cookies -b ./cookies -X POST -F "username=$1" -F "password=$2" -F "login=Login" -F "_csrf=$csrf" http://localhost:8080/login -w "%{size_upload}:%{size_request}")
    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    echo "Size: " $(($req_size + $size_up))
}

tweet () {
    login "$1" "$2";
    updateCSRF;
    sizes=$(curl -sb ./cookies -X POST -F "value=$3" -F "_csrf=$csrf" http://localhost:8080/tweet -w "%{size_upload}:%{size_request}")
    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    echo "Size: " $(($req_size + $size_up))
}

tweetNoLogin () {
    updateCSRF;
    sizes=$(curl -sb ./cookies -X POST -F "value=$1" -F "_csrf=$csrf" http://localhost:8080/tweet -w "%{size_upload}:%{size_request}")
    req_size=$(echo $sizes | cut -d: -f1)
    size_up=$(echo $sizes | cut -d: -f2)
    echo "Size: " $(($req_size + $size_up))
}

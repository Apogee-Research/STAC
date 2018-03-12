#!/usr/bin/env bash

set -E

trap '[ "$?" -ne 77 ] || exit 77' ERR

JAR_NAME="rsaCommander-0.1.jar"
echo -en "Finding gradle: "
if which gradle; then
    cp -R ../source/ temp
    mkdir challenge_dist
    mkdir ../../BT/challenge_program/
    cp ./Dockerfile ../../BT/challenge_program/

    (
        cd temp;
        if ! gradle clean build ; then echo "Build failed" && exit 77; fi
        cp "build/libs/${JAR_NAME}" ../challenge_dist/challenge_program.jar
    )

    rm -rf temp

    cp resources/privatekey_{b,c}.pem challenge_dist/

    (
        cd challenge_dist;
        tar -cf ../../../BT/challenge_program/challenge_program.tar .
    )

    rm -r challenge_dist;

else
    echo "Error: Gradle not found on path"
fi


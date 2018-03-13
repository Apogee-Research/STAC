#!/usr/bin/env bash

set -E

trap '[ "$?" -ne 77 ] || exit 77' ERR

JAR_NAME="rsaCommander-0.1.jar"
echo -en "Finding gradle: "
if which gradle; then
    cp -R ../source/ temp
    (
        cd temp;
        if ! gradle --console=rich clean build ; then echo "Build failed" && exit 77; fi
        cp "build/libs/${JAR_NAME}" ../challenge_program/challenge_program.jar
    )

    rm -rf temp

else
    echo "Error: Gradle not found on path"
fi


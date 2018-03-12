#!/usr/bin/env bash
echo -en "Finding gradle: "
if which gradle; then

cp -R ../source/ temp
mkdir challenge_dist
mkdir ../../BT/challenge_program/
cp ./Dockerfile ../../BT/challenge_program/

(
    cd temp;
    gradle clean build -x test;
    cp "build/libs/Tweeter-1.0.0a.jar" ../challenge_dist/
)

rm -rf temp

(
    cd challenge_dist;
    tar -cf ../../../BT/challenge_program/challenge_program.tar .
)

rm -r challenge_dist;

else
    echo "Error: Gradle not found on path"
fi

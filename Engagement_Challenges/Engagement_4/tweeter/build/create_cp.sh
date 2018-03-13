#!/usr/bin/env bash
echo -en "Finding gradle: "
if which gradle; then

cp -R ../source/ temp
rm -rf challenge_program
mkdir challenge_program

(
    cd temp;
    gradle clean build -x test;
    cp "build/libs/Tweeter-1.0.0a.jar" ../challenge_program
)

rm -rf temp

else
    echo "Error: Gradle not found on path"
fi


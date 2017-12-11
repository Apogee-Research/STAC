#!/bin/bash

SRC_DIR=../../../../Source/src
cp $SRC_DIR/Category17_vulnerable.java .
javac Category17_vulnerable.java
jar cfe Category17.jar Category17_vulnerable *.class ../../../Apogee_LICENSE
rm *.java *.class

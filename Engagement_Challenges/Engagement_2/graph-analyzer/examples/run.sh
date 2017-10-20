#!/bin/bash

#Generate postscript
java -Xint -jar ../challenge_program/GraphDisplay.jar dot ./example.dot xy diagram ps postscript_example

#Generate PNG files
java -Xint -jar ../challenge_program/GraphDisplay.jar dot ./example.dot xy diagram png PNG_example

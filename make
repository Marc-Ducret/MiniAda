#!/bin/bash

if [[ "$@" == "clean" ]]
then
    echo "Cleaning..."
	cd bin
	rm *
else
	echo "Compiling ADAc..."
    cd src
	javac -d ../bin/ net/slimevoid/miniada/Compiler.java
	echo "Compilation terminated"
fi
#!/bin/bash

# go to script folder so relative paths are ok
cd "$(dirname "$0")"

echo COPYING SOURCE

mkdir -p "release"

# 

mkdir -p "abgabe/source"

# copy common to server
rsync -a --progress ../app/common/src/main/java/de/ovgu/softwareprojekt/ abgabe/source/server/src/de/ovgu/softwareprojekt/ --exclude "*build*"

# server without build files
rsync -a --progress ../server abgabe/source/server --exclude "*build*"


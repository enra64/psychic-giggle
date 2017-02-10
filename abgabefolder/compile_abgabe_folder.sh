#!/bin/bash

# go to script folder so relative paths are ok
cd "$(dirname "$0")"

echo COPYING SOURCE

mkdir -p "abgabe"

mkdir -p "abgabe/source"

# app without build files
rsync -a --progress ../app abgabe/source/app --exclude "*build*"

# server without build files
rsync -a --progress ../server abgabe/source/server --exclude "*build*"



mkdir -p "abgabe/docs"

echo REGENERATING DOCS
# gen docs
../Abschlussbericht/generate_pdf.sh

# copy over docs
cp ../Abschlussbericht/*.pdf abgabe/docs/.


echo COPYING FILES FOR BINARIES

mkdir -p "abgabe/binary"
# copy various files required
cp ../keys.properties ../nesLayout.xml ../snes9x.conf ../vrep_sp_scene.ttt abgabe/binary/

echo "FINISHED; PLEASE COPY SERVER JAR to abgabe/binary/server.jar"

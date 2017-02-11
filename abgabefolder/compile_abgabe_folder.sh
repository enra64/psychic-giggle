#!/bin/bash

rm -r abgabe
rm psychic-frameworks-abgabe.zip

# go to script folder so relative paths are ok
cd "$(dirname "$0")"

echo COPYING SOURCE

mkdir -p "abgabe"

mkdir -p "abgabe/source"

# app without build files
rsync -a --progress ../app abgabe/source/app --exclude "*build*"

# server without build files
rsync -a --progress ../server abgabe/source/server --exclude "*build*" --exclude "*out*"



mkdir -p "abgabe/docs"

echo REGENERATING DOCS
# gen docs
../Abschlussbericht/generate_pdf.sh

# copy over docs
cp ../Abschlussbericht/*.pdf abgabe/.


echo COPYING FILES FOR BINARIES

mkdir -p "abgabe/binary"
# copy various files required
cp ../keys.properties ../nesLayout.xml ../snes9x.conf ../vrep_sp_scene.ttt abgabe/binary/


echo COPYING javadoc
cp -r "docs app mit common" abgabe/docs/
cp -r "docs server mit common" abgabe/docs/

echo COPYING APK
cp psychicsensors.apk abgabe/binary

echo COPYING ROM
cp mariokart.smc abgabe/binary

echo COPYING SERVER JAR
cp server.jar abgabe/binary

echo COPYING README
cp README.txt abgabe/

zip -r psychic-frameworks-abgabe.zip abgabe

echo "FINISHED"

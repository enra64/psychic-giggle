#!/bin/bash

# go to script folder so relative paths are ok
cd "$(dirname "$0")"
mkdir -p release



echo REGEN DOCS
../Abschlussbericht/generate_pdf.sh



echo COPYING jars
cp *.jar release



echo ZIPPING docs
mkdir -p docs
cp -r "docs app mit common" "docs server mit common" "../Abschlussbericht/Abschlussbericht.pdf" "../Abschlussbericht/Developer Guide.pdf" "../Abschlussbericht/User Guide.pdf" docs/
rm release/docs.zip
zip -qr release/docs.zip docs/*
rm -r docs



echo ZIPPING release server src
rm release/server_development.zip
zip -qr release/server_development.zip psychic_server

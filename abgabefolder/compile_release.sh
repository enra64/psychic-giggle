#!/bin/bash

# go to script folder so relative paths are ok
cd "$(dirname "$0")"

mkdir release

echo COPYING jars
cp *.jar release

zip -r release/docs.zip "docs app mit common" "docs server mit common"

zip -r release/server.zip release_src

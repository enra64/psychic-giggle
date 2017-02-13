#!/bin/bash

# go to script folder so relative paths are ok
cd "$(dirname "$0")"

echo BEGINNING RELEASE
./compile_release.sh

echo BEGINNING ABGABE
./compile_abgabe_folder.sh

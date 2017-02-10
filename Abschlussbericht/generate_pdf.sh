#!/bin/bash

# go to script folder so relative paths are ok
cd "$(dirname "$0")"

printf "generating main pdf\n"
# concat all docs
cat Einleitung.md Projektverlauf.md LessonsLearned.md Architektur.md > komplett.md

# create pdf from concatenated docs
pandoc --number-sections --toc --include-before=title.tex -Vlang=de-DE -Vgeometry:margin=1in komplett.md -o pdf.pdf

# remove temp file
rm komplett.md

printf "generating userguide pdf\n"
# create pdf from UserGuide.md
pandoc --number-sections --toc --include-before=title.tex -Vlang=de-DE -Vgeometry:margin=1in UserGuide.md -o UserGuide.pdf


printf "generating devguide pdf\n"
# create pdf from DeveloperGuide.md
pandoc --number-sections --toc --include-before=title.tex -Vlang=de-DE -Vgeometry:margin=1in DeveloperGuide.md -o DevGuide.pdf

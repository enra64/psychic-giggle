# concat all docs
cat titelseite.md Einleitung.md Projektverlauf.md Architektur.md DeveloperGuide.md > komplett.md

# create pdf from concatenated docs
pandoc --number-sections --toc -Vlang=de-DE -Vgeometry:margin=1in komplett.md -o pdf.pdf

# remove temp file
rm komplett.md

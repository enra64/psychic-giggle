cat Einleitung.md Projektverlauf.md Architektur.md DeveloperGuide.md > komplett.md
pandoc -V geometry:margin=1in komplett.md -o pdf.pdf
rm komplett.md

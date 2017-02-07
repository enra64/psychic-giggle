# cat all docs without toc
cat titelseite.md Einleitung.md Projektverlauf.md Architektur.md DeveloperGuide.md > komplett.md

# generate table of contents from komplett.md, pre- and append \n so pandoc understands the list
printf '# Inhaltsverzeichnis \n\n  ' > toc.md
ruby gen_toc.rb >> toc.md
printf '\n' >> toc.md

# concat all docs with toc
cat titelseite.md toc.md Einleitung.md Projektverlauf.md Architektur.md DeveloperGuide.md > komplett.md

# create pdf from concatenated docs
pandoc -V geometry:margin=1in komplett.md -o pdf.pdf

# remove temp files
#rm komplett.md
#rm toc.md

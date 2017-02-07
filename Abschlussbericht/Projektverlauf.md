# Projektverlauf
## Projektplan
![timeline](Timeline.png)
### Prototyping
Um eine geeignete Übertragungsart der Sensordaten zu finden und vertraut mit dem Server-Client-Konzept zu werden wurden Prototypen erstellt.
Hierbei wurden mehrere Varianten getestet: zum einem ob es besser ist Daten mit TCP oder UDP zu senden und zum anderen wurde verglichen, ob es besser ist den Server auf dem Smartphone oder auf dem PC zu betreiben.
### Mouseserver & Kommunikations-Backend
Es wurde das Kommunikations-Backend erstellt, das die Kommunikation zwischen Server und Clients sicherstellt. Aufgrund der Zeitkritikalität und Indifferenz gegenüber fehlenden Paketen der Sensordaten haben wir uns hier für UDP entschieden. Der Befehlsaustausch erfolgt über TCP.

Parallel zur Fertigstellung des Kommunikations-Backends wurde eine Maussteuerung per Smartphone implementiert. Hierbei werden Gyroskopdaten verwendet und mit Hilfe der von Java vorgegebnen Robot-Klasse Inputs emuliert.
### Spielsteuerung
Für die Umsetzung einer Spielsteuerung haben wir uns für Super Mario Kart auf dem Super Nintendo Entertainment System entschieden, da hier wenige Buttons benötigt werden und die Möglichkeit der Verwendung von mehr als einem Sensor besteht.
Der Gravitationsensor wird zur Steuerung der Lenkbewegung und der lineare Beschleunigungssensor zur Steuerung des Itemwurfes verwendet.
### Robotersteuerung
Zur Lösung eines Murmellabyrinths mit Hilfe des Gravitationsensors des Smartphones wurde eine Steuerung für den KUKA LBR iiwa R800 entworfen.


## Entwicklungsprozess
Zur Entwicklung der Applikation wurde Android Studio und zur Entwicklung des Servers wurde IntelliJ IDEA verwendet. Als Version Control System diente Git.

Aus am Anfang erstellten Use Cases wurden benötigte Funktionen und Subfunktionen abgeleitet. Diese wurden anfangs in JIRA eingetragen und nach und nach abgearbeitet.
Bei Auftreten eines Problems oder einer fehlenden Funktionalität wurde ein neuer Issue erstellt und hinzugefügt.

Wir haben uns keine Aufteilung der Aufgaben festgelegt. Issues wurden nach ihrer Dringlichkeit im Projektverlauf und der Länge ihrer Bestandszeit geordnet abgearbeitet.

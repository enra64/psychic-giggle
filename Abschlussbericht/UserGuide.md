# User-Guide
Die Bedienung von App und Server wird aus Sicht unserer Beispielimplementationen erläutert

## Was benötigt wird: 
- Handy mit Android (__mindestens 4.4__) 
- javafähiges Gerät (__Java 8__) um Server darzustellen
- Server Jar befindet sich auf dem Gerät
- Die App "PsychicSensors" ist auf dem Handy installiert

## Server starten:
- mittels GUI
      - alternativ eine spezifische Anwendung ohne GUI starten: Terminal Befehl: "java –jar server.jar Anwendung"

## App starten:
- App starten und auf Discovery (Server suchen) drücken
- gegebenenfalls Port über Option einstellen (darf nicht in Benutzung sein)
- gewünschten Server auswählen und verbinden

<p align="Center">
<img src="Screenshots/Discovery Activity.png" alt="Discovery Activity" Width="300px"/>
<br>
1. Optionen<br>
2. Server<br>
3. Server suchen<br>
</p>

## Einstellungen:
- hier lassen sich Name des Gerätes, benutzter Discovery Port und je nach gewählter Anwendung die Sensibilität der benutzbaren Sensoren des Handys einstellen (ist keine Anwendung gewählt, können alle Sensoren eingestellt werden)

<p align="Center">
<img src="Screenshots/Options Activity.png" alt="Options Activity Mouse Server" Width="300px"/>
<br>
1. Handy Namen<br>
2. Discovery Port<br>
3. Sensorname und Empfindlichkeitsslider<br>
4. Verwendungszweck des Sensors<br>
5. Optionen verlassen<br>
</p>

## GUI:
- Start durch Doppelklick
    * alternativ: durch den Terminal Befehl: java –jar server.jar
- diese GUI ist nur für die 3 implementierten Beispielanwendungen erstellt worden
- Änderungen dieser, oder eine neue GUI können durch den Implementator erfolgen

<p align="Center">
<img src="Screenshots/GUI.png" alt="GUI" Width="500px"/>
<br>
1. Maus Server starten<br>
2. Controller Server starten<br>
3. KUKA Server starten<br>
4. Aktuellen Server beenden<br>
5. Feld für Serverinformationen<br>
</p>

## Anwendungen:
### 1) Maussteuerung:
Terminal Befehl: java –jar server.jar mouse
- das Handy verhält sich wie ein gedachter Laserpointer, womit der Mauszeiger bewegt werden kann. Idealerweise zeigt die Spitze des Handys bereits vor der Verbindung des Handys mit dem Server auf den Cursor.
- "Linksklick" und "Rechtsklick" verhalten sich wie Links- und Rechtsklick bei einer Maus
- "Reset" setzt den Cursor auf die Mitte des aktuellen Monitors (auf dem der Cursor sich gerade befindet)
- "Sensor anhalten" stoppt alle Bewegungen des Cursors (Cursor ist nicht mehr durch Handy steuerbar)

<p align="Center">
<img src="Screenshots/Send Activity.png" alt="Send Activity Mouse Server" Width="300px"/>
<br>
1. Optionen<br>
2. Verbindung trennen<br>
3. Vom Server angeforderte Bedienelemente<br>
4. Rücksetzfunktion<br>
5. Sensordatenübertragung pausieren<br>
</p>


### 2) Controller:
Terminal Befehl: java –jar server.jar nes
- Die Nutzung des Controllers ist hauptsächlich für die Emulation des Spieles "Super Mario Kart" gedacht, aber kann, wenn die Steuerungseinstellungen der Anwendung richtig konfiguriert wurde, auch für andere Rennspiele genutzt werden. Die "keys.properties" Datei enthält alle Steuerungsbelegungen und orientiert sich dabei am SNES-Kontroller 
- Für die Nutzung des Spieles "Super Mario Kart" wird am Besten der Emulator SNES9X verwendet. Die richtige Konfiguration des Emulators für die Verwendung dieser Anwendung befindet sich in der Datei snes9x.conf. Diese Datei muss sich lediglich im selben Ordner des Emulators befinden 
- vor Benutzung sollte unbedingt die gewünschte Anwendung vorher gestartet sein, da sonst unerwünschte Zeichen und Cursorbewegungen die Folge sein können
- zur Benutzung sollte das Handy um 90° nach links gekippt werden (wie ein Lenkrad)
- Für eine Lenkbewegung wird das Handy nun nach links oder rechts gekippt
- In den Optionen kann die Lenkempfindlichkeit eingestellt werden
- In Super Mario Kart wird mittels des B-Buttons etwas im Menü ausgewählt oder im Spiel beschleunigt
      - Alternativ: Zum Auswählen eines Menüpunktes den Select-Button verwenden
- Das Menü kann mittels einer "Stoßbewegung" vom Körper weg ein höherer bzw. einer Bewegung zum Körper hin ein unterer Menüpunkt gewählt werden. Im Spiel kann mittels derselben Bewegung ein Item nach vorne oder nach hinten geworfen werden
      - Alternativ: Zur Benutzung eines Items kann ebenfalls der A-Button verwendet werden
- Der Startbutton pausiert das Spiel
- die Buttons der App emulieren entsprechend der config Datei Tasten von einer Tastatur
- "Sensor anhalten" stoppt alle Sensordatenübertragungen 


### 3) KUKA LBR iiwa 7 r800
Terminal Befehl: java –jar server.jar vrep
- Kuka Roboter "KUKA LBR iiwa 7 r800" oder entsprechende Simulationssoftware (vrep) ist erforderlich 
- Marble-Labyrinth-Control: Roboter steht in vordefinierter Position, zu bewegen ist nur das vorderste Gelenk
- alternativ: mit den Button "Wechsel zwischen Murmelmodus und Gelenkkontrolle" lässt sich jedes der Gelenke auswählen und einzeln bewegen
- "Sensor anhalten" stoppt alle Sensordatenübertragungen

### 4) Weitere Anwendungen
weitere Anwendungen und Tutorials können durch Implementatoren bereitgestellt werden

## Bekannte Probleme:
- benutzte Ports dürfen nicht von der Firewall blockiert werden
    - Lösung: neue Regel für die Firewall erstellen, in der dem Server erlaubt wird, mit dem Netzwerk zu kommunizieren
    - alternativ: Firewall deaktivieren

- unerwartetes Verhalten der Sensoren
    - Lösung: die Handysensoren neu kalibrieren

- Exception: "Address already in use: Cannot bind"
    - Lösung: anderen Discovery Port in den Einstellungen wählen
    
- Handy kann nicht zu Server verbinden
    - Lösung: im WLAN die Kommunikation zwischen Clients erlauben
    - alternativ: eigenes WLAN durch Hotspot erstellen

# User-Guide

Die Bedienung von App und Server wird aus Sicht unserer Beispielimplementation erläutert

# Was man braucht: 
- Handy mit Android(mindestens 4.4) 
- javafähiges Gerät(Java 8) um Server darzustellen
- Server Jar auf Gerät, App auf Handy installiert

# Server starten:
- mittels Terminal Befehl „java –jar servername Anwendung“

# App starten:
- App starten und auf Discovery (Server suchen) drücken
- gegebenenfalls Port über Option einstellen (darf nicht in Benutzung sein)
- gewünschten Server auswählen und verbinden

<p align="Center">
<img src="Screenshots/Discovery Activity.png" alt="Discovery Activity" Width="400px"/>
<br>
1. Optionen<br>
2. Server<br>
3. Server suchen<br>
</p>

# Einstellungen:
-hier lassen sich Name des Gerätes, benutzter Port und je nach gewählter Anwendung die Sensibilität der benutzbaren Sensoren des Handys einstellen (ist keine Anwendung gewählt, können alle Sensoren eingestellt werden)
<p align="Center">
<img src="Screenshots/Options Activity.png" alt="Options Activity Mouse Server" Width="400px"/>
<br>
1. Handy Namen<br>
2. Discovery Port<br>
3. Sensorname und Empfindlichkeitsslider<br>
4. Verwendungszweck des Sensors<br>
5. Optionen verlassen<br>
</p>

# Anwendungen:
## 1) Maussteuerung:
Terminal Befehl: java –jar server.jar mouse
- das Handy verhält sich wie ein gedachter Laserpointer, womit der Mauszeiger bewegt werden kann
- „Left Click“ und „Right Click“ verhalten sich wie Links- und Rechtsklick bei einer Maus
- „Zentrieren“ setzt den Cursor auf die Mitte des aktuellen Monitors (auf dem der Cursor sich gerade befindet)
- „Sensor anhalten“ stoppt alle Bewegungen des Cursors (Cursor ist nicht mehr durch Handy steuerbar)

<p align="Center">
<img src="Screenshots/Send Activity.png" alt="Send Activity Mouse Server" Width="400px"/>
<br>
1. Optionen<br>
2. Verbindung trennen<br>
3. Vom Server angeforderte Bedienelemente<br>
4. Rücksetzfunktion<br>
5. Datenübertragung pausieren<br>
</p>


## 2) Controller:
Terminal Befehl: java –jar server.jar nes
- Die Nutzung des Controllers ist hauptsächlich für die Emulation des Spieles "Super Mario Kart" gedacht aber kann, wenn die Steuerungseinstellungen der Anwendung richtig konfiguriert wurde, auch für andere Rennspiele genutzt werden. Die "keys.properties" Datei enthält alle Steuerungsbelegungen und orientiert sich dabei am SNES-Kontroller 
- vor Benutzung sollte unbedingt die gewünschte Anwendung vorher gestartet sein, da sonst unerwünschte Zeichen und Cursorbewegungen die Folge sind
- zur Benutzung sollte das Handy um 90° nach links gekippt werden (wie ein Lenkrad)
- die Buttons der App emulieren entsprechend der config Datei Tasten von einer Tastatur
- „Sensor anhalten“ stoppt alle Bewegungen 
-„zentrieren“ hat hier keine Funktion


## 3) Robotersteuerung
Terminal Befehl: java –jar server.jar vrep
-Kuka Roboter „Kuka lbr iiwa 7 r800“ oder entsprechende Simulationssoftware (vrep) ist erforderlich 
- Marble-Labyrinth-Control: Robbi steht in vordefinierter Position, zu bewegen ist nur das vorderste Glied
- alternativ: mit den Button „switch between marble labyrinth and joint control mode“ lässt sich jedes der Glieder auswählen und einzeln bewegen

## 4) Weitere Anwendungen
weitere Anwendungen und Tutorials können durch Implementator bereitgestellt werden

# Bekannte Probleme:
- benutzter Port darf nicht von Firewall blockiert werden<br>
Lösung: 
neue Regel für die Firewall erstellen, in dem es erlaubt ist, über den Port zu kommunizieren

- unerwartetes Verhalten der Sensoren<br>
Lösung:
die Handysensoren neu kalibrieren

-  Exception: "Address already in use: Cannot bind"<br>
Lösung:
anderen Discovery Port in den Einstellungen wählen


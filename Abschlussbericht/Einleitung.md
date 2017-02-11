% Abschlussbericht Softwareprojekt: PsychicFramework
% Ulrich B�tjer; Markus Hempel; Andr� Henniger; Arne Herdick
\newpage

# Ziel
Das Ziel des Projekts war die Erstellung eines Frameworks zur Nutzung von Sensordaten von Androidger�ten auf javaf�higen Endger�ten. 
Um dieses Ziel als erf�llt zu sehen, galt es, die Demonstration der Funktionalit�t dieses Frameworks anhand von drei Beispielanwendungen zu zeigen.
Bei den Beispielanwendungen handelt es sich um eine Maussteuerung, eine Spielsteuerung und die Bedienung eines Murmellabyrinthes mithilfe eines Roboters. 
Damit die Sensordaten an einen Server, welcher unser Framework umsetzt, geschickt werden k�nnen, war es n�tig eine App zu entwickeln.
Bei den Sensordaten handelt es sich um unverarbeitete Daten, welche von Sensoren, wie dem Gyroskop oder dem Lichtsensor, Android-f�higer Handys erfasst werden k�nnen.
Mit dem von uns erstellten Framework soll Drittpersonen die Umsetzung von Projekten, die Sensordaten auf Endger�ten ben�tigen, erheblich erleichtert werden.


# Usecase-Analyse
![Usecase-Diagramm](finalUse.png)

Der App-Benutzer verwendet die App und einen vom Entwickler vorgefertigten Server, um Sensordaten auf eine bestimmte Art zu verwenden. Der Entwickler nutzt das Framework, um einen Server mit gew�nschter Funktionalit�t umzusetzen, der dann von App-Benutzern verwendet werden kann. Hierzu muss das Senden und Empfangen der Sensordaten �ber eine Verbindung zwischen App und Server sowie das Verarbeiten dieser Daten ber�cksichtig werden. Zur Demonstration dienen eine Maussteuerung, eine Robotersteuerung und ein Gamepad zur Spielsteuerung.
Der App-Benutzer ben�tigt lediglich die Psychic-Sensors App auf seinem Handy und muss sich um nichts weiter k�mmern, au�er des Startens eines Psychic-Servers auf einem Endger�t.

Die Maussteuerung soll eine normale Computer-Maus ersetzen k�nnen. Das beinhaltet das Bewegen des Cursors sowie Links- und Rechtsklick.

Die Robotersteuerung ist eine Proof-Of-Concept-Anwendung, die erm�glichen soll, mithilfe eines KUKA LBR iiwa 7 R800 ein Murmellabyrinth zu l�sen. Ein Beispiel f�r ein solches Labyrinth findet man in der [Wikimedia Commons](https://de.wikipedia.org/wiki/Datei:PuzzleOfDexterity.jpg)[^1]. Das Murmellabyrinth soll dabei an der Werkzeugposition des Roboters befestigt sein, zum Beispiel mit einem Greifer. 

Die Spielsteuerung sollte demonstrieren, dass es mit unserer Implementation m�glich ist, Spiele zu steuern, die schnelle Reaktionen erfordern und dass unterschiedliche Sensortypen benutzt werden k�nnen. Wir haben uns f�r einen Controller f�r "Super Mario Kart" der Spielekonsole "Super Nintendo Entertainment System" entschieden. In diesem Spiel ist es erforderlich den gesteuerten Charakter nach Links und Rechts bewegen zu k�nnen. Au�erdem gibt es die M�glichkeit Items zu werfen, und es gibt einen Mehrspielermodus. Wir haben uns f�r dieses Spiel aufgrund der geringen Zahl ben�tigter Buttons entschieden, weil es ohne haptisches Feedback schwierig ist Kn�pfe zu treffen und sich auf das Spiel zu konzentrieren.


[^1]: https://de.wikipedia.org/wiki/Datei:PuzzleOfDexterity.jpg


# Anforderungen
Die zur Demonstration dienenden Anwendungen stellen schwer zu definierende Anforderungen an die Latenz und Frequenz der Sensordaten. Wir haben versucht, uns beim Festlegen der Grenzwerte auf bekannte Ger�te zu beziehen, die keine Probleme bei der Bedienbarkeit haben. Im folgenden werden die von uns aufgestellten Anforderungen definiert.


## Verbindungsqualit�t

### Frequenz
Um eine Mindestanforderung f�r die Frequenz der �bertragung festzulegen, haben wir uns an Spielen f�r Konsolen orientiert, da diese vermutlich �hnliche Anforderungen an Reaktionsgeschwindigkeit stellen wie unsere Beispielimplementationen. Spiele scheinen auf aktuellen Konsolen, zum Beispiel der Xbox One und der PS4, mit mindestens 30 Bildern pro Sekunde zu laufen, siehe zum Beispiel [diese Publikation von Ubisoft](http://blog.ubi.com/watch-dogs-next-gen-game-resolution-dynamism/)[^2]. Deshalb haben wir die zu unterst�tzende Mindestfrequenz auf 30 Sensordaten pro Sekunde festgelegt.

[^2]: http://blog.ubi.com/watch-dogs-next-gen-game-resolution-dynamism/

### Latenz
Eine akzeptable Grenze f�r die Latenz festzulegen war schwierig, da Latenzen im Millisekundenbereich schwer zu messen sind und keine klare Grenze zu akzeptablen Latenzen existiert. Wir haben daher versucht, uns �ber die Latenzen professionell hergestellter kabelloser Eingabeger�te zu informieren. Leider ist auch das schwierig, da Hersteller dazu meist keine Informationen ver�ffentlichen. Um dennoch eine maximale Latenz festlegen zu k�nnen, haben wir die Eingabeverz�gerung von Konsolen untersucht. In [diesem Artikel von www.eurogamer.net](http://www.eurogamer.net/articles/digitalfoundry-lag-factor-article?page=2)[^3] werden die in Spielen auftretenden Eingabeverz�gerungen untersucht. Zwischen den Spielen wurde eine Differenz von mehr als 50ms festgestellt. Da auch die Spiele mit h�herer Eingabeverz�gerung bedienbar sind, sollte eine Latenz unter diesem Wert keine Probleme verursachen.

[^3]: http://www.eurogamer.net/articles/digitalfoundry-lag-factor-article?page=2

### Jitter
Ein dritter Parameter f�r die Verbindungsqualit�t ist der Jitter, das hei�t wie sehr sich die Periodizit�t der ankommenden Sensordaten von der Periodizit�t der gesendeten Sensordaten unterscheidet. Je geringer der Jitter ist, desto besser ist die Verbindung. Unsere einzige Anforderung an diesen Aspekt der Verbindung war, dass sich kein Jitter bemerkbar macht.

## Maussteuerung
Zus�tzlich zu den Anforderungen an die Netzwerkparameter, die fl�ssige und direkte Bewegung des Maus-Cursors auf dem Bildschirm garantieren sollen, muss die Maus zumindest auch einen Links- und Rechtsklick zur Verf�gung stellen, um eine normale Maus zu emulieren.

## Spielsteuerung
Um die Anforderungen an die Spielbarkeit festzulegen, haben wir die Zeiten einiger L�ufe mit nativen Controllern auf der ersten Karte des Spiels, "Mario Circuit 1", gemessen. Dadurch hatten wir einen Vergleichswert von 1:20, die wir mit unserem Controller mindestens erreichen wollten, so dass dieser einem nativen Controller �hnlich ist.

Au�erdem sollte die Anordnung der Buttons in etwa dem nativen Controller entsprechen, weshalb ein flexibles Layouting vom Server aus m�glich sein muss.

Um die Itemmechanik von "Super Mario Kart" zu unterst�tzen, wollten wir lineare Bewegungen in einer separaten Achse auswerten, was die gleichzeitige Nutzung mehrerer Sensoren erforderte.

Da wir den Mehrspielermodus des Spiels ebenfalls nutzen wollten, musste die Verbindung mehrerer Clients zur gleichen Zeit unterst�tzt werden.

## Robotersteuerung
Es sollte mit der Robotersteuerung m�glich sein, ein Murmellabyrinth zu l�sen. Abgesehen davon stellte die Steuerung keine Anforderungen, die nicht bereits durch die Maussteuerung und der Spielsteuerung gestellt wurden, da die Anforderungen an Latenz und Frequenz nicht h�her sind, und Buttons auch schon von der Maus- und Spielsteuerung ben�tigt werden.


## Wiederverwendbarkeit
Da wir ein entwicklerfreundliches Framework erstellen wollten, mussten wir darauf achten dass unser Projekt nicht nur f�r unsere Beispiele nutzbar ist. Es sollte nicht notwendig sein, die App zu ver�ndern, um andere Applikationen zu entwickeln. Wichtig war auch, alle m�glichen Anforderungen an die Nachbearbeitung der Daten auf dem Server unterst�tzen zu k�nnen.
Des Weiteren ist f�r die Entwicklerfreundlichkeit wichtig �ber eine gute Code-Dokumentation zu verf�gen sowie die Software-Architektur leicht erweitern zu k�nnen.

Au�erdem wollten wir die Anforderungen an die Endger�te f�r den Server und das Handy f�r die App m�glichst gering halten.


## Bedienbarkeit
Die App sollte m�glichst benutzerfreundlich erstellt sein. Das bedeutete f�r uns, m�glichst wenig Konfiguration vom Nutzer zu fordern.


# Analyse unserer Ergebnisse
Aus den Requirements ist ein Client-Server-Framework entstanden. Die Clients laufen in einer App auf Android-Ger�ten, und die Server sind Java-Anwendungen auf Endger�ten. Mit unserem Framework k�nnen andere Entwickler einfach Daten nutzen, die die Clients mithilfe ihrer Sensoren generieren und dann an den Server schicken, indem sie eine Java-Klasse erweitern. Im Folgenden stellen wir unsere Ergebnisse kurz vor und vergleichen sie mit den gestellten Requirements.

## Verbindungsqualit�t

\newpage
### Frequenz
![Frequenz-Ergebnisgraph (y-Achse in `ns`, x-Achse Messungen seit Beginn)](frequenz_ergebnisgraph.png)

Die blaue Linie gibt die Differenz zwischen den Ankunftszeiten der Sensordaten auf dem Server an. Die gelbe Linie ist der Durchschnitt dieser Zeiten, und die rote Linie im Hintergrund ist die Differenz zwischen den Zeitstempeln der Sensordaten.

Wie zu sehen ist, betr�gt der durchschnittliche zeitliche Abstand zwischen Sensordaten ungef�hr 20ms, was in einer Frequenz von 50Hz resultiert. Da 50Hz unsere Anforderung von 30 Sensordaten pro Sekunde deutlich �berschreitet, sehen wir unsere Anforderung an die Updatefrequenz als mehr als erf�llt an.

\newpage
### Latenz
![RTT-Ergebnisgraph (y-Achse in `ms`, x-Achse Messungen seit Beginn)](rtt_ergebnisgraph.png)

Die y-Achse ist in Millisekunden angegeben, die x-Achse gibt die Nummer der Messung seit ihrem Beginn an. Die blaue Linie ist die Round-Trip-Time zwischen Server und Client f�r ein Kontrollpaket, dass vom Server geschickt wird und vom Client sofort beantwortet wird. Die gestrichelte gr�ne Linie ist der Mittelwert der RTTs. Wir haben die RTT als Messwert verwendet, damit wir uns nicht auf eine schwierige und potenziell ungenaue Uhrensynchronisation auf Server und Client verlassen m�ssen.

Wie zu sehen ist, liegt die durchschnittliche Round-Trip-Time bei 50ms; die Latenz zwischen Generierung der Sensordaten auf Clients und Eintreffem auf dem Server wird dementsprechend ungef�hr bei 25 Millisekunden liegen. Wir sehen unsere Anforderung an die Latenz damit erf�llt.

\newpage
### Jitter
![Jitter (y-Achse in `ms`, x-Achse Messungen seit Beginn)](jitter_ergebnisgraph.png)

Die blaue Linie gibt den Betrag der Differenz zwischen den Periodizit�ten der Sensordaten auf dem Server in Millisekunden an, die gestrichelte gr�ne Linie den durchschnittlichen Betrag des Jitters. Wie zu sehen ist, liegt der Betrag durchschnittlich bei ungef�hr 3ms und zumeist unter 5ms.

In der Praxis waren diese Differenzen sowie die seltenen Spitzen nie zu sp�ren. Da die Zeitstempel der Sensordaten erm�glichen es au�erdem, versp�tete Pakete zu ignorieren. Insofern stellt Jitter f�r uns kein Problem dar.

## Anforderungen der drei Steuerungen
Alle von uns geplanten Steuerungen wurden erfolgreich umgesetzt.
Unsere Anforderungen an die Netzwerkverbindungsqualit�t haben die Mindestanforderungen der drei Beispielimplementationen erreicht oder �bertroffen, da die Steuerungen keine Probleme mit schlechten Reaktionszeiten zeigen.

### Maussteuerung
Mit den Buttons in der App ist es m�glich, alle Funktionen einer einfachen Maus zu ersetzen: Man kann die Maus bewegen, auf Elemente des Bildschirms klicken, und klicken und ziehen, so dass auch das Scrollen durch Inhalte problemlos m�glich ist. 

### Spielsteuerung
Nach ein wenig Eingew�hnungszeit ist es uns mit unserem Controller routinem��ig gelungen, die geforderten 1:20 zu unterbieten; es ist au�erdem m�glich, den Zwei-Spieler-Modus mit zwei Handys zu spielen. Die Itemwurfmechanik wird ebenfalls sowohl f�r r�ckw�rts als auch f�r vorw�rtsgewandte W�rfe unterst�tzt. 

### Robotersteuerung
Da wir unsere Steuerung leider nur in der Simulationsumgebung von V-REP testen k�nnen, k�nnen wir die erfolgreiche L�sung eines Murmellabyrinthes nicht testen. Wir gehen jedoch aufgrund unserer Erfahrungen mit der Maus- und Spielsteuerung sowie den Ergebnissen der Simulation davon aus, dass es m�glich ist, ein Murmellabyrinth mit unserer Robotersteuerung zu l�sen.

## Wiederverwendbarkeit
Letztlich ist es sehr einfach geworden, einen neuen Server zu implementieren, und es existiert eine umfassende Dokumentation sowie drei Beispiele, die bei einer neuen Implementation zu Hilfe stehen.
Mit unserer Unterst�tzung fast aller Sensoren sowie flexibler Buttonlayouts k�nnen unterschiedlichste Anwendungen entwickelt werden. Durch unsere Wahl Javas als Implementationssprache laufen Server auf Windows, Linux und Apple-Ger�ten, die JDK 8 oder h�her installiert haben. Um die Verbindung zu einem Handy aufzubauen, m�ssen sich Server und Client im gleichen WLAN-Netzwerk mit Zwischenclientkommunikation befinden, ein Zustand, der in den meisten Heimnetzwerken gegeben ist. Alle Android-Ger�te mit Unterst�tzung f�r API-Level 19, also Ger�te mit mindestens Android 4.4 k�nnen die PsychicSensors-App installieren und werden somit als Client unterst�tzt.

Da wir uns gegen eine native Implementation von Eingabemethoden, zum Beispiel �ber Blutooth HID-Profile, entschieden haben, ist der Entwickler nur durch die Java-Umgebung beschr�nkt, so lange er die gew�nschten Funktionen nicht in einer nativen Programmiersprache umsetzt.

Wir sehen daher unsere Anforderung an das Framework, nicht nur von uns genutzt werden zu k�nnen, als erf�llt an.

## Bedienbarkeit der App
Das User Interface der App ist recht minimalistisch gehalten. Mit einem Knopfdruck werden Server gesucht, mit einem weiteren wird die Verbindung zu einem der gefundenen Server aufgebaut.
Es ist dem Nutzer m�glich, eine Empfindlichkeit f�r die Sensoren festzulegen, die der Server dann je nach Anwendung interpretieren kann. Falls der Standard-Discovery-Port auf dem Endger�t auf dem der Server l�uft blockiert ist, kann dieser in der App ge�ndert werden. Insgesamt erf�llt die App unsere Anforderungen an den Client, auch f�r gelegentliche Nutzer verst�ndlich zu sein, sehr gut.

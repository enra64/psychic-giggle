# Architektur
Das Psychic-Framework ist dreigeteilt in die App, den Server und einen gemeinsamen Kern.

## Gemeinsamer Kern
Im gemeinsamen Kern (das ```common```-Package) sind alle Klassen enthalten, die sowohl vom Server als auch von der App benötigt werden.

## App
Die App ist dafür zuständig, den Nutzer Server finden zu lassen und mit Servern zu kommunizieren. Sie erlaubt es dem Nutzer außerdem, die Sensitivität für alle Sensoren zu ändern und den Gerätenamen festzulegen.
Wenn der Nutzer sich dafür entschieden hat, mit einem Server in Verbindung zu treten, wird die App anfangen, die Daten der angefragten Sensoren zu übermitteln. Außerdem stellt sie die vom Implementator angefragten Buttons dar, und Benachrichtigt den Server über Knopfdrücke.

## Server
Der Server-Teil des Psychic-Frameworks dient dazu, die Erstellung von neuen Servern möglichst einfach zu machen. Beispiele lassen befinden sich im ```examples```-Package.

# Kommunikation
Alle Daten und Commands werden als serialisierte Java-Objekte übertragen.

## Server-Discovery
![Sequenzdiagramm der Discovery-Phase](discovery seqdia.png)
Grundlage der Serverfindung auf PC und Handy ist der sogenannte  ```DiscoveryThread```, in dem sich Funktionen befinden, die von Server und Client benötigen. Wichtig sind für die Funktionsweise insbesondere ```sendSelfID```, ```listen``` und der ```onDiscovery```-Callback. 

```sendSelfID``` sendet die für weitere Kommunikation notwendigen Informationen in Form eines sich selbst beschreibenden ```NetworkDevice```. Der Client verwendet diese Funktion, um Server via Broadcast zu finden: Empfängt der Server ein solches Paket, wird ```onDiscovery``` aufgerufen, und der ```DiscoveryServer``` schickt an den Sender des Paketes seine eigene Identifikation via ```sendSelfID```. Dann ist der Server dem ```DiscoveryClient``` bekannt, und der ```onDiscovery```-Callback wird aufgerufen.

```listen``` ist auf Client und Server dafür zuständig, per ```sendSelfID``` gesendete Pakete zu empfangen, und den ```onDiscovery```-Callback auszulösen.

Der Port, auf dem der Server die Broadcasts erwartet, muss in der App eingegeben werden, falls nicht der Standardport ```8888``` verwendet wird.

## Datenverbindung
Der Server wartet für jeden Client auf einem eigenen Port auf Daten, die via UDP gesendet werden. Dieser Port wird vom Server gewählt und in der Discovery-Phase dem Client mitgeteilt; es ist ein zufälliger freier Port.

## Kontrollverbindung
Der Server und die App warten jeweils auf zufällig gewählten freien Port; diese werden in der Discovery-Phase ausgetauscht. Die Kontrollverbindung läuft über TCP; für jede neue Kontrollnachricht (```AbstractCommand```) wird eine neue Verbindung aufgebaut.
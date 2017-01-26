#Developer Guide

Working Title: psychic giggle

ist ein Framework zur Nutzung von Android-Sensoren auf PCs zur einfachen und schnellen Verwendung von Sensordaten auf dem Computer.


Verwendung:

Um neue Funktionalitäten zu erstellen ist es nötig eine von AbstractServer erbende Klasse zu instanziieren.




 
Zur Erstellung minimaler Funktionalität wird benötigt:

```Java
public class ExampleServer implements NetworkDataSink {
	public ExampleServer() throws IOException {
		Server server = new Server();
		server.registerDataSink(this, SensorType.LinearAcceleration);
		server.start();
	}

	public void onData(NetworkDevice origin, SensorData data, float userSensitivity){
		
	}
	
	public void close(){
	
	}
}

```

Im Konstruktor wurde ein Server erstellt und ```this ```wurde als NetworkDataSink für den LinearAcceleration-Sensor registriert.
Die ankommenden Daten können in der ```onData ``` verarbeitet werden. Hierzu muss auf die Klassenvariable ```data``` in SensorData zugegriffen werden.
In der ```close()``` sollten alle verwendeten Ressourcen freigegeben werden.


#Verwendung von Buttons
```Java
public class ExampleServer implements NetworkDataSink, ButtonListener {
	public ExampleServer() throws IOException {
		Server server = new Server();
		server.registerDataSink(this, SensorType.LinearAcceleration);
		server.start();

		server.setButtonListener(this);
		server.addButton("Buttontext", 0);
		//server.setButtonLayout(xmlFileContent);
	}

	public void onButtonClick(ButtonClick click , NetworkDevice origin){

	}
}

```
 
Im Konstruktor muss ein ButtonListener gesetzt werden. Daraufhin können Buttons mit Text und ID hinzugefügt werden. 
Buttoninputs können dann in der onButtonClick verarbeitet werden. Die ID des gedrückten Buttons steht im ButtonClick-Objekt.

Eine weitere Möglichkeit ist die Verwendung von setButtonLayout(). Hierbei kann eine eigene Android XML Layout Datei als String übergeben werden. Es werden nur LinearLayout und Buttons unterstützt, die View IDs der
Buttons werden auch als ID der Buttons im Framework verwendet. 
Bei Verwendung von setButtonLayout werden alle durch addButton hinzugefügten Buttons entfernt und umgekehrt.


#Verwaltung von Clients


#Exceptionhandling

#Resetevents


 
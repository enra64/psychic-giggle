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

Im Konstruktor wurde ein Server erstellt und ```Java this ```wurde als NetworkDataSink für den LinearAcceleration-Sensor registriert.
Die ankommenden Daten können in der ```Java onData ``` verarbeitet werden. Hierzu muss auf Klassenvariable ```Java data``` in SensorData zugegriffen werden.
In der ```Java close()``` sollten alle verwendeten Ressourcen freigegeben werden.

 
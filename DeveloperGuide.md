# Developer Guide

## Working Title: psychic giggle
ist ein Framework zur Nutzung von Android-Sensoren auf PCs zur einfachen und schnellen Verwendung von Sensordaten auf dem Computer.


# Grundlegende Verwendung:
Zur Erstellung minimaler Funktionalität wird folgender Code benötigt:

```Java
public class ExampleServer implements NetworkDataSink {
	public ExampleServer() throws IOException {
		Server server = new Server();
		server.registerDataSink(this, SensorType.LinearAcceleration);
		server.start();
	}

	public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity){
	}

	public void close(){
	}
}
```

Im Konstruktor wurde ein Server erstellt und ```this``` wurde als NetworkDataSink für den LinearAcceleration-Sensor registriert.
Nachdem der Server mit ```start()``` gestartet wurde, können die ankommenden Daten in ```onData``` verarbeitet werden. Hierzu muss auf ```sensorData.data``` zugegriffen werden, ein ```float[]```, das die Sensordaten enthält.
In der ```close()``` sollten alle verwendeten Ressourcen freigegeben werden.

## ```NetworkDevice```
Das ```NetworkDevice``` wird vielfach verwendet um Clients und Server zu identifizieren. Mit ```getInetAddress()``` kann die aktuelle IP-Adresse als ```InetAddress``` abgefragt werden, unter ```getName()``` ist der Name des ```NetworkDevice``` verfügbar.

# Sensoren
Die derzeitig unterstützten Sensoren sind:
* Accelerometer
* GameRotationVector
* Gravity
* Gyroscope
* LinearAcceleration
* Magnetometer
* Orientation
* RotationVector
All diese Sensoren sind im ```SensorType```-Enum verbunden.

Sensordaten können mithilfe von ```setSensorOutputRange(SensorType, float)``` für den spezifizierten Sensor normalisiert werden. Der ```float```-Wert ist dabei der maximale Ausschlag sowohl in positiver als auch in negativer Richtung.

Die Update-Frequenz der Android-Sensoren kann mithilfe von ```setSensorSpeed()``` gesetzt werden, unterstützt sind die folgenden Werte:
* [SENSOR_DELAY_FASTEST](https://developer.android.com/reference/android/hardware/SensorManager.html#SENSOR_DELAY_FASTEST)
* [SENSOR_DELAY_GAME](https://developer.android.com/reference/android/hardware/SensorManager.html#SENSOR_DELAY_GAME)
* [SENSOR_DELAY_NORMAL](https://developer.android.com/reference/android/hardware/SensorManager.html#SENSOR_DELAY_NORMAL)
* [SENSOR_DELAY_UI](https://developer.android.com/reference/android/hardware/SensorManager.html#SENSOR_DELAY_UI)

# Verwendung von Buttons
```Java
public class ExampleServer implements ButtonListener {
	public ExampleServer() throws IOException {
		Server server = new Server();
		server.start();

		server.setButtonListener(this);
		server.addButton("Erster Button", 0);
		server.addButton("Zweiter Button", 1);
		//server.setButtonLayout(xmlFileContent);
	}

	public void onButtonClick(ButtonClick buttonClick , NetworkDevice origin){
	}
}

```

Im Konstruktor muss ein ButtonListener gesetzt werden. Daraufhin können Buttons mit ```addButton(String, int)``` hinzugefügt werden. Der ```String``` ist der Text den der Button anzeigt, der ```int``` is die ID, die beim Drücken des Buttons an den Server gesendet wird. Buttons werden auf allen verbundenen Clients angezeigt. Buttoninputs können dann in der ```onButtonClick``` verarbeitet werden. Mithilfe von ```buttonClick.getId()``` kann der Button identifiziert werden, und ```buttonClick.isPressed()``` ist ```true``` wenn der Button gedrückt wurde.

Zum Entfernen einzelner Buttons kann ```removeButtons(int)``` verwendet werden

Eine Alternative ist die Verwendung von ```setButtonLayout(String)```. Hierbei kann eine eigene Android XML Layout Datei als ```String``` übergeben werden. Es werden nur ```LinearLayout```- und ```Button```-Objekte unterstützt. Bei Verwendung von ```setButtonLayout(String)``` werden alle durch ```addButton(String, int)``` hinzugefügten Buttons entfernt und bei Verwendung von ```addButton(String, int)``` wird das durch ```setButtonLayout``` erstellte Layout entfernt.

Ein Beispiel für einen unterstützten XML-String ist das folgende Snippet:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:text="A"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="0"
        android:layout_weight="5"/>
</LinearLayout>
```
Für ```Button``` werden die folgenden Attribute unterstützt:
* ```android:text=""``` enthält den vom Button dargestellten Text
* ```android:id=""``` ist die ID die an den Server übertragen wird, und dort mithilfe von ```ButtonClick.getId()``` abgefragt werden kann
* ```android:layout_weight=""``` wird direkt für den Button gesetzt. Genaue Informationen sind in der [Android-Dokumentation](https://developer.android.com/guide/topics/ui/layout/linear.html#Weight) zu finden.

Für ```LinearLayout``` werden die folgenden Attribute unterstützt:
* ```android:layout_weight=""``` wird direkt für das Layout gesetzt. Genaue Informationen sind in der [Android-Dokumentation](https://developer.android.com/guide/topics/ui/layout/linear.html#Weight) zu finden.
* ```android:orientation=""``` wird direkt für das Layout gesetzt. Genaue Informationen sind in der [Android-Dokumentation](https://developer.android.com/reference/android/widget/LinearLayout.html#attr_android:orientation) zu finden.

Um alle Buttons zu entfernen kann ```clearButtons()``` aufgerufen werden.

# Verwaltung von Clients
```Java
public class ExampleServer implements ClientListener {
	public ExampleServer() throws IOException {
		Server server = new Server();
		server.start();

		server.setClientListener(this);
	}

    public boolean acceptClient(NetworkDevice newClient) {
        return false;
    }

    public void onClientDisconnected(NetworkDevice disconnectedClient) {
    }

    public void onClientTimeout(NetworkDevice timeoutClient) {
    }

    public void onClientAccepted(NetworkDevice connectedClient) {
    }
```
Um die verschiedenen Client events zu handeln, muss ein ```ClientListener``` gesetzt werden, der die Events empfängt.

## Maximale Anzahl Clients
Die maximale Anzahl von Clients ist theoretisch nicht beschränkt. Ein nutzerdefiniertes Maximum kann mithilfe von ```setClientMaximum(int)``` gesetzt werden, mit ```getClientMaximum()``` abgefragt werden und mit ```removeClientMaximum()``` entfernt werden.

## ```acceptClient(NetworkDevice)```
```acceptClient(NetworkDevice)``` wird immer dann aufgerufen, wenn ein neuer Client versucht sich mit dem Server zu verbinden. Die
Addresse des Clients und sein Name sind mit ```newClient.getInetAddress()``` und ```newClient.name``` verfügbar.
Wenn ```acceptClient(NetworkDevice)``` ```true``` zurückgibt, wird der Client angenommen; gibt es ```false``` zurück, wird der Client
abgelehnt.

## ```onClientDisconnected(NetworkDevice)```
```onClientDisconnected``` wird aufgerufen, wenn ein Client die Verbindung beendet hat oder nicht mehr erreichbar ist.
Der Client ist zum Zeitpunkt des Aufrufs nicht mehr über den Server verfügbar.

## ```onClientTimeout(NetworkDevice)```
```onClientTimeout``` wird aufgerufen, wenn ein Client eine zeitlang nicht mehr reagiert. Der Client ist zum Zeitpunkt
des Aufrufs nicht mehr über den Server verfügbar.

## ```onClientAccepted(NetworkDevice)```
```onClientAccepted(NetworkDevice)``` wird aufgerufen wenn  die Kommunikation zwischen Server und dem neuen Client funktioniert. Diese Funktion wird nur dann aufgerufen, wenn ```acceptClient(NetworkDevice)``` ```true``` für das entsprechende ```NetworkDevice``` zurückgegeben hat.

# Exceptionhandling
```Java
public class ExampleServer implements NetworkDataSink, ButtonListener, ClientListener, ExceptionListener {
    public ExampleServer() throws IOException {
        Server server = new Server();
        server.start();

        server.setExceptionListener(this);
    }
    @Override
    public void onException(Object origin, Exception exception, String info) {
    }
}
```
Um alle Exceptions die in verschiedenen Threads auftreten aufzufangen muss ein ```ExceptionListener``` registriert werden.
```onException(Object, Exception, String)``` wird dann aufgerufen, falls eine Exception auftritt, die nicht intern gehandelt werden kann. Der ```origin```

# Resetevents
```Java
public class ExampleServer implements ResetListener {
    public ExampleServer() throws IOException {
        Server server = new Server();
        server.start();

        server.setResetListener(this);
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
    }
}
```
Wenn ein Client den "Reset"-Button auf seinem Handy benutzt, wird die ```onResetPosition(NetworkDevice)``` aufgerufen. Dann sollte der derzeitige Status des Handys zurückgesetzt werden, bei der Beispielimplementation ```MouseServer``` wird zum Beispiel die derzeitige Position des Handys als neuer Nullpunkt gewertet.

# Entfernen einer ```NetworkDataSink```
Wenn eine ```NetworkDataSink``` nicht mehr benötigt wird, zum Beispiel weil der entsprechende Client getrennt wurde, kann sie mit ```unregisterDataSink(NetworkDataSink)``` von allen Sensoren abgemeldet werden, und mit  ```unregisterDataSink(NetworkDataSink, SensorType)``` von bestimmten Sensoren abgemeldet werden. Danach erhält die ```NetworkDataSink``` keine Daten mehr vom Server.

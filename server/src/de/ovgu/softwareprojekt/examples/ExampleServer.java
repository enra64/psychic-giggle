package de.ovgu.softwareprojekt.examples;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;
import de.ovgu.softwareprojekt.callback_interfaces.ClientListener;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojekt.networking.AbstractPsychicServer;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.networking.PsychicServer;

import java.io.IOException;

/**
 * An example server.
 */
public class ExampleServer implements NetworkDataSink, ButtonListener, ClientListener, ExceptionListener, ResetListener {
    private long lastSensorTs = 0, lastServerTs = 0;

    private static final boolean LOG_TIMES = false, FIVE_SENSORS = false;

    public ExampleServer() throws IOException {
        PsychicServer server = new PsychicServer();
        server.start();

        server.setButtonListener(this);
        server.setClientListener(this);
        server.setExceptionListener(this);
        server.setResetListener(this);

        server.setButtonLayout("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:orientation=\"horizontal\">\n" +
                "\n" +
                "    <Button\n" +
                "        android:text=\"A\"\n" +
                "        android:id=\"0\"\n" +
                "        android:layout_weight=\"3\"/>\n" +
                "\n" +
                "    <LinearLayout\n" +
                "        android:orientation=\"vertical\"\n" +
                "        android:layout_weight=\"5\">\n" +
                "\n" +
                "        <Button\n" +
                "            android:text=\"B\"\n" +
                "            android:id=\"1\"\n" +
                "            android:layout_weight=\"2\" />\n" +
                "\n" +
                "        <Button\n" +
                "            android:text=\"C\"\n" +
                "            android:id=\"2\"\n" +
                "            android:layout_weight=\"1\" />\n" +
                "    </LinearLayout>\n" +
                "</LinearLayout>");


        server.registerDataSink(this, SensorType.LinearAcceleration);

        if(FIVE_SENSORS){
            server.registerDataSink(this, SensorType.Gyroscope);
            server.registerDataSink(this, SensorType.Accelerometer);
            server.registerDataSink(this, SensorType.Gravity);
            server.registerDataSink(this, SensorType.MagneticField);
        }

        //server.setSensorSpeed(SensorType.LinearAcceleration, SetSensorSpeed.SensorSpeed.SENSOR_DELAY_FASTEST);
    }

    /**
     * Called when the user presses the reset position button
     *
     * @param origin which device pressed the button
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
    }

    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param sensorData      the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings for the sensor
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        //System.out.println(Arrays.toString(sensorData.data));
        // we want the following information:
        // time since last data package, receipt timestamp, server timestamp

        if(LOG_TIMES && sensorData.sensorType == SensorType.LinearAcceleration){
            long nowTsServer = System.nanoTime();
            long nowTsSensor = sensorData.timestamp;

            long timeDiffServer = nowTsServer - lastServerTs;
            lastServerTs = nowTsServer;

            long timeDiffSensor = nowTsSensor - lastSensorTs;
            lastSensorTs = nowTsSensor;

            long tsDiff = Math.abs(nowTsSensor - nowTsServer);

            // print local ts, local ts diff, remote ts, remote ts diff, diff between local/remote
            System.out.printf("%d,%d,%d,%d,%d", nowTsServer, timeDiffServer, nowTsSensor, timeDiffSensor, tsDiff);
            System.out.println();
        }

        // TODO: also, we want the round trip time for the connection watch data
    }

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    @Override
    public void close() {
    }

    /**
     * Called whenever a button requested by the server is clicked.
     * <p>
     * You may add buttons using {@link AbstractPsychicServer#addButton(String, int) addButton(String, id)}
     * or {@link AbstractPsychicServer#setButtonLayout(String) setButtonLayout(String)}
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
    }

    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        return true;
    }

    /**
     * Called when a client sent a disconnect signal or has disconnected
     *
     * @param disconnectedClient the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
    }

    /**
     * Called when a client hasn't responded in a while
     *
     * @param timeoutClient the client that did not respond
     */
    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
    }

    /**
     * Called when a Client has successfully been connected
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
    }

    /**
     * Called when an exception cannot be gracefully handled.
     *
     * @param origin    the instance (or, if it is a hidden instance, the known parent) that produced the exception
     * @param exception the exception that was thrown
     * @param info      additional information to help identify the problem
     */
    @Override
    public void onException(Object origin, Exception exception, String info) {
    }
}

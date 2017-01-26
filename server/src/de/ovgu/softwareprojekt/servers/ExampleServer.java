package de.ovgu.softwareprojekt.servers;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.AbstractServer;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.networking.Server;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by arne on 1/24/17.
 */
public class ExampleServer implements NetworkDataSink, ButtonListener {
    public ExampleServer() throws IOException {
        Server server = new Server();
        server.registerDataSink(this, SensorType.LinearAcceleration);
        server.start();

        server.setButtonListener(this);
    }

    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param sensorData            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings for the sensor
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        System.out.println(Arrays.toString(sensorData.data));
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
     * You may add buttons using {@link AbstractServer#addButton(String, int) addButton(String, id)}
     * or {@link AbstractServer#setButtonLayout(String) setButtonLayout(String)}
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {

    }
}

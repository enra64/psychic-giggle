package de.ovgu.softwareprojekt.servers;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;
import de.ovgu.softwareprojekt.callback_interfaces.ClientListener;
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
public class ExampleServer implements NetworkDataSink, ButtonListener, ClientListener {
    public ExampleServer() throws IOException {
        Server server = new Server();
        server.registerDataSink(this, SensorType.LinearAcceleration);
        server.start();

        server.setButtonListener(this);
        server.setClientListener(this);
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

    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        return false;
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
}

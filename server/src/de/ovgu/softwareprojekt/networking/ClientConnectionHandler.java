package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

@SuppressWarnings("WeakerAccess")
class ClientConnectionHandler {
    /**
     * Name of the server as displayed to discovery clients
     */
    private String mServerName;

    /**
     * Sensors required on the android devices
     */
    private Set<SensorType> mRequiredSensors;

    /**
     * Store requested speeds for all data sinks
     */
    private EnumMap<SensorType, SetSensorSpeed.SensorSpeed> mSensorSpeeds = new EnumMap<>(SensorType.class);

    /**
     * store requested output ranges for all sensors
     */
    private EnumMap<SensorType, Float> mSensorOutputRanges = new EnumMap<>(SensorType.class);

    /**
     * list of buttons that should be displayed on all clients
     */
    private Map<Integer, String> mButtonList = new HashMap<>();

    /**
     * This is the list of all currently bound clients.
     */
    private List<ClientConnection> mClientConnections = new ArrayList<>();

    /**
     * Who to notify about unhandleable exceptions
     */
    private ExceptionListener mExceptionListener;

    /**
     * Who to notify about commands
     */
    private OnCommandListener mCommandListener;

    /**
     * This is listener is used to notify of timeouts
     */
    private ClientListener mClientListener;

    /**
     * the data sink where all data produced by the clients is put into
     */
    private DataSink mDataSink;

    /**
     * AndroidXML for Buttonlayout
     * is null if no AndroidXML is given or the ButtonMap is used
     */
    private String mButtonXML = null;

    ClientConnectionHandler(String serverName, ExceptionListener exceptionListener, OnCommandListener commandListener, ClientListener clientListener, DataSink dataSink) {
        mServerName = serverName;
        mExceptionListener = exceptionListener;
        mCommandListener = commandListener;
        mClientListener = clientListener;
        mDataSink = dataSink;
    }

    /**
     * Creates a new correctly initialized unbound handler
     *
     * @throws IOException if the handler could not be initialized
     */
    ClientConnection getUnboundHandler() throws IOException {
        ClientConnection newHandler = new ClientConnection(
                mServerName,
                mExceptionListener,
                mCommandListener,
                mClientListener,
                mDataSink);

        // set the requested output ranges
        for (Map.Entry<SensorType, Float> sensor : mSensorOutputRanges.entrySet())
            newHandler.setOutputRange(sensor.getKey(), sensor.getValue());

        return newHandler;
    }

    /**
     * Add a new handler to the list of bound handlers and configure the client
     *
     * @param handler a bound handler
     * @throws IOException if the client could not be configured
     */
    void addHandler(ClientConnection handler) throws IOException {
        mClientConnections.add(handler);

        // send the sensor- and button requirements to the new client
        handler.updateSensors(mRequiredSensors);
        updateButtons();
        handler.updateSpeeds(mSensorSpeeds.entrySet());
    }

    /**
     * Close all client connections
     */
    void closeAll() {
        for (ClientConnection clientHandler : mClientConnections)
            clientHandler.close();
    }

    /**
     * This function find the connection handler that is managing a certain client, which is identified only by
     * his address
     *
     * @param address the address of the client that should be found
     * @return null if no matching handler was found, or the handler.
     */
    ClientConnection getClientHandler(InetAddress address) {
        for (ClientConnection clientConnection : mClientConnections)
            // true if the clients match
            if (clientConnection.getClient().address.equals(address.getHostAddress()))
                return clientConnection;
        return null;
    }

    /**
     * Add a button to be displayed on the clients
     * clears XMLLayout
     *
     * @param name text to be displayed on the button
     * @param id   id of the button. ids below zero are reserved.
     */
    void addButton(String name, int id) throws IOException {
        // reserve id's below zero
        assert (id >= 0);

        // add the new button to local storage
        mButtonList.put(id, name);
        //in Case ButtonMap is used after ButtonXML -> ButtonXML has to be set null again
        mButtonXML = null;

        // update the button list on our clients
        updateButtons();
    }

    /**
     * Remove a button from the clients
     * clears XMLLayout
     *
     * @param  id id of the button
     */
    void removeButton(int id) throws IOException {
        // remove the button from local storage
        mButtonList.remove(id);
        //in Case ButtonMap is used after ButtonXML -> ButtonXML has to be set null again
        mButtonXML = null;

        // update the button list on our clients
        updateButtons();
    }

    /**
     * sets mButtonXML to an incoming XML-String
     *
     * @param xml valid android XML layout using only linear layout and button
     *            if string is null ButtonMap will be used
     */
    void setButtonLayout(@Nullable String xml) throws IOException {
        this.mButtonXML = xml;

        updateButtons();
    }

    /**
     * Change the speed of a sensor. The default speed is the GAME speed.
     *
     * @param sensor the sensor to change
     * @param speed  the speed to use for sensor
     */
    void setSensorSpeed(SensorType sensor, SetSensorSpeed.SensorSpeed speed) throws IOException {
        // change the speed for sensor x
        mSensorSpeeds.put(sensor, speed);

        // update on all clients
        updateSensorSpeeds();
    }

    /**
     * Change the output range of a sensor
     *
     * @param sensor      affected sensor
     * @param outputRange the resulting maximum and negative minimum of the sensor output range. Default is -100 to 100.
     */
    public void setSensorOutputRange(SensorType sensor, float outputRange) {
        for (ClientConnection connectionHandler : mClientConnections)
            connectionHandler.setOutputRange(sensor, outputRange);
        mSensorOutputRanges.put(sensor, outputRange);
    }

    /**
     * This function find the connection handler that is managing a certain client
     *
     * @param client the client that should be found
     * @return null if no matching handler was found, or the handler.
     */
    public ClientConnection getClientHandler(NetworkDevice client) {
        for (ClientConnection clientConnection : mClientConnections)
            // true if the clients match
            if (clientConnection.getClient().equals(client))
                return clientConnection;
        return null;
    }

    boolean close(NetworkDevice client) {
        ClientConnection connectionHandler = getClientHandler(client);

        // return false if no matching client could be found
        if (connectionHandler == null)
            return false;

        // close the client connection
        connectionHandler.closeAndSignalClient();
        return true;
    }

    /**
     * Force each client to update his buttons
     * chooses between {@link ClientConnection#updateButtons(String)} and {@link ClientConnection#updateButtons(Map)}
     * if {@link #mButtonXML} is given, it is preferred over [{@link #mButtonList}
     * @throws IOException if the command could not be sent
     */
    private void updateButtons() throws IOException {
        for (ClientConnection client : mClientConnections) {
            if (mButtonXML != null) {
                client.updateButtons(mButtonXML);
            }
            else if(mButtonList != null){
                client.updateButtons(mButtonList);
            }
        }
    }

    /**
     * Force each client to update the sensor speeds
     *
     * @throws IOException if an update command could not be sent
     */
    private void updateSensorSpeeds() throws IOException {
        for (ClientConnection client : mClientConnections)
            client.updateSpeeds(mSensorSpeeds.entrySet());
    }


    /**
     * Force each client to update his required sensors
     *
     * @throws IOException if the command could not be sent
     */
    void updateSensors(Set<SensorType> requiredSensors) throws IOException {
        mRequiredSensors = requiredSensors;
        for (ClientConnection client : mClientConnections)
            client.updateSensors(mRequiredSensors);
    }
}

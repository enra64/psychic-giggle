package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ClientListener;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.CommandType;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

/**
 * The client connection manager handles the expected client state (sensor speeds, button config etc),
 * as well as the ClientConnection instances used to allow communication with the clients.
 * <p>
 * Configuration functions include:
 * <ul>
 * <li>{@link #addButton(String, int)}</li>
 * <li>{@link #setButtonLayout(String)}</li>
 * <li>{@link #setSensorSpeed(SensorType, SetSensorSpeed.SensorSpeed)}</li>
 * </ul>
 */
@SuppressWarnings("WeakerAccess")
class ClientConnectionManager implements ClientListener, UnexpectedClientListener {
    /**
     * This interface must be called if a client is lost to allow the {@link AbstractPsychicServer} to react
     */
    private final ClientLossListener mClientLossListener;

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
     * list of buttons that should be displayed on all clients
     */
    private Map<Integer, String> mButtonMap = new HashMap<>();

    /**
     * This is the list of all currently bound clients.
     */
    private final List<ClientConnection> mClientConnections = new ArrayList<>();

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
    private NetworkDataSink mDataSink;

    /**
     * This string is either null or the current description of the button layout.
     */
    private String mButtonXML = null;


    /**
     * Create a new ClientConnectionManager
     *
     * @param serverName        how the server is called
     * @param exceptionListener what should be called if an exception occurs in managed code
     * @param commandListener   who should be notified of commands
     * @param clientListener    who should be notified of client events
     * @param dataSink          who should get all the data
     */
    ClientConnectionManager(
            String serverName,
            ExceptionListener exceptionListener,
            OnCommandListener commandListener,
            ClientListener clientListener,
            NetworkDataSink dataSink,
            ClientLossListener clientLossListener) {
        mServerName = serverName;
        mExceptionListener = exceptionListener;
        mCommandListener = commandListener;
        mClientListener = clientListener;
        mDataSink = dataSink;
        mClientLossListener = clientLossListener;
    }

    /**
     * Creates a new correctly initialized unbound handler
     *
     * @return an unbound handler that must be added using {@link #addHandler(ClientConnection)} to make it part of the
     * clients managed by this {@link ClientConnectionManager}
     * @throws IOException if the handler could not be initialized
     */
    ClientConnection getUnboundHandler() throws IOException {
        return new ClientConnection(
                mServerName,
                mExceptionListener,
                mCommandListener,
                this,
                mDataSink,
                this);
    }

    /**
     * Add a new handler to the list of bound handlers and configure the client
     *
     * @param handler a bound handler
     * @throws IOException if the client could not be configured
     */
    void addHandler(ClientConnection handler) throws IOException {
        synchronized (mClientConnections) {
            mClientConnections.add(handler);
        }

        // send the sensor- and button requirements to the new client
        handler.updateSensors(mRequiredSensors);
        updateButtons();
        handler.updateSpeeds(mSensorSpeeds.entrySet());
    }

    /**
     * Close all client connections
     */
    void closeAll() {
        synchronized (mClientConnections) {
            Iterator<ClientConnection> it = mClientConnections.iterator();

            while (it.hasNext()) {
                ClientConnection connection = it.next();
                connection.closeAndSignalClient();
                it.remove();
            }
        }
    }

    /**
     * This function find the connection handler that is managing a certain client, which is identified only by
     * his address
     *
     * @param address the address of the client that should be found
     * @return null if no matching handler was found, or the handler.
     */
    ClientConnection getClientHandler(InetAddress address) {
        synchronized (mClientConnections) {
            for (ClientConnection clientConnection : mClientConnections)
                // true if the clients match
                if (clientConnection.getClient().address.equals(address.getHostAddress()))
                    return clientConnection;
        }
        return null;
    }

    /**
     * Add a button to be displayed on the clients
     * clears XMLLayout
     *
     * @param name text to be displayed on the button
     * @param id   id of the button. ids below zero are reserved.
     * @throws IOException if the required command could not be sent to all clients
     */
    void addButton(String name, int id) throws IOException {
        // reserve id's below zero
        assert (id >= 0);

        // add the new button to local storage
        mButtonMap.put(id, name);
        //in Case ButtonMap is used after ButtonXML -> ButtonXML has to be set null again
        mButtonXML = null;

        // update the button list on our clients
        updateButtons();
    }

    /**
     * Remove a button from the clients
     * clears XMLLayout
     *
     * @param id id of the button
     * @throws IOException if the required command could not be sent to all clients
     */
    void removeButton(int id) throws IOException {
        // remove the button from local storage
        mButtonMap.remove(id);
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
     * @throws IOException if the required command could not be sent to all clients
     */
    void setButtonLayout(@Nullable String xml) throws IOException {
        this.mButtonXML = xml;

        updateButtons();
    }

    /**
     * Remove all buttons added. Clears both buttons added using {@link #addButton(String, int)} and layouts created using
     * {@link #setButtonLayout(String)}.
     *
     * @throws IOException if the required command could not be sent to all clients
     */
    public void clearButtons() throws IOException {
        mButtonMap.clear();
        mButtonXML = null;
        updateButtons();
    }

    /**
     * Change the speed of a sensor. The default speed is the GAME speed.
     *
     * @param sensor the sensor to change
     * @param speed  the speed to use for sensor
     * @throws IOException if the required command could not be sent to all clients
     */
    void setSensorSpeed(SensorType sensor, SetSensorSpeed.SensorSpeed speed) throws IOException {
        // change the speed for sensor x
        mSensorSpeeds.put(sensor, speed);

        // update on all clients
        updateSensorSpeeds();
    }

    /**
     * This function find the connection handler that is managing a certain client
     *
     * @param client the client that should be found
     * @return null if no matching handler was found, or the handler.
     */
    public ClientConnection getClientHandler(NetworkDevice client) {
        synchronized (mClientConnections) {
            for (ClientConnection clientConnection : mClientConnections)
                // true if the clients match
                if (clientConnection.getClient().equals(client))
                    return clientConnection;
        }
        return null;
    }

    /**
     * Close the ClientConnection handling a client
     *
     * @param client the NetworkDevice that is no longer required
     * @return true if the client was found
     */
    boolean close(NetworkDevice client) {
        ClientConnection connectionHandler = getClientHandler(client);

        // return false if no matching client could be found
        if (connectionHandler == null)
            return false;

        // delete the connection handler
        synchronized (mClientConnections) {
            mClientConnections.remove(connectionHandler);
        }

        // close the client connection
        connectionHandler.closeAndSignalClient();
        return true;
    }

    /**
     * Force each client to update his buttons
     * chooses between {@link ClientConnection#updateButtons(String)} and {@link ClientConnection#updateButtons(Map)}
     * if {@link #mButtonXML} is given, it is preferred over [{@link #mButtonMap}
     *
     * @throws IOException if the update command could not be sent to a client
     */
    private void updateButtons() throws IOException {
        synchronized (mClientConnections) {
            for (ClientConnection client : mClientConnections) {
                if (mButtonXML != null) {
                    client.updateButtons(mButtonXML);
                } else if (mButtonMap != null) {
                    client.updateButtons(mButtonMap);
                }
            }
        }
    }

    /**
     * Force each client to update the sensor speeds
     *
     * @throws IOException if an update command could not be sent
     */
    private void updateSensorSpeeds() throws IOException {
        synchronized (mClientConnections) {
            for (ClientConnection client : mClientConnections)
                client.updateSpeeds(mSensorSpeeds.entrySet());
        }
    }

    /**
     * Send a notification to all known devices
     *
     * @param id      notification id. must be unique on all devices
     * @param title   the notification title
     * @param content the notification content
     * @throws IOException if a device could not be requested to display the notification
     */
    public void displayNotification(int id, String title, String content) throws IOException {
        for (ClientConnection connection : mClientConnections)
            connection.displayNotification(id, title, content);
    }

    /**
     * Force each client to update his required sensors
     *
     * @param requiredSensors a set of sensors that each client must enable
     * @throws IOException if the required command could not be sent to all clients
     */
    synchronized void updateSensors(Set<SensorType> requiredSensors) throws IOException {
        mRequiredSensors = requiredSensors;
        synchronized (mClientConnections) {
            for (ClientConnection client : mClientConnections)
                client.updateSensors(mRequiredSensors);
        }
    }

    /**
     * Forward the accept client message to the client listener
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        return mClientListener.acceptClient(newClient);
    }

    /**
     * Called when a ClientConnection detects disconnection of its device
     *
     * @param disconnectedClient the client that did not respond
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
        // remove the client from the list of known connections
        synchronized (mClientConnections) {
            mClientConnections.remove(getClientHandler(disconnectedClient));
        }

        // forwardData the disconnect
        mClientListener.onClientDisconnected(disconnectedClient);

        // notify client loss listener
        mClientLossListener.onClientLoss();
    }

    /**
     * Called when a ClientConnection detects timeout of its device
     *
     * @param timeoutClient the client that did not respond
     */
    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        // remove the client from the list of known connections
        synchronized (mClientConnections) {
            mClientConnections.remove(getClientHandler(timeoutClient));
        }

        // forwardData the timeout
        mClientListener.onClientTimeout(timeoutClient);

        // notify loss listener
        mClientLossListener.onClientLoss();
    }

    /**
     * This is called when a command is received by a ClientConnection should handle another client
     *
     * @param badClient address of the client sending to the wrong ClientConnection
     * @param command   the command that was received
     */
    @Override
    public void onUnexpectedClient(InetAddress badClient, AbstractCommand command) {
        // let the client register as usual, so only respond to other commands
        if (command.getCommandType() != CommandType.ConnectionRequest) {
            ClientConnection actualConnection = getClientHandler(badClient);
            System.out.println("remapped " + badClient);
            try {
                actualConnection.enforcePorts();
            } catch (IOException e) {
                mExceptionListener.onException(this, e, "Could not enforce ports for bad client " + badClient);
            }
        }
    }

    /**
     * Called when the ClientConnection confirms connection success. We just forwardData here
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        mClientListener.onClientAccepted(connectedClient);
    }

    /**
     * sends a description for the sensortype
     *
     * @param type        affected sensor
     * @param description description for the affected sensor
     * @throws IOException if the sensor description could not be sent
     */
    public void sendSensorDescription(SensorType type, String description) throws IOException {
        synchronized (mClientConnections) {
            for (ClientConnection con : mClientConnections)
                con.sendSensorDescription(type, description);
        }
    }

    /**
     * Hides Reset Button if not in use
     * @param isHidden
     * @throws IOException is thrown if command could not be sent
     */
    public void hideResetButton(boolean isHidden) throws IOException{
        synchronized (mClientConnections) {
            for (ClientConnection con : mClientConnections)
                con.hideResetButton(isHidden);
        }
    }

    /**
     * Retrieve the sensor range of a sensor on a device
     *
     * @param client the device with the sensor
     * @param sensor the sensor
     * @return the range of the sensor, see <a href="https://developer.android.com/reference/android/hardware/Sensor.html#getMaximumRange()">android docs</a>
     */
    float getSensorMaximumRange(NetworkDevice client, SensorType sensor) {
        return getClientHandler(client).getSensorMaximumRange(sensor);
    }

    /**
     * Retrieve the sensor range of a sensor on all connected devices
     *
     * @param sensor the sensor of which the maximum <a href="https://developer.android.com/reference/android/hardware/Sensor.html#getMaximumRange()">range</a> is required
     * @return a mapping from NetworkDevice to the float range
     */
    HashMap<NetworkDevice, Float> getSensorMaximumRange(SensorType sensor) {
        HashMap<NetworkDevice, Float> rangeMapping = new HashMap<>();
        for (ClientConnection clientConnection : mClientConnections)
            rangeMapping.put(clientConnection.getClient(), clientConnection.getSensorMaximumRange(sensor));
        return rangeMapping;
    }

    /**
     * Return the number of currently added and not disconnected clients.
     *
     * @return number of currently added and not disconnected clients.
     */
    int getClientCount() {
        return mClientConnections.size();
    }
}

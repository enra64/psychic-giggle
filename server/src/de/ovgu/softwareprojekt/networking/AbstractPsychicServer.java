package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;
import de.ovgu.softwareprojekt.callback_interfaces.ClientListener;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.*;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * This class encapsulates the whole server system:
 * <p>
 * 1) A DiscoveryServer used to make clients able to find us
 * 2) A CommandConnection to be able to reliable communicate about important stuff, like enabling sensors
 * 3) A DataConnection to rapidly transmit sensor data
 */
@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public abstract class AbstractPsychicServer implements
        OnCommandListener,
        ClientListener,
        ExceptionListener,
        ButtonListener,
        ResetListener,
        ClientLossListener {
    /**
     * AbstractPsychicServer handling responding to discovery broadcasts
     */
    private DiscoveryServer mDiscoveryServer;

    /**
     * This is the latest unbound client connection, which makes it also the currently advertised connection. When bound,
     * this instance will be moved to {@link #mClientManager}, and a new instance will be set instead
     */
    private ClientConnection mCurrentUnboundClientConnection;

    /**
     * port where discovery packets are expected
     */
    private final int mDiscoveryPort;

    /**
     * This class is used for keeping the sensor-, button and speed requirements synchronous
     */
    private final ClientConnectionManager mClientManager;

    /**
     * name of this server
     */
    private String mServerName;

    /**
     * The data mapper is responsible for forwarding data correctly
     */
    private DataMapper mDataMapper = new DataMapper();

    /**
     * The maximum number of clients allowed on the server
     */
    private int mClientMaximum = Integer.MAX_VALUE;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName    if not null, this name will be used. otherwise, the devices hostname is used
     * @param discoveryPort the port that should be listened on for new devices
     */
    @SuppressWarnings("WeakerAccess")
    public AbstractPsychicServer(@Nullable String serverName, int discoveryPort) {
        // if the server name was not set, use the host name
        mServerName = serverName != null ? serverName : getHostName();
        mDiscoveryPort = discoveryPort;

        mClientManager = new ClientConnectionManager(
                mServerName,
                this,
                this,
                this,
                mDataMapper,
                this);

        mDataMapper.setConnectionHandler(mClientManager);

        // SIGTERM -> close all clients, stop discovery server
        //Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        //System.out.println("AbstractPsychicServer started, shutdown runtime hook online");
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called. The
     * discovery port is defaulted to port 8888
     *
     * @param serverName the name this server will be discoverable as
     */
    public AbstractPsychicServer(String serverName) {
        this(serverName, 8888);
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     * The device hostname will be used as server name.
     *
     * @param discoveryPort the discovery port to use
     */
    public AbstractPsychicServer(int discoveryPort) {
        this(null, discoveryPort);
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called. Port 8888 will
     * be used as the discovery port, and the device hostname will be used as server name.
     */
    public AbstractPsychicServer() {
        this(null);
    }

    /**
     * Starts the server. This will start making the server available for discovery, and also block the command and
     * data sockets.
     *
     * @throws IOException if one of the connections could not be initialised
     */
    @SuppressWarnings("WeakerAccess")
    public void start() throws IOException {
        advertiseServer();
    }

    /**
     * Stop listening on all ports and free them.
     */
    public void close() {
        System.out.println("Closing down");
        mClientManager.closeAll();
        mCurrentUnboundClientConnection.close();
        System.out.println("All ClientConnections closed");
        mDiscoveryServer.close();
        System.out.println("Discovery server closed");
    }

    /**
     * Tries to get a server name via the host name; not very reliable, but also not very important.
     *
     * @return the host name of this device, or, if it is not known, "unknown hostname"
     */
    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknown hostname";
        }
    }

    /**
     * Called whenever a command packet has arrived
     *
     * @param origin  host of the sender
     * @param command the command issued
     */
    @Override
    public void onCommand(InetAddress origin, AbstractCommand command) {
        ClientConnection fromClient = mClientManager.getClientHandler(origin);

        // decide what to do with the packet
        switch (command.getCommandType()) {
            case ConnectionRequest:
                // since this commands CommandType is ConnectionRequest, we know to what to cast it
                ConnectionRequest request = (ConnectionRequest) command;

                // update the request source address
                request.self.address = origin.getHostAddress();

                // accept the client if the client maximum has not been reached and the subclass allows it
                boolean acceptClient = !isClientMaximumReached() && acceptClient(request.self);

                // reply to the client, either accepting or denying his request
                try {
                    mCurrentUnboundClientConnection.handleConnectionRequest(request.self, acceptClient);
                } catch (UnknownHostException e) {
                    onException(this, e, "Could not parse address of incoming connection request. This is bad.");
                }

                if (acceptClient) {
                    try {
                        // add unbound connection to list of bound connections.
                        // it will be automatically configured
                        mClientManager.addHandler(mCurrentUnboundClientConnection);

                        // begin advertising the next client connection
                        advertiseServer();
                    } catch (IOException e) {
                        onException(this, e, "Could not start listening for new client. Bad.");
                    }

                    onClientAccepted(mClientManager.getClientHandler(origin).getClient());
                }
                break;
            case ButtonClick:
                if (fromClient != null)
                    onButtonClick((ButtonClick) command, fromClient.getClient());
                break;
            case ResetToCenter:
                if (fromClient != null)
                    onResetPosition(fromClient.getClient());
                break;
            // ignore unhandled commands
            default:
                break;
        }
    }

    /**
     * Called when the client manager detects a client loss.
     * We use it to re-check whether we need to advertise the server again
     */
    @Override
    public final void onClientLoss() {
        try {
            advertiseServer();
        } catch (IOException e) {
            onException(this, e, "Could not advertise server");
        }
    }

    /**
     * Initialising the discovery server makes it possible for the client ot find use. The command- and data connection
     * need to be initialised, because they provide important information
     *
     * @throws IOException if no new {@link ClientConnection} could be initialized
     */
    private void advertiseServer() throws IOException {
        // initialise the command and data connections
        mCurrentUnboundClientConnection = mClientManager.getUnboundHandler();

        // stop old instances
        if (mDiscoveryServer != null)
            mDiscoveryServer.close();

        // do not advertise the server if the client maximum has been reached
        if (isClientMaximumReached())
            return;

        // the DiscoveryServer makes it possible for the client to find us, but it needs to know the command and
        // data ports, which is why we had to initialise those connections first
        mDiscoveryServer = new DiscoveryServer(
                this,
                mDiscoveryPort,
                mCurrentUnboundClientConnection.getCommandPort(),
                mCurrentUnboundClientConnection.getDataPort(),
                mServerName);
        mDiscoveryServer.start();
    }

    /**
     * Closes the clients handler, and notifies the client of his disconnection
     *
     * @param client the client to be removed
     * @return false if client could not be found, true otherwise
     */
    public boolean disconnectClient(NetworkDevice client) {
        mDataMapper.onClientRemoved(client);
        return mClientManager.close(client);
    }

    /**
     * Register a new data sink to be included in the data stream
     *
     * @param dataSink        where new data from the sensor should go
     * @param requestedSensor which sensors events are relevant
     * @throws IOException if a client could not be notified of the sensor change
     */
    public void registerDataSink(NetworkDataSink dataSink, SensorType requestedSensor) throws IOException {
        mDataMapper.registerDataSink(requestedSensor, dataSink);
    }

    /**
     * Register a new data sink to be included in the data stream
     *
     * @param dataSink        where new data from the sensor should go
     * @param origin          the network device which is allowed to send to this sink
     * @param requestedSensor which sensor events are relevant
     * @throws IOException if a client could not be notified of the sensor change
     */
    protected void registerDataSink(NetworkDataSink dataSink, NetworkDevice origin, SensorType requestedSensor) throws IOException {
        mDataMapper.registerDataSink(requestedSensor, origin, dataSink);
    }

    /**
     * Unregister a data sink from all sensors
     *
     * @param dataSink which data sink to remove
     * @throws IOException if a client could not be notified of a no longer required sensor
     */
    public void unregisterDataSink(NetworkDataSink dataSink) throws IOException {
        // unregister dataSink from all sensors it is registered to
        mDataMapper.unregisterDataSink(dataSink);
    }

    /**
     * Unregister a data sink from all sensors
     *
     * @param dataSink which data sink to remove
     * @param sensor   the sensor for which the data sink was registered
     * @throws IOException if a client could not be notified of a no longer required sensor
     */
    public void unregisterDataSink(NetworkDataSink dataSink, SensorType sensor) throws IOException {
        // unregister dataSink from all sensors it is registered to
        mDataMapper.unregisterDataSink(dataSink, sensor);
    }

    /**
     * Add a button to be displayed on the clients
     *
     * @param name text to be displayed on the button
     * @param id   id of the button. ids below zero are reserved.
     * @throws IOException if a client could not be notified of newly required button
     */
    public void addButton(String name, int id) throws IOException {
        mClientManager.addButton(name, id);
    }

    /**
     * Remove all buttons added to the app using eithr {@link #addButton(String, int)} or {@link #setButtonLayout(String)}.
     *
     * @throws IOException if a client could not be notified of a no longer required button
     */
    public void clearButtons() throws IOException {
        mClientManager.clearButtons();
    }

    /**
     * Remove a button from the clients
     *
     * @param id id of the button
     * @throws IOException if a client could not be notified of a no longer required button
     */
    public void removeButton(int id) throws IOException {
        mClientManager.removeButton(id);
    }

    /**
     * sets mButtonXML to an incoming XML-String
     *
     * @param xml valid android XML layout using only linear layout and button
     *            if string is null ButtonMap will be used
     * @throws IOException if a client could not be notified of the new button layout
     */
    public void setButtonLayout(@Nullable String xml) throws IOException {
        mClientManager.setButtonLayout(xml);
    }

    /**
     * Change the speed of a sensor. The default speed is the GAME speed.
     *
     * @param sensor the sensor to change
     * @param speed  the speed to use for sensor
     * @throws IOException if a client could not be notified of the changed sensor speed
     */
    public void setSensorSpeed(SensorType sensor, SetSensorSpeed.SensorSpeed speed) throws IOException {
        mClientManager.setSensorSpeed(sensor, speed);
    }

    /**
     * Retrieve the sensor range of a sensor on a specified device
     *
     * @param sensor the range of this sensor will be returned
     * @param device the sensor on this device will be returned
     * @return the sensors maximum range as reported by <a href="https://developer.android.com/reference/android/hardware/Sensor.html#getMaximumRange()">android</a>
     */
    public float getSensorMaximumRange(SensorType sensor, NetworkDevice device) {
        return mClientManager.getSensorMaximumRange(device, sensor);
    }

    /**
     * Retrieve the sensor range of a sensor on all connected devices
     *
     * @param sensor the sensor of which the maximum <a href="https://developer.android.com/reference/android/hardware/Sensor.html#getMaximumRange()">range</a> is required
     * @return a mapping from NetworkDevice to the float range
     */
    public HashMap<NetworkDevice, Float> getSensorMaximumRange(SensorType sensor) {
        return mClientManager.getSensorMaximumRange(sensor);
    }

    /**
     * this method sends a notification to the device which is then displayed
     *
     * @param id            - NotificationID: should start at 0 and increases with each new notification while device is connected
     * @param title         - title of the notification
     * @param content       - content of the notification
     * @param device        -  device that shall display the notification
     * @throws IOException if a client could not be notified of the notification that should be displayed
     */
    public void displayNotification(int id, String title, String content, NetworkDevice device) throws IOException {
        mClientManager.getClientHandler(device).displayNotification(id, title, content);
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
        mClientManager.displayNotification(id, title, content);
    }

    /**
     * Set the maximum number of clients that may be accepted.
     * If this number is reached, the server will no longer advertise the server.
     *
     * @param maximum maximum number of clients
     */
    public void setClientMaximum(int maximum) {
        mClientMaximum = maximum;
    }

    /**
     * Return the current client maximum
     *
     * @return current maximum client count
     */
    public int getClientMaximum() {
        return mClientMaximum;
    }

    /**
     * Removes the restriction on client numbers
     */
    public void removeClientMaximum() {
        mClientMaximum = Integer.MAX_VALUE;
    }

    /**
     * Hides Reset Button if not in use
     * @param isHidden
     * @throws IOException is thrown if command could not be sent
     */
    public void hideResetButton(boolean isHidden)throws IOException {
        mClientManager.hideResetButton(isHidden);
    }

    /**
     * Check for the maximum number of clients
     *
     * @return true if no further clients may connect
     */
    private boolean isClientMaximumReached() {
        return mClientManager.getClientCount() >= mClientMaximum;
    }

    /**
     * sends a description for the sensortype
     *
     * @param type        affected sensor
     * @param description description for the affected sensor
     * @throws IOException if the sensor description could not be sent
     */
    public void sendSensorDescription(SensorType type, String description) throws IOException {
        mClientManager.sendSensorDescription(type, description);
    }
}

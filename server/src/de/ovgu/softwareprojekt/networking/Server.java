package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.*;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import sun.net.NetworkClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.HashSet;

/**
 * This class encapsulates the whole server system:
 * <p>
 * 1) A DiscoveryServer used to make clients able to find us
 * 2) A CommandConnection to be able to reliable communicate about important stuff, like enabling sensors
 * 3) A DataConnection to rapidly transmit sensor data
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Server implements OnCommandListener, ClientListener, ExceptionListener, ButtonListener, ResetListener {
    /**
     * Server handling responding to discovery broadcasts
     */
    private DiscoveryServer mDiscoveryServer;

    /**
     * This is the latest unbound client connection, which makes it also the currently advertised connection. When bound,
     * this instance will be moved to {@link #mClientHandlerFactory}, and a new instance will be set instead
     */
    private ClientConnection mCurrentUnboundClientConnection;

    /**
     * port where discovery packets are expected
     */
    private final int mDiscoveryPort;

    /**
     * This class is used for keeping the sensor-, button and speed requirements synchronous
     */
    private final ClientConnectionHandler mClientHandlerFactory;

    /**
     * name of this server
     */
    private String mServerName;

    /**
     * The data mapper is responsible for forwarding data correctly
     */
    private DataMapper mDataMapper = new DataMapper();

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     */
    @SuppressWarnings("WeakerAccess")
    public Server(@Nullable String serverName, int discoveryPort) {
        // if the server name was not set, use the host name
        mServerName = serverName != null ? serverName : getHostName();
        mDiscoveryPort = discoveryPort;

        mClientHandlerFactory = new ClientConnectionHandler(
                mServerName,
                this,
                this,
                this,
                mDataMapper);

        mDataMapper.setConnectionHandler(mClientHandlerFactory);
        // SIGTERM -> close all clients, stop discovery server
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called. The
     * discovery port is defaulted to port 8888
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     */
    public Server(@Nullable String serverName) {
        this(serverName, 8888);
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
        mClientHandlerFactory.closeAll();
        mDiscoveryServer.close();
    }

    /**
     * Tries to get a server name via the host name; not very reliable, but also not very important.
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
        ClientConnection fromClient = mClientHandlerFactory.getClientHandler(origin);

        // decide what to do with the packet
        switch (command.getCommandType()) {
            case ConnectionRequest:
                // since this commands CommandType is ConnectionRequest, we know to what to cast it
                ConnectionRequest request = (ConnectionRequest) command;

                // update the request source address
                request.self.address = origin.getHostAddress();

                boolean acceptClient = acceptClient(request.self);

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
                        mClientHandlerFactory.addHandler(mCurrentUnboundClientConnection);

                        // begin advertising the next client connection
                        advertiseServer();
                    } catch (IOException e) {
                        onException(this, e, "Could not start listening for new client. Bad.");
                    }

                    onClientAccepted(mClientHandlerFactory.getClientHandler(origin).getClient());
                }
                break;
            case ButtonClick:
                if(fromClient != null)
                    onButtonClick((ButtonClick) command, fromClient.getClient());
                break;
            case ResetToCenter:
                if(fromClient != null)
                    onResetPosition(fromClient.getClient());
                break;
            // ignore unhandled commands
            default:
                break;
        }
    }

    /**
     * Initialising the discovery server makes it possible for the client ot find use. The command- and data connection
     * need to be initialised, because they provide important information
     */
    private void advertiseServer() throws IOException {
        // initialise the command and data connections
        mCurrentUnboundClientConnection = mClientHandlerFactory.getUnboundHandler();

        // stop old instances
        if (mDiscoveryServer != null)
            mDiscoveryServer.close();

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
        return mClientHandlerFactory.close(client);
    }

    /**
     * Register a new data sink to be included in the data stream
     *
     * @param dataSink        where new data from the sensor should go
     * @param requestedSensor which sensors events are relevant
     * @throws IOException if a client could not be notified of the sensor change
     */
    protected void registerDataSink(NetworkDataSink dataSink, SensorType requestedSensor) throws IOException {
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
     */
    public void unregisterDataSink(NetworkDataSink dataSink) throws IOException {
        // unregister dataSink from all sensors it is registered to
        mDataMapper.unregisterDataSink(dataSink);
    }

    /**
     * Unregister a data sink from all sensors
     *
     * @param dataSink which data sink to remove
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
     */
    protected void addButton(String name, int id) throws IOException {
        mClientHandlerFactory.addButton(name, id);
    }

    /**
     * Remove a button from the clients
     *
     * @param id id of the button
     */
    public void removeButton(int id) throws IOException {
        mClientHandlerFactory.removeButton(id);
    }

    /**
     * sets mButtonXML to an incoming XML-String
     *
     * @param xml valid android XML layout using only linear layout and button
     *            if string is null ButtonMap will be used
     */
    public void setButtonLayout(@Nullable String xml) throws IOException {
        mClientHandlerFactory.setButtonLayout(xml);
    }

    /**
     * Change the speed of a sensor. The default speed is the GAME speed.
     *
     * @param sensor the sensor to change
     * @param speed  the speed to use for sensor
     */
    public void setSensorSpeed(SensorType sensor, SetSensorSpeed.SensorSpeed speed) throws IOException {
        mClientHandlerFactory.setSensorSpeed(sensor, speed);
    }

    /**
     * Change the output range of a sensor
     *
     * @param sensor      affected sensor
     * @param outputRange the resulting maximum and negative minimum of the sensor output range. Default is -100 to 100.
     */
    protected void setSensorOutputRange(SensorType sensor, @SuppressWarnings("SameParameterValue") float outputRange) {
        mClientHandlerFactory.setSensorOutputRange(sensor, outputRange);
    }

    /**
     * this method sends a notification to the device which is then displayed
     *
     * @param id - NotificationID: should start at 0 and increases with each new notification while device is connected
     * @param title - title of the notification
     * @param content - content of the notification
     * @param isOnGoing - if true, notification is not removable by user
     * @param deviceAddress - address of the device that shall display the notification
     * @throws IOException
     */
    protected void displayNotification(int id, String title, String content, boolean isOnGoing, InetAddress deviceAddress) throws IOException {
        mClientHandlerFactory.getClientHandler(deviceAddress).displayNotification(id, title ,content, isOnGoing);

    }
}

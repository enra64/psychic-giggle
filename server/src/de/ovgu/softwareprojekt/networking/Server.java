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
public abstract class Server implements OnCommandListener, NetworkDataSink, ClientListener, ExceptionListener, ButtonListener, ResetListener {
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
     * Stores all known data sinks
     * <p>
     * This variable is an EnumMap, so iterations over the keys should be quite fast. The sinks are stored in a
     * HashSet for each sensor, so no sink can occur twice for one sensor type.
     */
    private EnumMap<SensorType, HashSet<NetworkDataSink>> mDataSinks = new EnumMap<>(SensorType.class);

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
                this);
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
    @Override
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
                }
                break;
            case ButtonClick:
                onButtonClick((ButtonClick) command, mClientHandlerFactory.getClientHandler(origin).getClient());
                break;
            case ResetToCenter:
                onResetPosition(mClientHandlerFactory.getClientHandler(origin).getClient());
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
     * onData is called whenever new data is to be processed. This function must not be overridden to allow the data sink
     * registration system to work
     *
     * @param origin          the network device which sent the data
     * @param data            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public final void onData(NetworkDevice origin, SensorData data, float userSensitivity){
        // relay the data to all sinks registered for this sensor type
        //mDataSinks.get(data.sensorType).forEach(sink -> sink.onData(origin, data, userSensitivity));
        HashSet<NetworkDataSink> registeredSinks = mDataSinks.get(data.sensorType);
        for(NetworkDataSink sink : registeredSinks)
            sink.onData(origin, data, userSensitivity);

    }

    /**
     * Closes the clients handler, and notifies the client of his disconnection
     *
     * @param client the client to be removed
     * @return false if client could not be found, true otherwise
     */
    public boolean disconnectClient(NetworkDevice client) {
        return mClientHandlerFactory.close(client);
    }

    /**
     * Register a new data sink to be included in the data stream
     *
     * @param dataSink        where new data from the sensor should go
     * @param requestedSensor which sensors events are relevant
     */
    protected void registerDataSink(NetworkDataSink dataSink, SensorType requestedSensor) throws IOException {
        // dataSink *must not be* this, as that would lead to infinite recursion
        if(this == dataSink)
            throw new InvalidParameterException("A Server subclass cannot register itself as a data sink.");

        // add new data sink list if the requested sensor type has no sinks yet
        if (!mDataSinks.containsKey(requestedSensor))
            mDataSinks.put(requestedSensor, new HashSet<>());

        // add the new sink to the list of sink for the sensor
        mDataSinks.get(requestedSensor).add(dataSink);

        // force resetting the sensors on the client
        mClientHandlerFactory.updateSensors(mDataSinks.keySet());
    }

    /**
     * Unregister a data sink from a sensors data stream
     *
     * @param dataSink        the data sink to be unregistered
     * @param requestedSensor the sensor the sink should be unregistered from
     */
    private void unregisterDataSink(NetworkDataSink dataSink, SensorType requestedSensor) throws IOException {
        mDataSinks.get(requestedSensor).remove(dataSink);

        // remove the sensor if it no longer has any data sinks
        if(mDataSinks.get(requestedSensor).size() == 0)
            mDataSinks.remove(requestedSensor);

        // force resetting the sensors on the client
        mClientHandlerFactory.updateSensors(mDataSinks.keySet());
    }

    /**
     * Unregister a data sink from all sensors
     *
     * @param dataSink which data sink to remove
     */
    public void unregisterDataSink(NetworkDataSink dataSink) throws IOException {
        // unregister dataSink from all sensors it is registered to
        for (SensorType type : mDataSinks.keySet())
            unregisterDataSink(dataSink, type);

        // force resetting the sensors on the client
        mClientHandlerFactory.updateSensors(mDataSinks.keySet());
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
}

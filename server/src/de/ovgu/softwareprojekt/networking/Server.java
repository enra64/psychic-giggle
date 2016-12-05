package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import com.sun.security.ntlm.Client;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.control.commands.EndConnection;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This class encapsulates the whole server system:
 * <p>
 * 1) A DiscoveryServer used to make clients able to find us
 * 2) A CommandConnection to be able to reliable communicate about important stuff, like enabling sensors
 * 3) A DataConnection to rapidly transmit sensor data
 */
@SuppressWarnings("unused")
public abstract class Server implements OnCommandListener, DataSink, ClientListener, ExceptionListener, ButtonListener, ResetListener {
    /**
     * Server handling responding to discovery broadcasts
     */
    private DiscoveryServer mDiscoveryServer;

    /**
     * list of buttons that should be displayed on all clients
     */
    private Map<Integer, String> mButtonList = new HashMap<>();

    /**
     * Name of the server as displayed to discovery clients
     */
    private String mServerName;

    /**
     * This is the list of all currently bound clients.
     */
    private List<ClientConnectionHandler> mClientConnections = new ArrayList<>();

    /**
     * This is the latest unbound client connection, which makes it also the currently advertised connection. When bound,
     * this instance will be moved to {@link #mClientConnections}, and a new instance will be set instead
     */
    private ClientConnectionHandler mCurrentUnboundClientConnection;

    /**
     * port where discovery packets are expected
     */
    private final int mDiscoveryPort;

    /**
     * Button id of the reset button
     */
    private static final int RESET_POSITION_BUTTON_ID = -1;

    /**
     * Stores all known data sinks
     * <p>
     * This variable is an EnumMap, so iterations over the keys should be quite fast. The sinks are stored in a
     * HashSet for each sensor, so no sink can occur twice for one sensor type.
     */
    private EnumMap<SensorType, HashSet<DataSink>> mDataSinks = new EnumMap<>(SensorType.class);

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
        // initialise the command and data connections
        mCurrentUnboundClientConnection = new ClientConnectionHandler(mServerName,
                this,
                this,
                this,
                this);
        advertiseServer(mCurrentUnboundClientConnection);
    }

    /**
     * Stop listening on all ports and free them.
     */
    @Override
    public void close() {
        for (ClientConnectionHandler client : mClientConnections)
            client.close();
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
     * Register a new data sink to be included in the data stream
     *
     * @param dataSink        where new data from the sensor should go
     * @param requestedSensor which sensors events are relevant
     */
    @SuppressWarnings("WeakerAccess")
    public void registerDataSink(DataSink dataSink, SensorType requestedSensor) throws IOException {
        // add new data sink list if the requested sensor type has no sinks yet
        if (!mDataSinks.containsKey(requestedSensor))
            mDataSinks.put(requestedSensor, new HashSet<DataSink>());

        // add the new sink to the list of sink for the sensor
        mDataSinks.get(requestedSensor).add(dataSink);

        // force resetting the sensors on the client
        updateSensors();
    }

    /**
     * Unregister a data sink from a sensors data stream
     *
     * @param dataSink        the data sink to be unregistered
     * @param requestedSensor the sensor the sink should be unregistered from
     */
    @SuppressWarnings("WeakerAccess")
    public void unregisterDataSink(DataSink dataSink, SensorType requestedSensor) throws IOException {
        mDataSinks.get(requestedSensor).remove(dataSink);

        // force resetting the sensors on the client
        updateSensors();
    }

    /**
     * Unregister a data sink from all sensors
     *
     * @param dataSink which data sink to remove
     */
    public void unregisterDataSink(DataSink dataSink) throws IOException {
        // unregister dataSink from all sensors it is registered to
        for (SensorType type : mDataSinks.keySet())
            unregisterDataSink(dataSink, type);

        // force resetting the sensors on the client
        updateSensors();
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
                    // add unbound connection to list of bound connections
                    mClientConnections.add(mCurrentUnboundClientConnection);

                    try {
                        // send the sensor- and button requirements to the new client
                        mCurrentUnboundClientConnection.updateSensors(mDataSinks.keySet());
                        mCurrentUnboundClientConnection.updateButtons(mButtonList);

                        // create new possible client connection
                        mCurrentUnboundClientConnection = new ClientConnectionHandler(
                                mServerName,
                                this,
                                this,
                                this,
                                this);
                    } catch (IOException e) {
                        onException(this, e, "Could not start listening for new client. Bad.");
                    }

                    // begin advertising the next client connection
                    advertiseServer(mCurrentUnboundClientConnection);
                }
                break;
            case EndConnection:
                // notify listener of disconnect
                EndConnection endConnection = (EndConnection) command;
                endConnection.self.address = origin.getHostAddress();
                onClientDisconnected(endConnection.self);

                // remove that client
                try {
                    getClientHandler(endConnection.self).close();
                } catch (NullPointerException e) {
                    // yeah this shouldn't happen
                    onException(this, e, "Could not find client that ended connection!");
                }
                break;

            case ButtonClick:
                onButtonClick((ButtonClick) command, getClientHandler(origin).getClient());
                break;
            case ResetToCenter:
                onResetPosition(getClientHandler(origin).getClient());
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
    private void advertiseServer(ClientConnectionHandler clientConnectionHandler) {
        // stop old instances
        if (mDiscoveryServer != null)
            mDiscoveryServer.close();

        // the DiscoveryServer makes it possible for the client to find us, but it needs to know the command and
        // data ports, which is why we had to initialise those connections first
        mDiscoveryServer = new DiscoveryServer(
                this,
                mDiscoveryPort,
                clientConnectionHandler.getCommandPort(),
                clientConnectionHandler.getDataPort(),
                mServerName);
        mDiscoveryServer.start();
    }


    /**
     * This onData must not be overridden to allow the data sink registration system to work
     *
     * @param sensorData new sensor data
     */
    @Override
    public final void onData(SensorData sensorData) {
        // iterate over all data sinks marked as interested in this sensor type
        for (DataSink sink : mDataSinks.get(sensorData.sensorType))
            sink.onData(sensorData);
    }

    /**
     * Closes the clients handler, and notifies the client of his disconnection
     *
     * @param client the client to be removed
     * @return false if client could not be found, true otherwise
     */
    public boolean disconnectClient(NetworkDevice client) {
        ClientConnectionHandler connectionHandler = getClientHandler(client);

        // return false if no matching client could be found
        if (connectionHandler == null)
            return false;

        // close the client connection
        connectionHandler.closeAndSignalClient();
        return true;
    }

    /**
     * This function find the connection handler that is managing a certain client
     *
     * @param client the client that should be found
     * @return null if no matching handler was found, or the handler.
     */
    private ClientConnectionHandler getClientHandler(NetworkDevice client) {
        for (ClientConnectionHandler clientConnection : mClientConnections)
            // true if the clients match
            if (clientConnection.getClient().equals(client))
                return clientConnection;
        return null;
    }

    /**
     * This function find the connection handler that is managing a certain client, which is identified only by
     * his address
     *
     * @param address the address of the client that should be found
     * @return null if no matching handler was found, or the handler.
     */
    private ClientConnectionHandler getClientHandler(InetAddress address) {
        for (ClientConnectionHandler clientConnection : mClientConnections)
            // true if the clients match
            if (clientConnection.getClient().address.equals(address.getHostAddress()))
                return clientConnection;
        return null;
    }

    /**
     * Add a button to be displayed on the clients
     *
     * @param name text to be displayed on the button
     * @param id   id of the button. ids below zero are reserved.
     */
    protected final void addButton(String name, int id) throws IOException {
        // reserve id's below zero
        assert (id >= 0);

        // add the new button to local storage
        mButtonList.put(id, name);

        // update the button list on our clients
        updateButtons();
    }

    /**
     * Remove a button from the clients
     *
     * @param id id of the button
     */
    public void removeButton(int id) throws IOException {
        // remove the button from local storage
        mButtonList.remove(id);

        // update the button list on our clients
        updateButtons();
    }

    /**
     * Force each client to update his buttons
     *
     * @throws IOException if the command could not be sent
     */
    private void updateButtons() throws IOException {
        for (ClientConnectionHandler client : mClientConnections)
            client.updateButtons(mButtonList);
    }

    /**
     * Force each client to update his required sensors
     *
     * @throws IOException if the command could not be sent
     */
    private void updateSensors() throws IOException {
        for (ClientConnectionHandler client : mClientConnections)
            client.updateSensors(mDataSinks.keySet());
    }
}

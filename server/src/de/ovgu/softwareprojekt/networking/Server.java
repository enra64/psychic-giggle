package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.*;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
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
public abstract class Server implements OnCommandListener, DataSink, ClientListener, ExceptionListener, ButtonListener {
    /**
     * Useful when you want to transmit SensorData
     */
    private UdpDataConnection mDataConnection;

    /**
     * Two-way communication for basically everything non-data, like enabling sensors or requesting connections
     */
    private CommandConnection mCommandConnection;

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
    public Server(@Nullable String serverName) {
        // if the server name was not set, use the host name
        mServerName = serverName != null ? serverName : getHostName();
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
        initialiseCommandConnection();
        initialiseDataConnection();
        initialiseDiscoveryServer();
    }

    /**
     * Stop listening on all ports and free them.
     */
    @Override
    public void close() {
        mCommandConnection.close();
        mDataConnection.close();
        mDiscoveryServer.close();
    }

    /**
     * Tries to get a server name via the host name; not very reliable, but also not very important.
     */
    private String getHostName() {
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
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
    public void onCommand(InetAddress origin, Command command) {
        // decide what to do with the packet
        switch (command.getCommandType()) {
            case ConnectionRequest:
                // since this commands CommandType is ConnectionRequest, we know to what to cast it
                ConnectionRequest request = (ConnectionRequest) command;

                // update the request source address
                request.self.address = origin.getHostAddress();

                // initialise the connection to be able to send the request answer
                mCommandConnection.setRemote(origin, request.self.commandPort);

                // accept or reject the client
                if (acceptClient(request.self))
                    onClientAccepted(request.self);
                else
                    onClientRejected(request.self);
                break;
            case EndConnection:
                // notify listener of disconnect
                EndConnection endConnection = (EndConnection) command;
                endConnection.self.address = origin.getHostAddress();
                onClientDisconnected(endConnection.self);

                // restart connections
                try {
                    close();
                    start();
                } catch (IOException e) {
                    onException(this, e, "Could not restart connection after client connected");
                }
                break;

            case ButtonClick:
                onButtonClick((ButtonClick) command);
                // ignore unhandled commands
            default:
                break;
        }
    }

    private void onClientAccepted(NetworkDevice self) {
        try {
            // accept the client
            mCommandConnection.sendCommand(new ConnectionRequestResponse(true));

            // notify the client of our button and sensor requirements
            updateButtons();
            updateSensors();

            // close the discovery server
            mDiscoveryServer.close();
        } catch (IOException e) {
            onException(this, e, "could not accept client");
        }
    }

    private void onClientRejected(NetworkDevice self) {
        try {
            // deny the client
            mCommandConnection.sendCommand(new ConnectionRequestResponse(false));
        } catch (IOException e) {
            onException(this, e, "could not reject client");
        }
    }

    /**
     * Notify the client of all required buttons
     *
     * @throws IOException if the command could not be sent
     */
    private void updateButtons() throws IOException {
        // if the button list was changed, we need to update the clients buttons
        if (mCommandConnection != null && mCommandConnection.isRunningAndConfigured())
            mCommandConnection.sendCommand(new UpdateButtons(mButtonList));
    }

    /**
     * Notify the client of all required sensors
     *
     * @throws IOException if the command could not be sent
     */
    private void updateSensors() throws IOException {
        // the key set is not serializable, so we must create an ArrayList from it
        if (mCommandConnection != null && mCommandConnection.isRunningAndConfigured())
            mCommandConnection.sendCommand(new SetSensorCommand(new ArrayList<>(mDataSinks.keySet())));
    }

    /**
     * Initialising the discovery server makes it possible for the client ot find use. The command- and data connection
     * need to be initialised, because they provide important information
     */
    private void initialiseDiscoveryServer() {
        // the DiscoveryServer makes it possible for the client to find us, but it needs to know the command and
        // data ports, which is why we had to initialise those connections first
        mDiscoveryServer = new DiscoveryServer(
                this,
                8888,
                mCommandConnection.getLocalPort(),
                mDataConnection.getLocalPort(),
                mServerName);
        mDiscoveryServer.start();
    }

    /**
     * Instantiate the command connection (with this as listener) and start listening for command packets
     */
    private void initialiseCommandConnection() throws IOException {
        // begin a new command connection, set this as callback
        mCommandConnection = new CommandConnection(this);

        // begin listening for commands
        mCommandConnection.start();
    }

    /**
     * Instantiate the data connection (with this as listener) and start listening for data packets
     */
    private void initialiseDataConnection() throws SocketException {
        // begin a new data connection
        mDataConnection = new UdpDataConnection();

        // register a callback for data objects
        mDataConnection.setDataSink(this);

        // begin listening for SensorData objects
        mDataConnection.start();
    }

    @Override
    public void onData(SensorData sensorData) {
        // iterate over all data sinks marked as interested in this sensor type
        for (DataSink sink : mDataSinks.get(sensorData.sensorType))
            sink.onData(sensorData);
    }


    /**
     * Add a button to be displayed on the clients
     *
     * @param name text to be displayed on the button
     * @param id   id of the button
     */
    public void addButton(String name, int id) throws IOException {
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
     * Called when an exception cannot be gracefully handled.
     *
     * @param origin    the instance (or, if it is a hidden instance, the known parent) that produced the exception
     * @param exception the exception that was thrown
     * @param info additional information to help identify the problem
     */
    @Override
    public abstract void onException(Object origin, Exception exception, String info);

    /**
     * Called whenever a button is clicked
     *
     * @param click event object specifying details like button id
     */
    @Override
    public abstract void onButtonClick(ButtonClick click);

    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public abstract boolean acceptClient(NetworkDevice newClient);

    /**
     * Called when a client sent a disconnect signal
     * @param disconnectedClient the lost client
     */
    @Override
    public abstract void onClientDisconnected(NetworkDevice disconnectedClient);
}

package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.NotNull;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.ConnectionWatch;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.*;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This class manages the command- and data connection for a single client. It intercepts the following commands:
 * {@link ConnectionAliveCheck} and {@link ChangeSensorSensitivity}, never notifying the command listener of receiving
 * them. When a {@link EndConnection} command is received, all ports are closed, and the command is forwarded; no other
 * command can be sent to the client after that.
 */
public class ClientConnection implements OnCommandListener, NetworkDataSink, ConnectionWatch.TimeoutListener {
    /**
     * This listener is to be called when we get commands from an unknown client
     */
    private final UnexpectedClientListener mUnexpectedClientListener;

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
     * Useful when you want to receive SensorData
     */
    private UdpDataConnection mDataConnection;

    /**
     * Two-way communication for basically everything non-data, like enabling sensors or requesting connections
     */
    private CommandConnection mCommandConnection;

    /**
     * Where to put data
     */
    private NetworkDataSink mDataSink;

    /**
     * The client this instance handles
     */
    private NetworkDevice mClient;

    /**
     * This class handles scaling the data of each sensor
     */
    private DataScalingHandler mDataScalingHandler;

    /**
     * False until {@link #onClientAccepted(NetworkDevice)} has run successfully, and after {@link #close()} was called
     */
    private volatile boolean mIsConnected = false;

    /**
     * Used to keep track of the connection health
     */
    private final ConnectionWatch mConnectionWatch;

    /**
     * The InetAddress of our client, saved to avoid constant conversions
     */
    private InetAddress mClientAddress;

    /**
     * Create a new client connection handler, which will immediately begin listening on random ports. They may be retrieved
     * using {@link #getCommandPort()} and {@link #getDataPort()}.
     *
     * @param serverName        how the client connection should identify the server
     * @param exceptionListener Who to notify about unhandleable exceptions
     * @param commandListener   Who to notify about commands
     * @param dataSink          Where to put data
     * @throws IOException when the listening process could not be started
     */
    ClientConnection(
            String serverName,
            ExceptionListener exceptionListener,
            OnCommandListener commandListener,
            ClientListener clientListener,
            NetworkDataSink dataSink,
            UnexpectedClientListener unexpectedClientListener) throws IOException {
        mExceptionListener = exceptionListener;
        mCommandListener = commandListener;
        mClientListener = clientListener;
        mUnexpectedClientListener = unexpectedClientListener;

        // this is our sink for everything to be able to do customized scaling for any sensor type
        mDataScalingHandler = new DataScalingHandler(dataSink);
        mDataSink = mDataScalingHandler;

        initialiseCommandConnection();
        initialiseDataConnection();

        //NetworkDevice identifying this client handler (eg name, data, command port)
        NetworkDevice self = new NetworkDevice(serverName, mCommandConnection.getLocalPort(), mDataConnection.getLocalPort());
        System.out.println("new client connection: " + self.hashCode());
        mConnectionWatch = new ConnectionWatch(self, this, mCommandConnection, exceptionListener);
    }

    /**
     * Send a command to this client
     *
     * @param command the command to be sent
     * @throws IOException if an error <b>other than</b> {@link ConnectException} occurred. If a {@link ConnectException}
     *                     occurred, the client is likely listening no longer.
     */
    public void sendCommand(AbstractCommand command) throws IOException {
        if (mIsConnected) {
            try {
                mCommandConnection.sendCommand(command);
            } catch (ConnectException e) {
                // if the client does not listen anymore, it most probably is down
                if (e.getMessage().contains("Connection refused")) {
                    // notify client listener only if we have not been closed
                    if (mIsConnected)
                        mClientListener.onClientDisconnected(mClient);

                    // close self
                    close();
                }
            }
        }
    }

    /**
     * Retrieve the client this connection handler is bound to
     */
    @NotNull
    NetworkDevice getClient() {
        return mClient;
    }

    /**
     * Handle a connection request to this client connection. If acceptClient is true, this instance may not be used to
     * handle another client.
     *
     * @param client       the client that wants to connect
     * @param acceptClient true if the client should be accepted, and this instance be bound to it
     * @throws UnknownHostException if the clients address could not be parsed to an {@link java.net.InetAddress}. Unlikely...
     */
    void handleConnectionRequest(NetworkDevice client, boolean acceptClient) throws UnknownHostException {
        // if mClient is not null, we already handled a client, but we cannot handle another one.
        assert mClient == null;

        // initialise the connection to be able to send the request answer
        mCommandConnection.setRemote(client.getInetAddress(), client.commandPort);

        // accept or reject the client
        if (acceptClient)
            onClientAccepted(client);
        else
            onClientRejected();
    }

    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        mDataSink.onData(origin, sensorData, userSensitivity);
    }

    /**
     * Close all network interfaces this connection handler has used
     */
    public void close() {
        mIsConnected = false;
        mCommandConnection.close();
        mDataConnection.close();
        mConnectionWatch.close();
    }

    /**
     * Close the ports, but beforehand send an {@link EndConnection} command to the client.
     */
    void closeAndSignalClient() {
        try {
            System.out.println("closing client " + mClient);
            sendCommand(new EndConnection(null));
        } catch (IOException ignored) {
            // ignore this exception, since the connection state to this client doesn't matter anymore anyways
        }
        close();
    }

    /**
     * Notify a client of its acceptance and set this instance to handle that client
     *
     * @param client the client we will be handling
     */
    private void onClientAccepted(final NetworkDevice client) {
        try {
            // flag that we have been connected
            mIsConnected = true;

            // notify the data connection of its client
            mDataConnection.setClient(client);

            // store client identification
            mClient = client;

            // store client address
            mClientAddress = client.getInetAddress();

            // accept the client
            sendCommand(new ConnectionRequestResponse(true));

            // give the connection watch the device it watches
            mConnectionWatch.setRemote(client);

            // start the connection watch
            mConnectionWatch.start();
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "could not accept client");
        }
    }

    /**
     * Notify a client of its rejection. This instance may continue to be used for another client.
     */
    private void onClientRejected() {
        try {
            // deny the client
            mCommandConnection.sendCommand(new ConnectionRequestResponse(false));
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "could not reject client");
        }
    }

    /**
     * Instantiate the command connection (with this as listener) and start listening for command packets
     */
    private void initialiseCommandConnection() throws IOException {
        // begin a new command connection, set this as callback
        mCommandConnection = new CommandConnection(this);

        // begin listening for commands
        mCommandConnection.start();

        System.out.println("new command connection on port " + mCommandConnection.getLocalPort());
    }

    /**
     * Instantiate the data connection (with this as listener) and start listening for data packets
     */
    private void initialiseDataConnection() throws SocketException {
        // begin a new data connection
        mDataConnection = new UdpDataConnection(mExceptionListener);

        // register a callback for data objects
        mDataConnection.setDataSink(mDataSink);

        // begin listening for SensorData objects
        mDataConnection.start();
    }

    /**
     * Notify the client of all required buttons
     * will overwrite any settings made by {@link #updateButtons(String)}
     *
     * @throws IOException if the command could not be sent
     */
    void updateButtons(Map<Integer, String> buttonList) throws IOException {
        // if the button list was changed, we need to update the clients buttons
        sendCommand(new UpdateButtonsMap(buttonList));
    }

    /**
     * Send client custom xml button layout
     * will overwrite any settings made by {@link #updateButtons(Map)}
     *
     * @param xml valid android XML layout using only linear layout and button
     * @throws IOException if the command could not be sent
     */
    void updateButtons(String xml) throws IOException {
        sendCommand((new UpdateButtonsXML(xml)));
    }

    /**
     * Notify the client of all required sensors
     *
     * @throws IOException if the command could not be sent
     */
    void updateSensors(Set<SensorType> requiredSensors) throws IOException {
        sendCommand(new SetSensorCommand(new ArrayList<>(requiredSensors)));
    }

    void updateSpeeds(Set<Map.Entry<SensorType, SetSensorSpeed.SensorSpeed>> speedSet) throws IOException {
        for (Map.Entry<SensorType, SetSensorSpeed.SensorSpeed> sensorSpeedMapping : speedSet)
            sendCommand(new SetSensorSpeed(sensorSpeedMapping.getKey(), sensorSpeedMapping.getValue()));
    }

    /**
     * @return the port the {@link CommandConnection} is listening on
     */
    int getCommandPort() {
        return mCommandConnection.getLocalPort();
    }

    /**
     * @return the port the {@link UdpDataConnection} is listening on
     */
    int getDataPort() {
        return mDataConnection.getLocalPort();
    }

    @Override
    public void onCommand(InetAddress inetAddress, AbstractCommand command) {
        // ignore commands not sent by this client
        if (!inetAddress.equals(mClientAddress))
            mUnexpectedClientListener.onUnexpectedClient(inetAddress, command);

        // we only want to catch a certain subset of commands here
        switch (command.getCommandType()) {
            case ChangeSensorSensitivity:
                // update the sensitivity for the given sensor
                ChangeSensorSensitivity sensorChangeCommand = (ChangeSensorSensitivity) command;
                mDataScalingHandler.setSensorSensitivity(sensorChangeCommand.sensorType, sensorChangeCommand.sensitivity);
                break;
            case SensorRangeNotification:
                SensorRangeNotification notification = (SensorRangeNotification) command;
                mDataScalingHandler.setSourceRange(notification.type, notification.range);
                break;
            case ConnectionAliveCheck:
                mConnectionWatch.onCheckEvent();
                break;
            case EndConnection:
                if (mIsConnected)
                    mClientListener.onClientDisconnected(mClient);
                close();
                break;
            default:
                mCommandListener.onCommand(inetAddress, command);
        }
    }

    /**
     * Change the output range for a certain sensor
     *
     * @param sensor      the sensor whose data will be affected
     * @param outputRange the maximum and negative minimum of the resulting output range
     */
    void setOutputRange(SensorType sensor, float outputRange) {
        mDataScalingHandler.setOutputRange(sensor, outputRange);
    }

    /**
     * This function may be called by the {@link ConnectionWatch} if it detects a timeout
     *
     * @param age the response delay, e.g. how long did the client need to respond
     */
    @Override
    public void onTimeout(long age) {
        // notify our client listener of the timeout
        if (mIsConnected)
            mClientListener.onClientTimeout(mClient);

        // notify the client that we do no longer communicate with it in case it is still listening
        closeAndSignalClient();
    }

    /**
     * Call this to remind the client of its ports, if it got the wrong ports at the start
     *
     * @throws IOException if the command could not be sent
     */
    void enforcePorts() throws IOException {
        sendCommand(new RemapPorts(getDataPort(), getCommandPort()));
    }
}

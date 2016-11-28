package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.NotNull;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.*;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * This class manages the command- and data connection for a single client
 */
public class ClientConnectionHandler {
    /**
     * Useful when you want to transmit SensorData
     */
    private UdpDataConnection mDataConnection;

    /**
     * Two-way communication for basically everything non-data, like enabling sensors or requesting connections
     */
    private CommandConnection mCommandConnection;

    /**
     * The client this instance handles
     */
    private NetworkDevice mClient;

    /**
     * Who to notify about unhandleable exceptions
     */
    private ExceptionListener mExceptionListener;

    /**
     * Who to notify about commands
     */
    private OnCommandListener mCommandListener;

    /**
     * Where to put data
     */
    private DataSink mDataSink;

    /**
     * Create a new client connection handler, which will immediately begin listening on random ports. They may be retrieved
     * using {@link #getCommandPort()} and {@link #getDataPort()}.
     *
     * @param exceptionListener Who to notify about unhandleable exceptions
     * @param commandListener   Who to notify about commands
     * @param dataSink          Where to put data
     * @throws IOException when the listening process could not be started
     */
    public ClientConnectionHandler(ExceptionListener exceptionListener, OnCommandListener commandListener, DataSink dataSink) throws IOException {
        mExceptionListener = exceptionListener;
        mCommandListener = commandListener;
        mDataSink = dataSink;

        initialiseCommandConnection();
        initialiseDataConnection();
    }

    /**
     * Send a command to this client
     *
     * @param command the command to be sent
     * @throws IOException if the command could not be sent
     */
    public void sendCommand(Command command) throws IOException {
        mCommandConnection.sendCommand(command);
    }

    /**
     * Retrieve the client this connection handler is bound to
     */
    public
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
    public void handleConnectionRequest(NetworkDevice client, boolean acceptClient) throws UnknownHostException {
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

    /**
     * Close all network interfaces this connection handler has used
     */
    public void close() {
        mCommandConnection.close();
        mDataConnection.close();
    }

    /**
     * Close the ports, but beforehand send an {@link EndConnection} command to the client.
     */
    public void closeAndSignalClient() {
        try {
            sendCommand(new EndConnection(null));
        } catch (IOException ignored) {
        }
        close();
    }

    /**
     * Notify a client of its acceptance and set this instance to handle that client
     *
     * @param client the client we will be handling
     */
    private void onClientAccepted(NetworkDevice client) {
        try {
            // accept the client
            mCommandConnection.sendCommand(new ConnectionRequestResponse(true));

            // store client identification
            mClient = client;
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
        mCommandConnection = new CommandConnection(mCommandListener);

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
        mDataConnection.setDataSink(mDataSink);

        // begin listening for SensorData objects
        mDataConnection.start();
    }

    /**
     * Notify the client of all required buttons
     *
     * @throws IOException if the command could not be sent
     */
    public void updateButtons(Map<Integer, String> buttonList) throws IOException {
        // if the button list was changed, we need to update the clients buttons
        sendCommand(new UpdateButtons(buttonList));
    }

    /**
     * Notify the client of all required sensors
     *
     * @throws IOException if the command could not be sent
     */
    public void updateSensors(Set<SensorType> requiredSensors) throws IOException {
        sendCommand(new SetSensorCommand(new ArrayList<>(requiredSensors)));
    }

    /**
     * @return the port the {@link CommandConnection} is listening on
     */
    public int getCommandPort() {
        return mCommandConnection.getLocalPort();
    }

    /**
     * @return the port the {@link UdpDataConnection} is listening on
     */
    public int getDataPort() {
        return mDataConnection.getLocalPort();
    }
}

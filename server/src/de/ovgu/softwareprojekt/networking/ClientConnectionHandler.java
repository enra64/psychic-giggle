package de.ovgu.softwareprojekt.networking;

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
 * This class manages the command- and data connection to a single client
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

    private NetworkDevice mClient;

    private ExceptionListener mExceptionListener;

    private OnCommandListener mCommandListener;

    private DataSink mDataSink;

    public ClientConnectionHandler(ExceptionListener exceptionListener, OnCommandListener commandListener, DataSink dataSink) throws IOException {
        initialiseCommandConnection();
        initialiseDataConnection();

        mExceptionListener = exceptionListener;
        mCommandListener = commandListener;
    }

    public void start() {

    }

    public void sendCommand(Command command) throws IOException {
        mCommandConnection.sendCommand(command);
    }

    public NetworkDevice getClient(){
        return mClient;
    }

    public void handleConnectionRequest(ConnectionRequest connectionRequest, boolean acceptClient) throws UnknownHostException {
        // initialise the connection to be able to send the request answer
        mCommandConnection.setRemote(connectionRequest.self.getInetAddress(), connectionRequest.self.commandPort);

        // accept or reject the client
        if (acceptClient)
            onClientAccepted(connectionRequest.self);
        else
            onClientRejected();
    }

    public void close() {
        mCommandConnection.close();
        mDataConnection.close();
    }

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

    public int getCommandPort() {
        return mCommandConnection.getLocalPort();
    }

    public int getDataPort() {
        return mDataConnection.getLocalPort();
    }
}

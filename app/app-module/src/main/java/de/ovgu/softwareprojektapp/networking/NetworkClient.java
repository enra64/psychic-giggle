package de.ovgu.softwareprojektapp.networking;

import android.os.AsyncTask;

import java.io.IOException;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * The network client contains both the command and the data connection. Use {@link #sendCommand(Command)}
 * to send commands, request a connection with the server with {@link #requestConnection()}. To send
 * data with it, you may use {@link #onData(SensorData)}, but really, this class implements {@link DataSink}
 * so you can simply give it to {@link DataSource DataSources}. You will be notified of incoming commands
 * via the listener you had to supply in the constructor.
 */
public class NetworkClient implements DataSink {
    /**
     * Stores necessary information about our server, like data port, command port, and its address
     */
    private NetworkDevice mServer;

    /**
     * Stores all information necessary for a server to communicate with use: command and data port.
     * The name is also stored.
     */
    private NetworkDevice mSelf;

    /**
     * Connection for everything not {@link de.ovgu.softwareprojekt.SensorData}
     */
    private CommandConnection mCommandConnection;

    /**
     * Connection for everything {@link de.ovgu.softwareprojekt.SensorData}
     */
    private UdpConnection mOutboundDataConnection;

    /**
     * Callback for incoming commands
     */
    private OnCommandListener mCommandListener;

    /**
     * Create a new network client. To connect to a server, call {@link #requestConnection()}, and wait
     * for an answer from the server
     *
     * @param server          the server we want to talk to
     * @param selfName        how to identify the client
     * @param commandListener who should be called back for incoming commands
     */
    public NetworkClient(NetworkDevice server, String selfName, OnCommandListener commandListener) {
        // store the server device information
        mServer = server;

        // who should be notified of incoming commands
        mCommandListener = commandListener;

        // initialise command and data connection
        initialiseCommandConnection();
        initialiseDataConnection();

        // store our own identification now that we know the data and command ports
        mSelf = new NetworkDevice(
                selfName,
                mCommandConnection.getLocalPort(),
                mOutboundDataConnection.getLocalPort()
        );
    }

    /**
     * Configure both the command and the data connection to connect to {@link #mServer}
     */
    private void initialiseCommandConnection() {
        // create a command connection
        try {
            // create the command connection, set this to receive incoming commands
            mCommandConnection = new CommandConnection(mCommandListener);

            // we want to send commands to the server
            mCommandConnection.setRemote(mServer);

            // start the listening process
            mCommandConnection.start();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    /**
     * Initialises a data connection. the outbound connection will know its port after that
     */
    private void initialiseDataConnection() {
        // create the outbound data connection
        try {
            // send all data to the server to the data port
            mOutboundDataConnection = new UdpConnection(mServer);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    /**
     * Request a connection with the configured server
     *
     * @throws IOException if the command connection could not be started
     */
    public void requestConnection() throws IOException {
        // start listening for incoming commands
        mCommandConnection.start();

        // send a connection request
        sendCommand(new ConnectionRequest(mSelf));
    }

    /**
     * Send a command to the server
     *
     * @param command the command to be sent to the server
     */
    public void sendCommand(Command command) {
        new SendCommand().execute(command);
    }

    /**
     * Send incoming sensor data to the server
     *
     * @param sensorData outgoing sensor data
     */
    @Override
    public void onData(SensorData sensorData) {
        mOutboundDataConnection.onData(sensorData);
    }

    /**
     * finalize operations
     */
    @Override
    public void close() {
        mOutboundDataConnection.close();
    }

    /**
     * This class is a wrapper for {@link CommandConnection#sendCommand(Command) sending commands}
     * to avoid dealing with network on the ui thread. Use as follows:
     * <br><br>
     * {@code new SendCommand().execute(new WhatEverCommand()); }
     * <br><br>
     * where WhatEverCommand is a subclass of {@link Command}
     */
    private class SendCommand extends AsyncTask<Command, Void, Void> {
        @Override
        protected Void doInBackground(Command... commands) {
            try {
                mCommandConnection.sendCommand(commands[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

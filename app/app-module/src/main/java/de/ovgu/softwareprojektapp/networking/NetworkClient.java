package de.ovgu.softwareprojektapp.networking;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.ConnectionWatch;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.ConnectionAliveCheck;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequestResponse;
import de.ovgu.softwareprojekt.control.commands.EndConnection;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

/**
 * The network client contains both the command and the data connection. Use {@link #sendCommand(AbstractCommand)}
 * to send commands, request a connection with the server with {@link #requestConnection()}. To send
 * data with it, you may use {@link #onData(SensorData)}, but really, this class implements {@link DataSink}
 * so you can simply give it to {@link DataSource DataSources}. You will be notified of incoming commands
 * via the listener you had to supply in the constructor.
 */
public class NetworkClient implements DataSink, ExceptionListener, OnCommandListener, ConnectionWatch.TimeoutListener {
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
     * Callbacks for server timeout events
     */
    private TimeoutListener mTimeoutListener;

    /**
     * listener for exceptions interesting to the user
     */
    private ExceptionListener mExceptionListener = null;

    /**
     * Handles our connection checking
     */
    private ConnectionWatch mConnectionWatch;

    /**
     * Create a new network client. To connect to a server, call {@link #requestConnection()}, and wait
     * for an answer from the server
     *
     * @param server          the server we want to talk to
     * @param selfName        how to identify the client
     * @param commandListener who should be called back for incoming commands
     */
    public NetworkClient(NetworkDevice server, String selfName, OnCommandListener commandListener, ExceptionListener exceptionListener, TimeoutListener timeoutListener) {
        // store the server device information
        mServer = server;

        // store listeners
        mTimeoutListener = timeoutListener;
        mExceptionListener = exceptionListener;
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

        // create a new passive connection watch
        mConnectionWatch = new ConnectionWatch(mSelf, this, mExceptionListener);
    }

    /**
     * Configure both the command and the data connection to connect to {@link #mServer}
     */
    private void initialiseCommandConnection() {
        // create a command connection
        try {
            // create the command connection, set this to receive incoming commands
            mCommandConnection = new CommandConnection(this);

            // we want to send commands to the server
            mCommandConnection.setRemote(mServer);

            // start the listening process
            mCommandConnection.start();
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "NetworkClient: IOException when trying to initialise CommandConnection");
        }
    }

    /**
     * Initialises a data connection. the outbound connection will know its port after that
     */
    private void initialiseDataConnection() {
        // create the outbound data connection
        try {
            // send all data to the server to the data port
            mOutboundDataConnection = new UdpConnection(mServer, this);
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "NetworkClient: IOException when trying to initialise DataConnection");
        }
    }

    /**
     * Start a timer that periodically checks whether we have recently communicated with the server.
     * If the last connection is too old, it gives alarm, as the connection has probably timed out
     */
    private void startConnectionCheckTimer() {
        // set the last connection to NOW (+ execution delay of timer) to avoid immediate disconnect
        mConnectionWatch.start();
    }

    /**
     * Request a connection with the configured server
     */
    public void requestConnection() {
        try {
            // start listening for incoming commands
            mCommandConnection.start();

            // send a connection request
            sendCommand(new ConnectionRequest(mSelf));

        } catch (IOException e) {
            mExceptionListener.onException(this, e, "NetworkClient: could not start listening for commands");
        }
    }

    /**
     * Send a command to the server
     *
     * @param command the command to be sent to the server
     */
    public void sendCommand(AbstractCommand command) {
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
        mCommandConnection.close();
        mConnectionWatch.close();
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        // relay the exception, but set us as origin, because the UdpConnection instance is not known to users
        mExceptionListener.onException(this, exception, origin.getClass().toString() + info);
    }

    /**
     * Return a {@link NetworkDevice} identifying this network client
     */
    public NetworkDevice getSelf(){
        return mSelf;
    }

    /**
     * Notifies the server that we will stop working
     */
    public void signalConnectionEnd() {
        sendCommand(new EndConnection(mSelf));
    }

    /**
     * Intercept certain commands
     * @param origin hostname of whoever sent the packet
     * @param command the command that was sent
     */
    @Override
    public void onCommand(InetAddress origin, AbstractCommand command) {
        switch (command.getCommandType()){
            // completely handle connection checks
            case ConnectionAliveCheck:
                ConnectionAliveCheck response = (ConnectionAliveCheck) command;
                response.answerer = getSelf();
                sendCommand(response);
                // notify the connection watch of the event
                mConnectionWatch.onCheckEvent();
                break;
            case ConnectionRequestResponse:
                ConnectionRequestResponse res = (ConnectionRequestResponse) command;
                // if the connection was granted, start the connection check timer
                if(res.grant)
                    startConnectionCheckTimer();

                // relay the connection request
                mCommandListener.onCommand(origin, command);
                break;
            default:
                mCommandListener.onCommand(origin, command);
        }
    }

    @Override
    public void onTimeout(long responseDelay) {

    }

    /**
     * This class is a wrapper for {@link CommandConnection#sendCommand(AbstractCommand) sending commands}
     * to avoid dealing with network on the ui thread. Use as follows:
     * <br><br>
     * {@code new SendCommand().execute(new WhatEverCommand()); }
     * <br><br>
     * where WhatEverCommand is a subclass of {@link AbstractCommand}
     */
    private class SendCommand extends AsyncTask<AbstractCommand, Void, Void> {
        @Override
        protected Void doInBackground(AbstractCommand... commands) {
            try {
                mCommandConnection.sendCommand(commands[0]);
            } catch (IOException e) {
                mExceptionListener.onException(
                        NetworkClient.this,
                        e,
                        "NetworkClient: exception when trying to send a command");
            }
            return null;
        }
    }

    public interface TimeoutListener {
        void onConnectionTimeout();
    }
}

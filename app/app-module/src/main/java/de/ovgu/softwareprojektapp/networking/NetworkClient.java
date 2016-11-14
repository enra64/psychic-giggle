package de.ovgu.softwareprojektapp.networking;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetAddress;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojektapp.SendActivity;

/**
 * Created by arne on 11/14/16.
 */

public class NetworkClient implements DataSink {
    /**
     * Stores necessary information about our server, like data port, command port, and its address
     */
    private NetworkDevice mServer;

    private NetworkDevice mSelf;

    /**
     * Connection for everything not {@link de.ovgu.softwareprojekt.SensorData}
     */
    private CommandConnection mCommandConnection;

    /**
     * Connection for everything {@link de.ovgu.softwareprojekt.SensorData}
     */
    private UdpConnection mOutboundDataConnection;

    private OnCommandListener mCommandListener;

    public NetworkClient(
            NetworkDevice server,
            String selfName,
            OnCommandListener commandListener) {
        mServer = server;

        mCommandListener = commandListener;

        initialiseCommandConnection();

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
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    public void requestConnection() throws IOException {
        mCommandConnection.start();

        sendCommand(new ConnectionRequest(mSelf));
    }

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

    void sendCommand(Command command) {
        new SendCommand().execute(command);
    }

    @Override
    public void onData(SensorData sensorData) {
        mOutboundDataConnection.onData(sensorData);
    }

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

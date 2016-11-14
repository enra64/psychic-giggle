package de.ovgu.softwareprojektapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.net.InetAddress;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojektapp.networking.UdpConnection;
import de.ovgu.softwareprojektapp.sensors.Gyroscope;

public class SendActivity extends AppCompatActivity implements OnCommandListener {
    // de-magic-stringify the intent extra keys
    static final String EXTRA_SERVER_NAME = "Name";
    static final String EXTRA_SERVER_ADDRESS = "Address";
    static final String EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort";
    static final String EXTRA_SERVER_PORT_COMMAND = "CommandPort";
    static final String EXTRA_SERVER_PORT_DATA = "DataPort";

    static final String EXTRA_SELF_NAME = "SelfName";

    /**
     * Stores necessary information about our server, like data port, command port, and its address
     */
    private NetworkDevice mServer;

    /**
     * Connection for everything not {@link de.ovgu.softwareprojekt.SensorData}
     */
    private CommandConnection mCommandConnection;

    /**
     * Connection for everything {@link de.ovgu.softwareprojekt.SensorData}
     */
    private UdpConnection mOutboundDataConnection;

    /**
     * Gyroscope sensor object; implements datasource, listens to gyroscope changes
     */
    private Gyroscope mGyroscope = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // we create a NetworkDevice from our extras to have all the data we need in a neat package
        parseServer();

        // we need to initialise the command and data connections
        initialiseConnections();
    }

    /**
     * Configure both the command and the data connection to connect to {@link #mServer}
     */
    private void initialiseConnections(){
        // create a command connection
        try {
            // create the command connection, set this to receive incoming commands
            mCommandConnection = new CommandConnection(this);

            // we want to send commands to the server
            mCommandConnection.setRemote(mServer);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }

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
     * Create a NetworkDevice object from the intent extras
     */
    private void parseServer() {
        Bundle in = getIntent().getExtras();
        mServer = new NetworkDevice(
                in.getString(EXTRA_SERVER_NAME),
                in.getInt(EXTRA_SERVER_PORT_DISCOVERY),
                in.getInt(EXTRA_SERVER_PORT_COMMAND),
                in.getInt(EXTRA_SERVER_PORT_DATA),
                in.getString(EXTRA_SERVER_ADDRESS));
    }

    /**
     * Create a NetworkDevice describing this client
     * @return a NetworkDevice with correctly configured name, command port and data port
     */
    private NetworkDevice getSelf() {
        return new NetworkDevice(
                getIntent().getExtras().getString(EXTRA_SELF_NAME),
                mCommandConnection.getLocalPort(),
                mOutboundDataConnection.getLocalPort()
        );
    }

    /**
     * This function is called by a button. Currently, it starts listening to server commands and
     * requests a connection with the server given as an intent extra
     */
    public void initiateConnection(View v) {
        try {
            // begin listening for commands
            mCommandConnection.start();

            // send a connection request
            new SendCommand().execute(new ConnectionRequest(getSelf()));
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    /**
     * Enable or disable the gyroscope
     * @param enable true if the gyroscope must be enabled
     */
    private void setGyroscope(boolean enable) {
        if (enable) {
            // create a new gyroscope if that has not yet happened
            if (mGyroscope == null)
                mGyroscope = new Gyroscope(this);

            // make the gyroscope directly output to the network
            mGyroscope.setDataSink(mOutboundDataConnection);

            // start listening to gyroscope events
            mGyroscope.start();

        } else {
            // disable the gyroscope
            mGyroscope.close();
            mGyroscope = null;
        }
    }

    /**
     * Called whenever a new command comes in
     *
     * @param origin  where the command comes from
     * @param command the command itself
     */
    @Override
    public void onCommand(InetAddress origin, Command command) {
        // decide what to do with the command
        switch (command.getCommandType()) {
            case SetSensor:
                // enable or disable a sensor
                SetSensorCommand com = (SetSensorCommand) command;

                // decide which sensor should be switched
                switch (com.sensorType) {
                    case Gyroscope:
                        setGyroscope(com.enable);
                        break;
                    default:
                        break;
                }

                break;
            // ignore unhandled commands
            default:
                break;
        }
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